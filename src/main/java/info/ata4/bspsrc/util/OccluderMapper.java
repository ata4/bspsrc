package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.*;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Class for mapping occluder entities to their original brushes
 */
public class OccluderMapper {

    private static final Logger L = LogUtils.getLogger();

    private BspSourceConfig config;
    private BspData bsp;

    /**
     * Occluders aren't worldbrushes, so we can limit our 'search' by limiting the brushes to non worldbrushes.
     * This is used for both {@link OccMappingMode#MANUAL} and  {@link OccMappingMode#ORDERED}
     */
    private ArrayList<DBrush> nonWorldBrushes;

    /**
     * ONLY used for {@link OccMappingMode#ORDERED}
     */
    private SortedMap<Integer, Integer> potentialOccluderBrushes;

    /**
     * Predicate that test if a {@link DTexInfo} matches one that would be used for occluder brush sides.
     * ONLY used for {@link OccMappingMode#ORDERED}, because I'm not sure if other games use different SurfaceFlags!
     */
    private static final Predicate<DTexInfo> matchesOccluderTexInfo = dTexInfo -> dTexInfo.flags.equals(EnumSet.of(SurfaceFlag.SURF_NOLIGHT)) || dTexInfo.flags.equals(EnumSet.of(SurfaceFlag.SURF_TRIGGER, SurfaceFlag.SURF_NOLIGHT));


    public OccluderMapper(BspData bsp, BspSourceConfig config) {
        this.config = config;
        this.bsp = bsp;

        prepareNonWorldBrushes();
        preparePotentialOccBrushes();
    }

    private void prepareNonWorldBrushes()
    {
        BspTreeStats tree = new BspTreeStats(bsp);
        tree.walk(0);

        nonWorldBrushes = new ArrayList<>(bsp.brushes.subList(tree.getMaxBrushLeaf() + 1, bsp.brushes.size()));
    }

    /**
     * Because there is no direct way of determining if a brush was used as an occluder, we have to use a process of elimination to get a list of potential occluder brushes.
     * Fills {@code potentialOccluderBrushes} with potential brushes, that could have represented occluders as keys and and the amount of brush sides, that could have represented occluder faces as values
     */
    private void preparePotentialOccBrushes() {
        potentialOccluderBrushes = nonWorldBrushes.stream()
                .filter(dBrush -> dBrush.contents.equals(EnumSet.of(BrushFlag.CONTENTS_SOLID))) // Every occluder brush only seems to have this one BrushFlag
                .collect(Collectors.toMap(
                        bsp.brushes::indexOf,
                        dBrush -> (int) bsp.brushSides.subList(dBrush.fstside, dBrush.fstside + dBrush.numside).stream()
                                .map(dBrushSide -> bsp.texinfos.get(dBrushSide.texinfo))
                                .filter(matchesOccluderTexInfo)
                                .count(),
                        (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        TreeMap::new    //Important! because we wanna preserve the index order of the original brush lump, so we can use it in the 'ORDERED MAPPING' method
                ));

        // Remove every brush, that didn't actually had a brush side which could be a potential occluder face
        potentialOccluderBrushes.values().removeIf(faceCount -> faceCount == 0);
    }

    /**
     * Maps all {@link DOccluderData} to their original brushes by comparing their faces to brush sides and determining if they are similar (Identical or contained in the brushside)
     *
     * @return A {@link Map} where the keys represent an occluder an the values a list of brush ids
     */
    private Map<Integer, Set<Integer>> manualMapping() {
        // Map all occluders to a list of representing brushes
        Map<Integer, Set<Integer>> occBrushMapping = bsp.occluderDatas.stream()
                .collect(Collectors.toMap(
                        dOccluderData -> bsp.occluderDatas.indexOf(dOccluderData),
                        this::mapOccluder
                ));

        // Remove every occluder mapping that has 0 brushes assigned, because we couldn't find a mapping
        occBrushMapping.values().removeIf(list -> list.size() == 0);

        return occBrushMapping;
    }

    /**
     * Finds all brushes that represent this occluder and return their index in {@link BspData#brushes}
     *
     * @param dOccluderData occluder to find brushes for
     * @return an Integer list of brush indexes
     */
    private Set<Integer> mapOccluder(DOccluderData dOccluderData) {
        return bsp.occluderPolyDatas.subList(dOccluderData.firstpoly, dOccluderData.firstpoly + dOccluderData.polycount).stream()
                .map(dOccluderPolyData -> nonWorldBrushes.stream()
                        .filter(dBrush -> bsp.brushSides.subList(dBrush.fstside, dBrush.fstside + dBrush.numside).stream()
                                .anyMatch(brushSide -> occFacesContainsBrushFace(dOccluderPolyData, dBrush, brushSide))
                        )
                        .findAny()
                )
                .filter(Optional::isPresent)
                .map(optionalDBrush -> bsp.brushes.indexOf(optionalDBrush.get()))
                .filter(index -> {assert index != -1; return index != -1;})   //Shouldn't happen, but just in case 'indexOf' returns -1 we filter these out here
                .collect(Collectors.toSet());
    }

    /**
     * Test if the specified occluder face contains the specified brush side
     *
     * @param dOccluderPolyData the occluder face
     * @param dBrush the brush, the brush side belongs to
     * @param dBrushSide the brush side
     * @return true if the specified occluder face contains the specified brush side, false otherwise
     */
    private boolean occFacesContainsBrushFace(DOccluderPolyData dOccluderPolyData, DBrush dBrush, DBrushSide dBrushSide) {
        return VectorUtil.matchingAreaPercentage(dOccluderPolyData, dBrush, dBrushSide, bsp) > 0; // <- May need to be some epsilon instead of 0
    }

    /**
     * Mapps all occluder entities in to their brushes in ascending brush index order
     *
     * @return A {@code Map} with occluder ids as keys and and a List of Integers as values
     */
    private Map<Integer, Set<Integer>> orderedMapping() {
        List<Integer> reverseOccluderFaces = bsp.occluderDatas.stream()
                .flatMap(dOccluderData -> IntStream.range(0, dOccluderData.polycount)
                        .mapToObj(value -> bsp.occluderDatas.indexOf(dOccluderData))
                )
                .collect(Collectors.toList());

        List<Integer> reverseBrushFaces = potentialOccluderBrushes.entrySet().stream()
                .flatMap(entry -> LongStream.range(0, entry.getValue())
                        .mapToObj(value -> entry.getKey())
                )
                .sorted()
                .collect(Collectors.toList());

        int min = Math.min(reverseOccluderFaces.size(), reverseBrushFaces.size());

        return IntStream.range(0, min)
                .boxed()
                .collect(Collectors.groupingBy(reverseOccluderFaces::get, Collectors.collectingAndThen(
                        Collectors.toSet(),
                        indices -> indices.stream()
                                .map(reverseBrushFaces::get)
                                .collect(Collectors.toSet())
                )));
    }

    /**
     * Creates and returns an occluder to brushes mapping
     * <p></p>
     * If the amount of occluder faces is equal to the amount of occluder faces of all potential occluder brushes we just map the occluder faces in order to the brushes.
     * This is possible because vBsp seems to compile the occluders in order.
     * If this is not the case we use {@code manualMapping} to manually map the occluders to their brushes
     *
     * @return A {@code Map} where the keys represent occluder ids and values a list of brush ids
     */
    public Map<Integer, Set<Integer>> getOccBrushMapping() {
        if (!config.writeOccluders)
            return Collections.emptyMap();

        if (bsp.occluderDatas.size() == 0) {
            L.info("No occluders to reallocate...");
            return Collections.emptyMap();
        }

        if (config.occForceManualMapping) {
            L.info("Forced manual occluder mapping method");
            return OccMappingMode.MANUAL.map(this);
        }

        int occluderFacesNum = potentialOccluderBrushes.values().stream()
                .mapToInt(i -> i)
                .sum();

        int occluderBrushFacesNum = bsp.occluderDatas.stream()
                .mapToInt(occluder -> occluder.polycount)
                .sum();

        if (occluderFacesNum == occluderBrushFacesNum) {
            L.info("Equal amount of occluder faces and occluder brush faces. Using '" + OccMappingMode.ORDERED + "' method");
            return OccMappingMode.ORDERED.map(this);
        } else {
            L.info("Unequal amount of occluder faces and occluder brush faces. Falling back to '" + OccMappingMode.MANUAL + "' method");
            return OccMappingMode.MANUAL.map(this);
        }
    }

    public enum OccMappingMode {
        MANUAL(OccluderMapper::manualMapping),
        ORDERED(OccluderMapper::orderedMapping);

        private Function<OccluderMapper, Map<Integer, Set<Integer>>> mapper;

        OccMappingMode(Function<OccluderMapper, Map<Integer, Set<Integer>>> mapper) {
            this.mapper = mapper;
        }

        public Map<Integer, Set<Integer>> map(OccluderMapper occMapper) {
            return mapper.apply(occMapper);
        }

        @Override
        public String toString() {
            return super.toString().substring(0, 1).toUpperCase() + super.toString().substring(1).toLowerCase();
        }
    }
}
