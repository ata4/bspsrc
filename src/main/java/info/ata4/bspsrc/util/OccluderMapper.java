package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.*;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.modules.texture.ToolTexture;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * Class for mapping occluder entities to their original brushes
 */
public class OccluderMapper {

    private static final Logger L = LogUtils.getLogger();

    private BspSourceConfig config;
    private BspData bsp;

    // Occluders aren't worldbrushes, so we can limit our 'search' by limiting the brushes to non worldbrushes
    private ArrayList<DBrush> nonWorldBrushes;

    // ONLY used for 'ORDERED MAPPING'
    private ArrayList<DBrush> potentialOccluderBrushes;
    private Map<Integer, Integer> occluderFaces;

    // Predicate that test if a texture name matches one of the valid occluder texture names.
    // ONLY used for 'ORDERED MAPPING', because for some reason vBsp sometimes changes textures of all occluders to some arbitrary texture
    private static final Predicate<String> matchesOccluder = s -> s.equalsIgnoreCase(ToolTexture.TRIGGER) ||
                                                            s.equalsIgnoreCase(ToolTexture.OCCLUDER);

    public OccluderMapper(BspData bsp, BspSourceConfig config) {
        this.config = config;
        this.bsp = bsp;

        prepareNonWorldBrushes();
        preparePotentialOccBrushes();
        prepareOccluderFaces();
    }

    private void prepareNonWorldBrushes()
    {
        BspTreeStats t = new BspTreeStats(bsp);
        t.walk(0);

        nonWorldBrushes = new ArrayList<>(bsp.brushes.subList(t.getMaxBrushLeaf() + 1, bsp.brushes.size()));
    }

    /**
     * Fills {@code potentialOccluderBrushes} with potential brushes that could have represented occluders
     */
    private void preparePotentialOccBrushes() {
        potentialOccluderBrushes = nonWorldBrushes.stream()                                                                 // Iterate over every existing brush
                .filter(dBrush -> !dBrush.isDetail())                                                                   // - Filter all out that have the 'detail' flag
                .filter(dBrush -> !dBrush.isAreaportal())                                                               // - Filter all out that have the 'areaportal' flag
                .filter(dBrush -> bsp.brushSides.subList(dBrush.fstside, dBrush.fstside + dBrush.numside).stream()      // - Iterate over every brush side and test if it texture matches 'matchesOccluder'
                        .map(dBrushSide -> bsp.texinfos.get((int) dBrushSide.texinfo))                                  // -- Map brushside to textinfo
                        .filter(dTexInfo -> dTexInfo.texdata >= 0)                                                      // -- Skip brushsides that don't have textdata
                        .map(dTexInfo -> bsp.texdatas.get(dTexInfo.texdata))                                            // -- Map textinfo to textdata
                        .map(dTexData -> bsp.texnames.get(dTexData.texname))                                            // -- Map textdata to textname
                        .anyMatch(matchesOccluder)                                                                      // -- Test if any texture matches 'matchesOccluder'
                )
                .collect(Collectors.toCollection(ArrayList::new));                                                      // - Collect every brush into a list that had at ^2least one brushside that matched 'matchesOccluder'
    }

    /**
     * Fills {@code occluderFaces} with all potential brushes mapped an {@code Integer} representing the number of faces that matches '{@code matchesOccluder}'
     */
    private void prepareOccluderFaces() {
        occluderFaces = potentialOccluderBrushes.stream()
                .collect(Collectors.toMap(dBrush -> bsp.brushes.indexOf(dBrush), dBrush -> (int) bsp.brushSides.subList(dBrush.fstside, dBrush.fstside + dBrush.numside).stream()
                        .map(dBrushSide -> bsp.texinfos.get((int) dBrushSide.texinfo))
                        .filter(dTexInfo -> dTexInfo.texdata >= 0)
                        .map(dTexInfo -> bsp.texdatas.get(dTexInfo.texdata))
                        .map(dTexData -> bsp.texnames.get(dTexData.texname))
                        .filter(matchesOccluder)
                        .count())
                );
    }

    /**
     * Maps all occluder entities to their brushes in form of a {@code Map}
     *
     * @return A {@code Map} where the keys represent an occluder an the values a list of brush ids
     */
    private Map<Integer, Set<Integer>> manualMapping() {
        // Map all occluders to a list of representing brushes
        Map<Integer, Set<Integer>> occBrushMapping = bsp.occluderDatas.stream()
                .collect(Collectors.toMap(dOccluderData -> bsp.occluderDatas.indexOf(dOccluderData), this::mapOccluder));

        // Remove every occluder mapping that has 0 brushes assigned, because we couldn't find a mapping
        occBrushMapping.values().removeIf(list -> list.size() == 0);

        // Because the Texturebuilder needs to know which brush is a occluder we flag them here. (The Texturebuilder needs to know this information, because the brushside that represents the occluder has almost always the wrong tooltexture applied, which we need to fix)
        occBrushMapping.values().forEach(brushIndexes -> brushIndexes.forEach(index -> bsp.brushes.get(index).flagAsOccluder(true)));


        return occBrushMapping;
    }

    /**
     * Finds all brushes that represent this occluder and return their indexes of bsp.brushes
     *
     * @param dOccluderData occluder to find brushes for
     * @return a Integer list of brush indexes
     */
    private Set<Integer> mapOccluder(DOccluderData dOccluderData) {
        return bsp.occluderPolyDatas.subList(dOccluderData.firstpoly, dOccluderData.firstpoly + dOccluderData.polycount).stream()
                .map(dOccluderPolyData -> nonWorldBrushes.stream()
                        .filter(dBrush -> bsp.brushSides.subList(dBrush.fstside, dBrush.fstside + dBrush.numside).stream()
                                .anyMatch(brushSide -> occFacesMatchesBrushFace(dOccluderPolyData, dBrush, brushSide)))
                        .findAny())
                .filter(Optional::isPresent)
                .map(dBrush -> bsp.brushes.indexOf(dBrush.get()))
                .filter(index -> index != -1)   //Shouldn't happen, but just in case 'indexOf' returns -1 we filter these out here
                .collect(Collectors.toSet());
    }

    /**
     * Test if the specified occluder face matches with the specified brush side
     *
     * @param dOccluderPolyData the occluder face
     * @param dBrush the brush, the brush side belongs to
     * @param dBrushSide the brush side
     * @return true if the specified occluder face matches with the specified brush side, false otherwise
     */
    private boolean occFacesMatchesBrushFace(DOccluderPolyData dOccluderPolyData, DBrush dBrush, DBrushSide dBrushSide) {
        Winding w = WindingFactory.fromSide(bsp, dBrush, dBrushSide);

        // If the amount of vertices are unequal we know the cant be identical
        if (w.size() != dOccluderPolyData.vertexcount)
            return false;

        // Test vertices against each other
        for (int j = 0; j < w.size(); j++) {
            boolean identical = true;
            for (int k = 0; k < w.size(); k++) {
                Vector3f occVertex = bsp.verts.get(bsp.occluderVerts.get(dOccluderPolyData.firstvertexindex + k)).point;
                if (!occVertex.equals(w.get((j + k) % w.size()))) {
                    identical = false;
                    break;
                }
            }
            if (identical)
                return true;
        }
        return false;
    }

    /**
     * Mapps all occluder entities in to their brushes in ascending brush index order
     *
     * @return A {@code Map} with occluder ids as keys and and a List of Integers as values
     */
    private Map<Integer, Set<Integer>> orderedMapping() {
        // Get the min amount of occluder faces that can be process#
        // This should always be limited by the amount of occluderData but we test nonetheless
        long min = Math.min(occluderFaces.values().stream().mapToInt(i -> i).sum(), bsp.occluderDatas.stream().mapToInt(occluder -> occluder.polycount).sum());

        HashMap<Integer, Set<Integer>> occBrushMap = new HashMap<>();

        // Convert the amount of occluderfaces we can process into actual occluders (occluders can have multiple faces)
        int minOccluders = 0;
        int index = 0;
        while (min > 0) {
            if (bsp.occluderDatas.get(index).polycount > min)
                break;

            min -= bsp.occluderDatas.get(index).polycount;
            minOccluders++;
            index++;
        }

        // Map all occluder to brushes in ascending brush index order
        // And yeah, i know this complete method looks very ulgy. Didn't know how to write a better algorithm for this
        int occBrushIndex = 0;
        int remaining = 0;
        for (int i = 0; i < minOccluders; i++) {
            Set<Integer> occBrushes = new HashSet<>();

            int j = bsp.occluderDatas.get(i).polycount;
            while (j > 0) {
                occBrushes.add(bsp.brushes.indexOf(potentialOccluderBrushes.get(occBrushIndex)));
                if (remaining < 0)
                    j -= remaining;
                else
                    j -= occluderFaces.get(bsp.brushes.indexOf(potentialOccluderBrushes.get(occBrushIndex)));
                remaining = 0;

                if (j >= 0)
                    occBrushIndex++;
            }
            remaining = j;

            occBrushMap.put(occBrushMap.size(), occBrushes);

            // Because the Texturebuilder needs to know which brush is a occluder we flag them here. (The Texturebuilder needs to know this information because the brushside that represents the occluder has almost always the wrong tooltexture applied)
            for (int brushID: occBrushes) {
                if (brushID == -1)
                    continue;

                bsp.brushes.get(brushID).flagAsOccluder(true);
            }
        }
        return occBrushMap;
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

        if (config.occForceMapping) {
            L.info("Forced occluder method: '" + config.occMappingMode + "'");
            return config.occMappingMode.map(this);
        }

        int occluderFacesNum = occluderFaces.values().stream()
                .mapToInt(i -> i)
                .sum();

        int occluderBrushFacesNum = bsp.occluderDatas.stream()
                .mapToInt(occluder -> occluder.polycount)
                .sum();

        if (occluderFacesNum == occluderBrushFacesNum) {
            L.info("Equal amount of occluder faces as occluder brush faces. Using '" + OccMappingMode.ORDERED + "' method");
            return OccMappingMode.ORDERED.map(this);
        } else {
            L.info("Unequal amount of occluder faces as occluder brush faces. Falling back to '" + OccMappingMode.MANUAL + "' method");
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
