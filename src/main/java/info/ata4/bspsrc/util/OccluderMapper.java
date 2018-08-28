package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.modules.texture.ToolTexture;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * Class for mapping occluder entities to their original brushes
 */
public class OccluderMapper {

    private static final Logger L = LogUtils.getLogger();

    private BspSourceConfig config;
    private BspData bsp;

    private ArrayList<DBrush> potentialOccluderBrushes;
    private Map<Integer, Integer> occluderFaces;

    // Predicate that test if a texture name matches one of the valid occluder texture names. YES, an occluder can contain the areaportal texture after compile.
    // Don't ask me why but if there are areaportals in a map all occluder textures are getting changed to areaportal textures
    private static final Predicate<String> matchesOccluder = s -> s.equalsIgnoreCase(ToolTexture.TRIGGER) ||
                                                            s.equalsIgnoreCase(ToolTexture.OCCLUDER) ||
                                                            s.equalsIgnoreCase(ToolTexture.AREAPORTAL);

    public OccluderMapper(BspData bsp, BspSourceConfig config) {
        this.config = config;
        this.bsp = bsp;

        preparePotentialOccBrushes();
        prepareOccluderFaces();
    }

    /**
     * Fills {@code potentialOccluderBrushes} with potential brushes that could have represented occluders
     */
    private void preparePotentialOccBrushes() {
        potentialOccluderBrushes = bsp.brushes.stream()                                                                 // Iterate over every existing brush
                .filter(dBrush -> !dBrush.isDetail())                                                                   // - Filter all out that have the 'detail' flag
                .filter(dBrush -> !dBrush.isAreaportal())                                                               // - Filter all out that have the 'areaportal' flag
                .filter(dBrush -> IntStream.range(dBrush.fstside, dBrush.fstside + dBrush.numside)                      // - Iterate over every brush side and test if it texture matches 'matchesOccluder'
                        .mapToObj(i -> bsp.brushSides.get(i))                                                           // -- Map brushside index to brushside
                        .map(dBrushSide -> bsp.texinfos.get((int) dBrushSide.texinfo))                                  // -- Map brushside to textinfo
                        .filter(dTexInfo -> dTexInfo.texdata >= 0)                                                      // -- Skip brushsides that don't have textdata
                        .map(dTexInfo -> bsp.texdatas.get(dTexInfo.texdata))                                            // -- Map textinfo to textdata
                        .map(dTexData -> bsp.texnames.get(dTexData.texname))                                            // -- Map textdata to textname
                        .anyMatch(matchesOccluder)                                                                      // -- Test if any texture matches 'matchesOccluder'
                )
                .collect(Collectors.toCollection(ArrayList::new));                                                      // - Collect every brush into a list that had atleast one brushside that matched 'matchesOccluder'
    }

    /**
     * Fills {@code occluderFaces} with all potential brushes mapped an {@code Integer} representing the number of faces that matches '{@code matchesOccluder}'
     */
    private void prepareOccluderFaces() {
        occluderFaces = potentialOccluderBrushes.stream()
                .map(dBrush -> {
                    int faces = (int) IntStream.range(dBrush.fstside, dBrush.fstside + dBrush.numside)
                            .mapToObj(i -> bsp.brushSides.get(i))
                            .map(dBrushSide -> bsp.texinfos.get((int) dBrushSide.texinfo))
                            .filter(dTexInfo -> dTexInfo.texdata >= 0)
                            .map(dTexInfo -> bsp.texdatas.get(dTexInfo.texdata))
                            .map(dTexData -> bsp.texnames.get(dTexData.texname))
                            .filter(matchesOccluder)
                            .count();

                    return new AbstractMap.SimpleEntry<>(bsp.brushes.indexOf(dBrush), faces);
                })
                .collect(Collectors.toMap(o -> o.getKey(), o -> o.getValue()));
    }

    /**
     * Maps all ocluder entities to their brushes in form of a {@code Map}
     *
     * @return A {@code Map} where the keys represent an occluder an the values a list of brush ids
     */
    private Map<Integer, List<Integer>> createOccBrushMapping() {
        Map<Integer, List<Integer>> occBrushMapping = bsp.occluderDatas.stream()                                        // Iterate over every occluderData entry
                .map(dOccluderData -> {                                                                                 // - Map them to an entry where the key is the occluderData and the value a list of brush indexes
                    List<Integer> mappedBrushes = IntStream.range(dOccluderData.firstpoly, dOccluderData.firstpoly + dOccluderData.polycount)   // -- Iterate over every occluderface the occluder has
                            .mapToObj(i -> bsp.occluderPolyDatas.get(i))                                                // --- Map the occluderFace index to occluderFace
                            .map(dOccluderPolyData -> potentialOccluderBrushes.stream()                                 // --- Map the occluder Face to a brush that contains a identical brushside with a texture matching 'matchesOccluder': Compare it to every potential occluder
                                    .filter(dBrush -> IntStream.range(0, dBrush.numside)                                // ---- Iterate over every brushside the brush has and test if it has matching vertices and a texture that matches 'matchesOccluder'
                                            .anyMatch(i -> {
                                                Winding w = WindingFactory.fromSide(bsp, dBrush, i);

                                                if (w.size() != dOccluderPolyData.vertexcount)                          // ----- If the amount of vertices are unequal we know the cant be identical
                                                    return false;

                                                for (int j = 0; j < w.size(); j++) {                                    // ----- Test vertices against each other
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
                                            }))
                                    .findFirst().orElse(null))                                                    // ---- Find the first brush that met the requirement. If no exist return null
                            .map(dBrush -> bsp.brushes.indexOf(dBrush))                                                 // --- Map the found brush back to its index. If no brush was found -1
                            .collect(Collectors.toList());                                                              // --- Collect all brushes into a list

                    return new AbstractMap.SimpleEntry<>(bsp.occluderDatas.indexOf(dOccluderData), mappedBrushes);      // -- Return the found brushes as an entry. Key = occluderData, value = List of brush indexes
                })
                .collect(Collectors.toMap(o -> o.getKey(), o -> o.getValue()));                                         // - Collect all entries into a Map

        // Because the Texturebuilder needs to know which brush is a occluder we flag them here. (The Texturebuilder needs to know this information because the brushside that represents the occluder has almost always the wrong tooltexture applied)
        for (Map.Entry<Integer, List<Integer>> entry: occBrushMapping.entrySet()) {
            for (Integer brushID: entry.getValue()) {
                if (brushID == -1)
                    continue;

                bsp.brushes.get(brushID).flagAsOccluder(true);
            }
        }

        return occBrushMapping;
    }

    /**
     * Mapps all occluder entities in to their brushes in ascending brush index order
     *
     * @return A {@code Map} with occluder ids as keys and and a List of Integers as values
     */
    private Map<Integer, List<Integer>> mapOccBrushMappingOrdered() {
        // Get the min amount of occluder faces that can be process#
        // This should always be limited by the amount of occluderData but we test nonetheless
        long min = Math.min(occluderFaces.entrySet().stream().mapToInt(Map.Entry::getValue).sum(), bsp.occluderDatas.stream().mapToInt(occluder -> occluder.polycount).sum());

        HashMap<Integer, List<Integer>> occBrushMap = new HashMap<>();

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
            ArrayList<Integer> occBrushes = new ArrayList<>();

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
     * If this is not the case we use {@code createOccBrushMapping} to manually map the occluders to their brushes
     *
     * @return A {@code Map} where the keys represent occluder ids and values a list of brush ids
     */
    public Map<Integer, List<Integer>> getOccBrushMapping() {
        if (!config.writeOccluders)
            return Collections.EMPTY_MAP;

        if (config.occForceMapping) {
            L.info("Forced occluder method: '" + config.occMappingMode + "'");
            return config.occMappingMode.map(this);
        }

        if (occluderFaces.entrySet().stream().mapToInt(Map.Entry::getValue).sum() == bsp.occluderDatas.stream().mapToInt(occluder -> occluder.polycount).sum()) {
            L.info("Equal amount of occluder faces as occluder brush faces. Using '" + OccMappingMode.ORDERED + "' method");
            return OccMappingMode.ORDERED.map(this);
        } else {
            L.info("Unequal amount of occluder faces as occluder brush faces. Falling back to '" + OccMappingMode.MANUAL + "' method");
            return OccMappingMode.MANUAL.map(this);
        }
    }

    public enum OccMappingMode {
        MANUAL(OccluderMapper::createOccBrushMapping),
        ORDERED(OccluderMapper::mapOccBrushMappingOrdered);

        private Function<OccluderMapper, Map<Integer, List<Integer>>> mapper;

        OccMappingMode(Function<OccluderMapper, Map<Integer, List<Integer>>> mapper) {
            this.mapper = mapper;
        }

        public Map<Integer, List<Integer>> map(OccluderMapper occMapper) {
            return mapper.apply(occMapper);
        }

        @Override
        public String toString() {
            return super.toString().substring(0, 1).toUpperCase() + super.toString().substring(1).toLowerCase();
        }
    }
}
