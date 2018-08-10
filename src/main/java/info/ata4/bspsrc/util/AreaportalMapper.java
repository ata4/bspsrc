package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DAreaportal;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.log.LogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
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

    private ArrayList<AreaportalHelper> areaportalHelpers = new ArrayList<>();
    private ArrayList<AreaportalBrush> areaportalBrushes = new ArrayList<>();

    public AreaportalMapper(BspData bsp) {
        prepareApHelpers(bsp);
        prepareApBrushes(bsp);
    }

    /**
     * Fills {@code areaportalHelpers} with Objects that represent every Areaportal entity
     * @param bsp {@code BspData} object which contains brushes/areaportals
     */
    private void prepareApHelpers(BspData bsp) {

        // Skip everything if no areaportals exist
        if (bsp.areaportals.isEmpty()) {
            return;
        }

        for (DAreaportal areaportal: bsp.areaportals) {

            // Skip first areaportal because it doesn't represent any func_areaportal entity in hammer (and maybe because I dont't have any idea what it even represents)
            if (areaportal.portalKey == 0)
                continue;

            // Check if a AreaportalHelper is already defined for that specific areaportal. We expect to always get 2 'DAreaportal's for one areaportal because it always has to intersect with 2 areas
            boolean exist = false;
            for (AreaportalHelper areaportalHelper: areaportalHelpers) {
                if (areaportalHelper.portalID == areaportal.portalKey) {
                    exist = true;

                    if (areaportal.clipPortalVerts != areaportalHelper.vertices.length)
                        L.log(Level.WARNING, "Found duplicated areaportals with different vertices count. Expected {0} found {1}", new Object[]{areaportalHelper.vertices.length, (int) areaportal.clipPortalVerts});

                    for (int i = 0; i < areaportal.clipPortalVerts; i++) {
                        Vector3f vertex = bsp.clipPortalVerts.get(areaportal.firstClipPortalVert + i).point;
                        if (!vertex.equals(areaportalHelper.vertices[i]))
                            L.log(Level.WARNING, "Found duplicated areaportals with different vertices position. Expected {0} found {1}", new Object[]{vertex, areaportalHelper.vertices[i]});
                    }

                    break;
                }
            }

            // If there is no AreaportalHelper already defined for the specific areaportal, we create it and add it to the list
            if (!exist) {
                AreaportalHelper areaportalHelper = new AreaportalHelper();
                areaportalHelper.portalID = areaportal.portalKey;

                Vector3f[] vertices = new Vector3f[areaportal.clipPortalVerts];
                for (int i = 0; i < vertices.length; i++) {
                    vertices[i] = bsp.clipPortalVerts.get(areaportal.firstClipPortalVert + i).point;
                }
                areaportalHelper.vertices = vertices;

                areaportalHelpers.add(areaportalHelper);
            }
        }

        // Now we check if any of those areaportals share the same space/are identical. This happens when vBsp optimizes multiple areaportals into 1.
        // TODO: Do more research on that as it seems odd that vBsp would just leave a duplicated areaportal in the map
        ArrayList<AreaportalHelper> cloneList = (ArrayList<AreaportalHelper>) areaportalHelpers.clone();
        ListIterator<AreaportalHelper> iterator = cloneList.listIterator();
        while (iterator.hasNext()) {                                                                // Process every areaportalhelper
            AreaportalHelper apHelper = iterator.next();
            iterator.remove();                                                                      // *Directly* remove the current element from the list so we save some loop cycles and don't compare it to it self later


            cloneList.stream()                                                                      // Compare current areaportal to all existing ones, except for itself because we removed it early
                    //.filter(apHelper2 -> apHelper2 != apHelper) // Not needed
                    .filter(apHelper2 -> apHelper.vertices.length == apHelper2.vertices.length)     // If the AreaportalHelpers have different count of vertices, they can't share the same space: filters them out
                    .filter(apHelper2 -> IntStream.range(0, apHelper.vertices.length)               // Filter all AreaportalHelpers out that don't share the same vertices: Iterates over every vertex and compares them
                            .allMatch(i -> apHelper.vertices[i].equals(apHelper2.vertices[i])))
                    .forEach(apHelper2 -> {                                                         // All remaining Areaportals area getting their duplicated ones added into the 'duplicated' list
                        apHelper.duplicated.add(apHelper2);
                        apHelper2.duplicated.add(apHelper);
                    });
        }
    }


    /**
     * Fills {@code areaportalBrushes} with Objects that represent every Areaportal brush after vBsp combined/optimized them.
     * These Objects contain info of their original brushes, so reconstruction is possible
     * @param bsp {@code BspData} object which contains brushes/areaportals
     */
    private void prepareApBrushes(BspData bsp) {

        // Create a new List with all areaportal brushes
        ArrayList<DBrush> apBrushes = bsp.brushes.stream()
                .filter(DBrush::isAreaportal)
                .collect(Collectors.toCollection(ArrayList::new));

        // Converts and groups areaportal brushes into 'areaportalBrushes'
        apBrushes.forEach(dBrush -> {                                               // Iterate over every areaportal brush
            boolean exist = areaportalBrushes.stream()                              // Do we already 'AreaportalBrush' that represents this brush?
                    .anyMatch(areaportalBrush -> areaportalBrush.brushes.contains(dBrush));

            if (!exist) {                                                           // If not we create it
                AreaportalBrush apBrush = new AreaportalBrush();
                apBrush.brushes.add(dBrush);
                List<DBrush> intersectingBrushes = apBrushes.stream()               // Get all brushes that intersect with this one
                        .filter(otherBrush -> otherBrush != dBrush)                   // -We don't want to compare the brush to itself, so we filter it out here
                        .filter(otherBrush -> intersects(dBrush, otherBrush, bsp))    // -Filter all brushes out that don't intersect
                        .collect(Collectors.toList());                                // -Return as list
                apBrush.brushes.addAll(intersectingBrushes);

                areaportalBrushes.add(apBrush);                                     // Finally we add the 'AreaportalBrush' into the list
            }
        });
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

    /**
     * A little areaportal helper class for simplicity
     */
    private class AreaportalHelper {

        public int portalID;
        public List<AreaportalHelper> duplicated = new ArrayList<>();   // Array of AreaportalHelpers which are duplicated with this instance. Two AreaportalHelpers are duplicated when they share the same clipPortalVerts.
                                                                        // This happens when vBsp optimizes multiple areaportals into one
        public Vector3f[] vertices;
    }

    /**
     * A little areaportal brush helper class for simplicity
     */
    private class AreaportalBrush {
        public ArrayList<DBrush> brushes = new ArrayList<>(); // List of brushes this areaportal is made of
    }
}
