package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DAreaportal;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.struct.DVertex;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class for mapping areaportal entities to their original brushes
 * Accounts for vBsp optimization
 */
public class AreaportalMapper {

    private static final Logger L = LogUtils.getLogger();

    private BspData bsp;

    private ArrayList<AreaportalHelper> areaportalHelpers = new ArrayList<>();
    private ArrayList<DBrush> areaportalBrushes = new ArrayList<>();

    public AreaportalMapper(BspData bsp) {
        this.bsp = bsp;

        if (checkAreaportal())
            throw new RuntimeException("gsewaol IHUBwsgeroihuzgqofgh");

        prepareApHelpers();
        prepareApBrushes();
    }

    private boolean checkAreaportal()
    {
        return bsp.areaportals.stream().allMatch(dAreaportal -> dAreaportal.clipPortalVerts == 0);
    }

    /**
     * Fills {@code areaportalHelpers} with Objects that represent every Areaportal entity
     * @param bsp {@code BspData} object which contains brushes/areaportals
     */
    private void prepareApHelpers() {

        // Skip everything if no areaportals exist
        if (bsp.areaportals.isEmpty()) {
            return;
        }

        bsp.areaportals.stream()
                .filter(dAreaportal -> dAreaportal.portalKey != 0)                                                      // Ignore first areaportal because it doesn't seem important...      And i don't have any idea what it should even represent
                .forEach(dAreaportal -> {
                    boolean exist = areaportalHelpers.stream()                                                          // Do we already have a 'AreaportalHelper' that represents this areaportal?
                            .anyMatch(areaportalHelper -> areaportalHelper.portalID.contains((int) dAreaportal.portalKey));

                    if (!exist) {                                                                                       // If not we create one
                        AreaportalHelper apHelper = new AreaportalHelper();                                            // Save the portal id
                        ArrayList<Vector3f> vertices = IntStream.range(0, dAreaportal.clipPortalVerts)
                                .mapToObj(i -> bsp.clipPortalVerts.get(dAreaportal.firstClipPortalVert + i).point)
                                .collect(Collectors.toCollection(ArrayList::new));
                        apHelper.vertices = vertices.toArray(new Vector3f[]{});                                         // Save all vertices

                        Set<Integer> portalIDs = new HashSet<>();
                        portalIDs.add((int) dAreaportal.portalKey);
                        ArrayList<Integer> ids = bsp.areaportals.stream()                                  // Find all other areaportals that share the same space with this one and add their ids to this helper object
                                .filter(otherDAreaportal -> otherDAreaportal.portalKey != 0)                            // Find all other areaportals that share the same space with this one and add their ids to this helper object
                                .filter(otherDAreaportal -> otherDAreaportal != dAreaportal)                           // -We don't want to compare the areaportal to itself, so we filter it out here
                                .filter(otherDAreaportal -> IntStream.range(0, dAreaportal.clipPortalVerts)               // Check if all vertices are the same -> sharing the same space
                                    .allMatch(i -> apHelper.vertices[i].equals(bsp.clipPortalVerts.get(otherDAreaportal.firstClipPortalVert + i).point)))
                                .map(dupAp -> (int) dupAp.portalKey)                                                      // Convert the areaportal objects into their ids so we can add them easier
                                .collect(Collectors.toCollection(ArrayList::new));

                        portalIDs.addAll(ids);                                                                                                // Add all areaportal ids we found
                        apHelper.portalID.addAll(portalIDs);
                        areaportalHelpers.add(apHelper);                                                                // Finally add the helper into our list
                    }
                });
    }

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

        ArrayList<DBrush> areaportalBrushes = new ArrayList<>(this.areaportalBrushes);

        Map<Integer, Integer> apHelperBrushMapping = areaportalHelpers.stream()
                .map(apHelper -> {
                    Winding apWinding = new Winding(apHelper.vertices);
                    Vector3f[] plane = apWinding.buildPlane();
                    Vector3f vec1 = plane[1].sub(plane[0]);
                    Vector3f vec2 = plane[2].sub(plane[0]);
                    Vector3f normal = vec2.cross(vec1);
                    float dist = normal.normalize().dot(new Vector3f(0f, 0f, 0f).sub(plane[0]));

                    List<DBrush> brushes = areaportalBrushes.stream()
                            .map(dBrush -> {
                                Winding w = IntStream.range(0, dBrush.numside)
                                        .mapToObj(i -> WindingFactory.fromSide(bsp, dBrush, i))
                                        .filter(winding -> winding.size() > 2)
                                        .filter(winding -> winding.intersect(apWinding))
                                        .findFirst().orElse(null);

                                return w == null ? null : dBrush;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return new AbstractMap.SimpleEntry<>(apHelper, brushes);
                })
                .filter(entry -> entry.getValue().size() >= entry.getKey().portalID.size())
                .map(entry -> {
                    AreaportalHelper apHelpers = entry.getKey();
                    List<DBrush> brushes = entry.getValue();

                    return IntStream.range(0, apHelpers.portalID.size())
                            .mapToObj(i -> {
                                areaportalBrushes.remove(brushes.get(i));
                                return new AbstractMap.SimpleEntry<Integer, Integer>(apHelpers.portalID.get(i), bsp.brushes.indexOf(brushes.get(i)));
                            })
                            .collect(Collectors.toMap(o -> o.getKey(), o -> o.getValue()));
                })
                .collect(HashMap::new, HashMap::putAll, HashMap::putAll);

        return Collections.unmodifiableMap(apHelperBrushMapping);
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

        IntStream.range(0, otherBrush.numside).forEach(i -> vertices.addAll(WindingFactory.fromSide(bsp, otherBrush, i)));  //Saves all vertices from 'otherBrush' into a set

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

    public static void main(String[] args)
    {
        Winding w1 = new Winding(new Vector3f[]{
                new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 1f),
                new Vector3f(1f, 1f, 0f), new Vector3f(1f, 0f , 0f)
        });
        Winding w2 = new Winding(new Vector3f[]{
                new Vector3f(0f, 1f, 0f), new Vector3f(0f, 2f, 0f),
                new Vector3f(1f, 2f, 0f), new Vector3f(1f, 1f, 0f)
        });



        System.out.println(w1.intersect(w2));
    }

    public Map<Integer,Integer> getApBrushMapping()
    {
        System.out.println(areaportalHelpers.stream().mapToInt(value -> value.portalID.size()).sum());
        System.out.println(bsp.brushes.stream().filter(DBrush::isAreaportal).count());
        if (areaportalHelpers.stream().mapToInt(value -> value.portalID.size()).sum() == bsp.brushes.stream().filter(DBrush::isAreaportal).count())
        {
            System.out.println("Using reverse order methode");
            HashMap<Integer, Integer> apBrushMap = new HashMap<>();
            bsp.brushes.stream()
                    .filter(DBrush::isAreaportal)
                    .sorted((o1, o2) -> Integer.compare(bsp.brushes.indexOf(o2), bsp.brushes.indexOf(o1)))
                    .forEach(dBrush -> {
                        apBrushMap.put(apBrushMap.size() + 1, bsp.brushes.indexOf(dBrush));
                    });

            return apBrushMap;
        } else {
            return createApBrushMapping();
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
