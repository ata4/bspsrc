package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DAreaportal;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class for mapping areaportal entities to their original brushes
 * Accounts for vBsp optimization
 */
public class AreaportalMapper {

    private static final Logger L = LogUtils.getLogger();

    private BspSourceConfig config;
    private BspData bsp;

    private ArrayList<AreaportalHelper> areaportalHelpers = new ArrayList<>();
    private ArrayList<DBrush> areaportalBrushes = new ArrayList<>();

    public AreaportalMapper(BspData bsp, BspSourceConfig config) {
        this.bsp = bsp;
        this.config = config;

        if (checkAreaportal())
            L.warning("Invalid areaportals, map was probably compiled with errors! Errors should be expected");

        prepareApHelpers();
        prepareApBrushes();
    }


    /**
     * Checks if there are any invalid areaportal entities, meaning the map was compiled with errors
     * @return {@code true} if any areaportal entity is invalid, else {@code false}
     */
    private boolean checkAreaportal() {
        return bsp.areaportals.stream()
                .filter(dAreaportal -> dAreaportal.portalKey != 0)
                .anyMatch(dAreaportal -> dAreaportal.clipPortalVerts == 0);
    }

    /**
     * Group similar areaportals into {@code areaportalHelpers}, as they represent the same portal
     */
    private void prepareApHelpers() {

        for (DAreaportal dAreaportal : bsp.areaportals) {
            // Ignore first areaportal because it doesn't seem important...      And i don't have any idea what it should even represent
            if (dAreaportal.portalKey == 0)
                continue;

            // Do we already have a 'AreaportalHelper' that represents this areaportal?
            Optional<AreaportalHelper> matchingApHelper = areaportalHelpers.stream()
                    .filter(apHelper -> IntStream.range(0, apHelper.vertices.length)
                            .allMatch(i -> apHelper.vertices[i].equals(bsp.clipPortalVerts.get(dAreaportal.firstClipPortalVert + i).point)))
                    .findAny();

            // If there is no AreaportalHelper that represents this portal, we create one
            // If there is already an AreaportalHelper representing this portal, we just add the portalID to it
            if (matchingApHelper.isPresent()) {
                matchingApHelper.get().portalID.add((int) dAreaportal.portalKey);
            } else {
                AreaportalHelper areaportalHelper = new AreaportalHelper();
                areaportalHelper.vertices = bsp.clipPortalVerts.subList(dAreaportal.firstClipPortalVert, dAreaportal.firstClipPortalVert + dAreaportal.clipPortalVerts).stream()
                        .map(dVertex -> dVertex.point)
                        .toArray(Vector3f[]::new);
                areaportalHelper.portalID.add((int) dAreaportal.portalKey);
                areaportalHelpers.add(areaportalHelper);
            }
        }

        // Sort the list of areaportal helpers by their first portal id. This is not necessary but could help if the manual mapping makes mistakes
        areaportalHelpers.sort(Comparator.comparingInt(apHelper -> apHelper.portalID.first()));
    }

    /**
     * Fills {@code areaportalBrushes} with Objects that represent every areaportal brush
     */
    private void prepareApBrushes() {
        areaportalBrushes.addAll(bsp.brushes.stream()
                .filter(DBrush::isAreaportal)
                .collect(Collectors.toList()));
    }

    /**
     * Maps all areaportal entitys to their brushes in form of a {@code Map}
     *
     * @return A {@code Map} where the keys represent areaportal ids and the values brush ids
     */
    private Map<Integer, Integer> manualMapping() {
        ArrayList<DBrush> areaportalBrushes = new ArrayList<>(this.areaportalBrushes);                                  // We create a copy of this list so we can dynamically remove elements when we assign them. This way they can't be mapped to two entities

        Map<AreaportalHelper, List<DBrush>> apHelperBrushMapping = areaportalHelpers.stream()                                         // I know i maybe shouldn't have only used streams here...
                .map(apHelper -> {
                    Winding apWinding = new Winding(apHelper.vertices);                                                 // We iterate over every areaportal and create some basic information about it: normal vector, distance to world origin
                    Vector3f[] plane = apWinding.buildPlane();
                    Vector3f vec1 = plane[1].sub(plane[0]);
                    Vector3f vec2 = plane[2].sub(plane[0]);
                    Vector3f normal = vec2.cross(vec1);
                    float dist = normal.normalize().dot(new Vector3f(0f, 0f, 0f).sub(plane[0]));

                    List<DBrush> brushes = areaportalBrushes.stream()                                                   // For every areaportal we check every brush that has a face that intersects/touch the areaportal and store them into a list
                            .map(dBrush -> {                                                                            // - Find Brush with side that intersects/touches the areportal
                                Winding w = IntStream.range(0, dBrush.numside)                                          // -- Iterate over every side of this brush
                                        .mapToObj(i -> WindingFactory.fromSide(bsp, dBrush, i))                         // -- Create winding of this face
                                        .filter(winding -> winding.size() > 2)                                          // -- If the winding for whatever reason doesn't have at least 3 vertices we filter it out to prevent errors
                                        .filter(winding -> winding.intersect(apWinding))                                // -- Filter every face out that doesn't intersect/touch
                                        .findFirst().orElse(null);                                                // -- Return the first face which met the requirements or null if no element met them

                                return w == null ? null : dBrush;                                                       // - If we found a face that met the requirements we return the brush of it else null
                            })
                            .filter(Objects::nonNull)                                                                   // Filter all elements out that are null
                            .collect(Collectors.toList());                                                              // Finally we got a list of brushes that have side that intersect/touch the areaportal -> return this
                    return new AbstractMap.SimpleEntry<>(apHelper, brushes);                                            // Return an entry with the areaportal helper as key and the brushes as value
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));                                         // Collect all entries into a Map

        HashMap<Integer, List<DBrush>> portalToHelperMap = new HashMap<>();                                             // Now we reorder this map into a new one so that every key represents one areaportalId and the value the list of brushes
        for (Map.Entry<AreaportalHelper, List<DBrush>> entry: apHelperBrushMapping.entrySet()) {
            for (Integer portalID: entry.getKey().portalID) {
                portalToHelperMap.put(portalID, entry.getValue());
            }
        }

        HashMap<Integer, Integer> mapping = new HashMap<>();
        ArrayList<DBrush> usedBrushes = new ArrayList<>();
        for (int i = 0; i < portalToHelperMap.entrySet().size(); i++) {                                                 // Map every key in the map to one of the brushes in the list. When multiple brushes are available always the one with the lowest index is taken. Brushes can only be assigned to one areaportal
            if (!portalToHelperMap.containsKey(i))
                continue;

            for (DBrush dBrush: portalToHelperMap.get(i)) {
                if (usedBrushes.contains(dBrush))
                    continue;

                mapping.put(i, bsp.brushes.indexOf(dBrush));
                usedBrushes.add(dBrush);
                break;
            }
        }

        return Collections.unmodifiableMap(mapping);
    }

    private Map<Integer, Integer> orderedMapping() {
        int areaportalIDCount = areaportalHelpers.stream()
                .mapToInt(apHelper -> apHelper.portalID.size())
                .sum();

        int areaportalBrushCount = areaportalBrushes.size();

        return IntStream.range(0, Math.min(areaportalIDCount, areaportalBrushCount))
                .boxed()
                .collect(Collectors.toMap(i -> i + 1, i -> bsp.brushes.indexOf(areaportalBrushes.get(i))));
    }


    // Maybe something I will still need later. I had to change the 'Manual' method because i made some mistakes when creating the algorithm. The current method is very similar to the 'ordered' method.
    // This will hopefully change soon. So this method will probably be used later again.

//    /**
//     * Test if two brushes intersect/touch
//     *
//     * @param brush first brush
//     * @param otherBrush second brush
//     * @param bsp {@code BspData} object which contains needed geometry info
//     * @return {@code true} when the brushes intersect/touch, else {@code false}
//     */
//    private boolean intersects(DBrush brush, DBrush otherBrush, BspData bsp)
//    {
//        HashSet<Vector3f> vertices = new HashSet<>();
//
//        //Saves all vertices from 'otherBrush' into a set
//        for (int i1 = 0; i1 < otherBrush.numside; i1++) {
//            vertices.addAll(WindingFactory.fromSide(bsp, otherBrush, i1));
//        }
//
//        boolean intersects = true;                                  // Some Math magic. If the variable intersect is true at the end, we know they intersect...      But seriously i don't know how well i can explain this
//        for (int i = 0; i < brush.numside; i++) {
//            Winding w = WindingFactory.fromSide(bsp, brush, i);     // Basically what we do here is comparing each face/side of 'brush' to each vertex of the other brush.
//                                                                    // For each face we create a plane and measure the closest distance from each vertex of the other brush to that plane.
//            Vector3f[] plane = w.buildPlane();                      // Because the closest distance from a point to a plane is always a right angle we use the normal vector
//            Vector3f vec1 = plane[1].sub(plane[0]);                 // If distance is <= 0 we know that that the point lies 'behind'/in that plane
//            Vector3f vec2 = plane[2].sub(plane[0]);                 // A brush intersects/touches with another brush if every plane has at least one vertex of 'otherBrush' 'behind'/in it
//
//            Vector3f cross = vec2.cross(vec1).normalize();
//            if (vertices.stream().noneMatch(vertex -> cross.dot(vertex.sub(plane[0])) <= 0)) {
//                intersects = false;
//                break;
//            }
//        }
//
//        return intersects;
//    }

    /**
     * Creates and returns an areaportal to brush mapping.
     * <p></p>
     * If the amount of portals is equal to the amount of areaportal brushes we just map the areaportal in order to the brushes.
     * This is possible because vBsp seems to compile the areaportals in order.
     * If this is not the case we use {@code manualMapping} to manually map the areaportal brushes to areaportal entities
     *
     * @return A {@code Map} where the keys represent portal ids and values the brush ids
     */
    public Map<Integer,Integer> getApBrushMapping()
    {
        if (!config.writeAreaportals)
            return Collections.emptyMap();

        if (config.apForceMapping) {
            L.info("Forced areaportal method: '" + config.apMappingMode + "'");
            return config.apMappingMode.map(this);
        }

        if (areaportalHelpers.stream().mapToInt(value -> value.portalID.size()).sum() == bsp.brushes.stream().filter(DBrush::isAreaportal).count()) {
            L.info("Equal amount of areaporal entities as areaportal brushes. Using '" + ApMappingMode.ORDERED + "' method");
            return ApMappingMode.ORDERED.map(this);
        } else {
            L.info("Unequal amount of areaporal entities as areaportal brushes. Falling back to '" + ApMappingMode.MANUAL + "' method");
            return ApMappingMode.MANUAL.map(this);
        }
    }

    public enum ApMappingMode {
        MANUAL(AreaportalMapper::manualMapping),
        ORDERED(AreaportalMapper::orderedMapping);

        private Function<AreaportalMapper, Map<Integer, Integer>> mapper;

        ApMappingMode(Function<AreaportalMapper, Map<Integer, Integer>> mapper) {
            this.mapper = mapper;
        }

        public Map<Integer, Integer> map(AreaportalMapper apMapper) {
            return mapper.apply(apMapper);
        }

        @Override
        public String toString() {
            return super.toString().substring(0, 1).toUpperCase() + super.toString().substring(1).toLowerCase();
        }
    }

    /**
     * A little areaportal helper class for simplicity
     */
    private class AreaportalHelper {

        public TreeSet<Integer> portalID = new TreeSet<>();         //All areaportal entities assigned to this helper. All areaportals are sorted by their portalID!
        public Vector3f[] vertices;
    }
}
