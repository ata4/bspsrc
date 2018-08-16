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
        return bsp.areaportals.stream().filter(dAreaportal -> dAreaportal.portalKey != 0).anyMatch(dAreaportal -> dAreaportal.clipPortalVerts == 0);
    }

    /**
     * Fills {@code areaportalHelpers} with Objects that represent every portal
     */
    private void prepareApHelpers() {

        // Skip everything if no areaportals exist
        if (bsp.areaportals.isEmpty())
            return;

        for (DAreaportal dAreaportal : bsp.areaportals) {
            if (dAreaportal.portalKey == 0)                                                                             // Ignore first areaportal because it doesn't seem important...      And i don't have any idea what it should even represent
                continue;

            boolean exist = areaportalHelpers.stream()
                    .anyMatch(areaportalHelper -> areaportalHelper.portalID.contains((int) dAreaportal.portalKey));     // Do we already have a 'AreaportalHelper' that represents this areaportal?

            if (!exist) {                                                                                               // If not we create one
                AreaportalHelper apHelper = new AreaportalHelper();
                ArrayList<Vector3f> vertices = IntStream.range(0, dAreaportal.clipPortalVerts)                          // Get all vertices
                        .mapToObj(i -> bsp.clipPortalVerts.get(dAreaportal.firstClipPortalVert + i).point)
                        .collect(Collectors.toCollection(ArrayList::new));
                apHelper.vertices = vertices.toArray(new Vector3f[]{});                                                 // Save all vertices

                Set<Integer> portalIDs = new HashSet<>();                                                               // Set of all areaportal ids that are duplicated with this
                portalIDs.add((int) dAreaportal.portalKey);                                                             // But first we add the areaportals own id

                ArrayList<Integer> ids = bsp.areaportals.stream()                                                       // Find all other areaportals that share the same space with this one and add their ids to this helper object
                        .filter(otherDAreaportal -> otherDAreaportal.portalKey != 0)                                    // - Filter first areaportal out because of reason mentioned above
                        .filter(otherDAreaportal -> otherDAreaportal != dAreaportal)                                    // - We don't want to compare the areaportal to itself, so we filter it out here
                        .filter(otherDAreaportal -> IntStream.range(0, dAreaportal.clipPortalVerts)                     // - Check if all vertices are the same -> sharing the same space
                                .allMatch(i -> apHelper.vertices[i].equals(bsp.clipPortalVerts.get(otherDAreaportal.firstClipPortalVert + i).point)))
                        .map(dupAp -> (int) dupAp.portalKey)                                                            // - Convert the areaportal objects into their ids so we can add them easier
                        .collect(Collectors.toCollection(ArrayList::new));                                              // - Return all ids as a list
                portalIDs.addAll(ids);                                                                                  // Add all areaportal ids we found. 'portalIDs' has to be a set so we don't get duplicated ids
                apHelper.portalID.addAll(portalIDs);                                                                    // Add all ids into our areaportal helper object
                areaportalHelpers.add(apHelper);                                                                        // Finally add the helper into our list
            }
        }

        // Sort all areaportal ids in all areaporal helper objects. This is important!
        for (AreaportalHelper apHelper: areaportalHelpers) {
            apHelper.portalID.sort(Integer::compareTo);
        }
        // Sort the list of areaportal helpers by their first portal id. This is not necessary but could help if the manual mapping makes mistakes
        areaportalHelpers.sort(Comparator.comparingInt(apHelper -> apHelper.portalID.get(0)));
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
     * Maps all areaportal entitys to their brushes in form of a {@code HashMap}
     *
     * @return A {@code HashMap} where the key represents an areaportal id and the value a brush id
     */
    public Map<Integer, Integer> createApBrushMapping() {

        ArrayList<DBrush> areaportalBrushes = new ArrayList<>(this.areaportalBrushes);                                  // We create a copy this list so we can dynamically remove elements when we assign them. This way they can't be mapped to two entities

        Map<Integer, Integer> apHelperBrushMapping = areaportalHelpers.stream()                                         // I know i maybe shouldn't have only used streams here...
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
                .map(entry -> {                                                                                         // Now we create a HashMap for every areaportal helper with all its areaportal entities mapped to brush ids
                    AreaportalHelper apHelpers = entry.getKey();
                    List<DBrush> brushes = entry.getValue();

                    HashMap<Integer, Integer> mapping = new HashMap<>();
                    for (int i = 0; i < apHelpers.portalID.size(); i++) {
                        areaportalBrushes.remove(brushes.get(i));
                        mapping.put(apHelpers.portalID.get(i), bsp.brushes.indexOf(brushes.get(i)));
                    }
                    return mapping;
                })
                .collect(HashMap::new, HashMap::putAll, HashMap::putAll);                                               // Finally we combine all HashMaps to one big one

        return Collections.unmodifiableMap(apHelperBrushMapping);                                                       // And return it as an unmodifiable map
    }

    /**
     * Test if two brushes intersect/touch
     *
     * @param brush first brush
     * @param otherBrush second brush
     * @param bsp {@code BspData} object which contains needed geometry info
     * @return {@code true} when the brushes intersect/touch, else {@code false}
     */
    private boolean intersects(DBrush brush, DBrush otherBrush, BspData bsp)
    {
        HashSet<Vector3f> vertices = new HashSet<>();

        //Saves all vertices from 'otherBrush' into a set
        for (int i1 = 0; i1 < otherBrush.numside; i1++) {
            vertices.addAll(WindingFactory.fromSide(bsp, otherBrush, i1));
        }

        boolean intersects = true;                                  // Some Math magic. If the variable intersect is true at the end, we know they intersect...      But seriously i don't know how well i can explain this
        for (int i = 0; i < brush.numside; i++) {
            Winding w = WindingFactory.fromSide(bsp, brush, i);     // Basically what we do here is comparing each face/side of 'brush' to each vertex of the other brush.
                                                                    // For each face we create a plane and measure the closest distance from each vertex of the other brush to that plane.
            Vector3f[] plane = w.buildPlane();                      // Because the closest distance from a point to a plane is always a right angle we use the normal vector
            Vector3f vec1 = plane[1].sub(plane[0]);                 // If distance is <= 0 we know that that the point lies 'behind'/in that plane
            Vector3f vec2 = plane[2].sub(plane[0]);                 // A brush intersects/touches with another brush if every plane has at least one vertex of 'otherBrush' 'behind'/in it

            Vector3f cross = vec2.cross(vec1).normalize();
            if (vertices.stream().noneMatch(vertex -> cross.dot(vertex.sub(plane[0])) <= 0)) {
                intersects = false;
                break;
            }
        }

        return intersects;
    }

    /**
     * Creates and returns an areaportal to brush mapping.
     * <p></p>
     * If the amount of portals is equal to the amount of areaportal brushes we just map the areaportal in order to the brushes.
     * This is possible because vBsp seems to compile the areaportals in order.
     * If this is not the case we use {@code createApBrushMapping} to manually map the areaportal brushes to areaportal entities
     *
     * @return A {@code Map} where the keys represent portal ids and values the brush ids
     */
    public Map<Integer,Integer> getApBrushMapping()
    {
        if (!config.writeAreaportals)
            return Collections.EMPTY_MAP;

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
        MANUAL(AreaportalMapper::createApBrushMapping),
        ORDERED(apMapper -> {
            BspData bsp = apMapper.bsp;
            long min = Math.min(apMapper.areaportalHelpers.stream().mapToInt(value -> value.portalID.size()).sum(), bsp.brushes.stream().filter(DBrush::isAreaportal).count());

            HashMap<Integer, Integer> apBrushMap = new HashMap<>();
            ArrayList<DBrush> apBrushes = bsp.brushes.stream().filter(DBrush::isAreaportal).collect(Collectors.toCollection(ArrayList::new));
            for (int i = 0; i < min; i++) {
                apBrushMap.put(apBrushMap.size() + 1, bsp.brushes.indexOf(apBrushes.get(i)));
            }
            return apBrushMap;
        });


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

        public ArrayList<Integer> portalID = new ArrayList<>();         //All areaportal entities assigned to this helper
        public Vector3f[] vertices;
    }
}
