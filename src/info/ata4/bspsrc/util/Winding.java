/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.*;
import info.ata4.bsplib.vector.Vector3f;
import java.util.*;

/**
 * Winding utility class.
 * 
 * <i>"Not wind like the air, but wind like a watch"</i>
 *
 * Original class name: unmap.Wind
 * Original author: Rof
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */

public class Winding implements List<Vector3f> {

    private static final int MAX_LEN = 56756; // sqrt(3)*32768
    private static final int MAX_COORD = 32768;
    private static final int SIDE_FRONT = 0;
    private static final int SIDE_BACK = 1;
    private static final int SIDE_ON = 2;

    // epsilon values
    private static final float EPS_SPLIT = 0.01f;
    private static final float EPS_COMP = 0.5f;
    private static final float EPS_DEGEN = 0.1f;

    // list of vectors to vertex points
    private List<Vector3f> verts;
    
    /**
     * Constructs a winding from face vertices
     *
     * @param bsp BSP data
     * @param face Face
     * @param both if true, wind in both directions
     * @return Winding for the face
     */
    public static Winding windFromFace(BspData bsp, DFace face) {
        Winding w = new Winding();

        for (int i = 0; i < face.numedge; i++) {
            int v;
            int sedge = bsp.surfEdges.get(face.fstedge + i);

            if (sedge < 0) {
                // backwards wound edge
                v = bsp.edges.get(-sedge).v[1];
            } else {
                // forwards wound edge
                v = bsp.edges.get(sedge).v[0];
            }
            
            w.verts.add(bsp.verts.get(v).point);
        }

        return w;
    }

    /**
     * Constructs a winding from a brush, for a brush side
     * 
     * Equals the brush side part of CreateBrushWindings() in brushbsp.cpp
     *
     * @param bsp BSP data
     * @param brush Brush
     * @param side Brush side ID
     * @return Winding for the brush side
     */
    public static Winding windFromSide(BspData bsp, DBrush brush, int side) {
        int ibside = brush.fstside + side;
        int iplane = bsp.brushSides.get(ibside).pnum;
        
        Winding w = windFromPlane(bsp.planes.get(iplane));

        // clip to all other planes
        for (int i = 0; i < brush.numside; i++) {
            int ibside2 = brush.fstside + i;
            int iplane2 = bsp.brushSides.get(ibside2).pnum;
            
            // don't clip plane to itself
            if (i == side) {
                continue;
            }
            
            // don't clip to bevel planes
            if (bsp.brushSides.get(ibside2).bevel) {
                continue;
            }

            // remove everything behind the plane
            DPlane plane = bsp.planes.get(iplane2);
            DPlane flipPlane = new DPlane();
            flipPlane.normal = plane.normal.scalar(-1);
            flipPlane.dist = -plane.dist;
            w.clipPlane(flipPlane, false);
        }
        
        // return the clipped winding
        return w;
    }
    
    public static Winding windFromAreaportal(BspData bsp, DAreaportal ap) {
        Winding w = new Winding();
        
        for (int i = 0; i < ap.clipPortalVerts; i++) {
            int pvi = ap.firstClipPortalVert + i;
            w.verts.add(bsp.clipPortalVerts.get(pvi).point);
        }
        
        return w;
    }

    /**
     * Constructs a winding from occluder vertices
     *
     * @param bsp BSP data
     * @param opd Occluder polygon data
     * @return Winding for the occluder
     */
    public static Winding windFromOccluder(BspData bsp, DOccluderPolyData opd) {
        Winding w = new Winding();

        for (int k = 0; k < opd.vertexcount; k++) {
            int pvi = bsp.occluderVerts.get(opd.firstvertexindex + k);
            w.verts.add(bsp.verts.get(pvi).point);
        }
        
        return w;
    }
    
    /**
     * Constructs a huge square winding from a plane
     * 
     * Equals BaseWindingForPlane() in polylib.cpp
     * 
     * @param pl plane
     */
    public static Winding windFromPlane(DPlane pl) {
        // find the dominant axis of plane normal
        float dmax = -1.0F;
        int idir = -1;

        // for each axis
        for (int i = 0; i < Vector3f.AXES; i++) {
            float dc = Math.abs(pl.normal.getAxis(i));
            // find the biggest component
            if (dc <= dmax) {
                continue;
            }
            dmax = dc;
            idir = i;
        }

        // didn't find one (null or NaN'ed vector)
        if (idir == -1) {
            throw new RuntimeException("Plane " + pl + ": bad normal");
        }

        // this will be the "upwards" pointing vector
        Vector3f vup = Vector3f.NULL;

        switch (idir) {
            case SIDE_FRONT:
            case SIDE_BACK:
                // use z unit vector
                vup = new Vector3f(0, 0, 1);
                break;
            case SIDE_ON:
                // use x unit vector
                vup = new Vector3f(1, 0, 0);
        }

        // remove the component of this vector along the normal
        float vdot = vup.dot(pl.normal);
        vup = vup.addMult(-vdot, pl.normal);
        
        // make it a unit (perpendicular)
        vup = vup.normalize();

        // the vector from origin perpendicularly touching plane
        Vector3f org = pl.normal.scalar(pl.dist);
        
        // this is the "rightwards" pointing vector
        Vector3f vrt = vup.cross(pl.normal);

        vup = vup.scalar(MAX_LEN);
        vrt = vrt.scalar(MAX_LEN);

        Winding w = new Winding();
        
        // move diagonally away from org to create the corner verts
        w.verts.add(org.sub(vrt).add(vup)); // left up
        w.verts.add(org.add(vrt).add(vup)); // right up
        w.verts.add(org.add(vrt).sub(vup)); // right down
        w.verts.add(org.sub(vrt).sub(vup)); // left down
        
        return w;
    }
    
    public Winding() {
        this.verts = new ArrayList<Vector3f>();
    }
    
    public Winding(Winding that) {
        this.verts = new ArrayList<Vector3f>(that.verts);
    }

    /**
     * Returns true if the winding still has one of the points
     * from basewinding for plane.
     * 
     * Equals WindingIsHuge() from brushbsp.cpp
     * 
     * @return true if winding is huge
     */
    public boolean isHuge() {
        for (Vector3f point : this) {
            for (int i = 0; i < Vector3f.AXES; i++) {
                if (Math.abs(point.getAxis(i)) > MAX_COORD) {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * Clips this winding to a plane defined by a normal and distance, removing
     * all vertices in front or behind it.
     * 
     * Equals ClipWindingEpsilon() in polylib.cpp
     * 
     * @param normal plane normal
     * @param dist plane distance to origin
     * @param eps clipping epsilon
     * @param back keep vertices behind the plane?
     */
    public void clipEpsilon(Vector3f normal, float dist, float eps, boolean back) {        
        // counts number of front, back and on vertices
        int[] counts = new int[] {0, 0, 0};
        int size = verts.size();
        float[] dists = new float[size + 1];
        int[] sides = new int[size + 1];

        // determine sides for each point
        for (int i = 0; i < size; i++) {
            // distance along norm-dirn from origin to vertex
            float dot = verts.get(i).dot(normal);
            
            // distance along norm-dirn from clip plane to vertex
            dot -= dist;
            
            // store it
            dists[i] = dot;
            
            if (dot > eps) {
                // vertex in front of plane
                sides[i] = SIDE_FRONT;
            } else if (dot < -eps) {
                // vertex behind plane
                sides[i] = SIDE_BACK;
            } else {
                // vertex on plane (within epsilon)
                sides[i] = SIDE_ON;
            }

            // count relative vertex positions
            counts[sides[i]]++;
        }

        sides[size] = sides[0]; // loop around to 0'th
        dists[size] = dists[0];

        if (counts[SIDE_FRONT] == 0) {
            // no vertices in front - all behind clip plane
            if (!back) {
                verts.clear();
            }
            return;
        }
        if (counts[SIDE_BACK] == 0) {
            // no vertices in back - all in front of clip plane
            if (back) {
                verts.clear();
            }
            return;
        }
        
        List<Vector3f> vertsNew = new ArrayList<Vector3f>();

        for (int i = 0; i < size; i++) {
            // get i'th vertex
            Vector3f p1 = verts.get(i);
            
            if (sides[i] == SIDE_ON) {
                vertsNew.add(p1);
                continue;
            }
            
            if (sides[i] == SIDE_FRONT && !back) {
                // add copy the current vertex
                vertsNew.add(p1);
            }
            
            if (sides[i] == SIDE_BACK && back) {
                // add copy the current vertex
                vertsNew.add(p1);
            }

            if (sides[i + 1] == SIDE_ON) {
                // next vertex is on the plane, so go to next vertex stat
                continue;
            }
            
            if (sides[i + 1] == sides[i]) {
                // next vertex does not change side, so go to next vertex stat
                continue;
            }
            
            // otherwise, we are crossing the clip plane between this vertex and the next
            // so generate a split point
            
            // will contain the next vertex position
            Vector3f p2;

            if (i == size - 1) {
                // we're the last vertex in the winding
                // next vertex is the 0'th one
                p2 = verts.get(0);
            } else {
                // else get the next vertex
                p2 = verts.get(i + 1);
            }

            // dot is fractional position of clip plane between
            // this vertex and the next
            float dot = dists[i] / (dists[i] - dists[i + 1]);
            
            // vector of the split vertex
            Vector3f mv = Vector3f.NULL;

            for (int j = 0; j < Vector3f.AXES; j++) {
                // avoid round off error when possible
                if (normal.getAxis(j) == 1.0f) {
                    mv = mv.setAxis(j, dist);
                } else if (normal.getAxis(j) == -1.0f) {
                    mv = mv.setAxis(j, -dist);
                } else {
                    // check it! MSH
                    mv = mv.setAxis(j, p1.getAxis(j) + dot * (p2.getAxis(j) - p1.getAxis(j)));
                }
            }

            // write the output vertex
            vertsNew.add(mv);
        }
        
        // update vertex list
        this.verts = vertsNew;
    }

    /**
     * Clips this winding to a plane and removes all vertices behind or in front
     * of it.
     * 
     * @param pl plane to clip to
     * @param back keep vertices behind the plane?
     */
    public void clipPlane(DPlane pl, boolean back) {
        clipEpsilon(pl.normal, pl.dist, EPS_SPLIT, back);
    }

    /**
     * Compare two windings, taking into account that start points may not match
     * 
     * @param that other winding
     * @return true if it matches this winding
     */
    public boolean matches(Winding that) {
        final int size = verts.size();
        
        // if windings have different number of points, trivially fail
        if (size != that.verts.size()) {
            return false;
        }
        
        // minimum match distance
        float min = 1e6f;

        for (int i = 0; i < size; i++) {
            float mdist = 0.0f;
            
            // get the aggregate distance at offset i
            for (int j = 0; j < size; j++) {
                // wrap index if greater than size
                int k = (j + i) % size;
                
                // distance between vertex j of this and k of that
                mdist += verts.get(j).sub(that.verts.get(k)).length();
            }

            // update minimum match distance
            min = Math.min(min, mdist);
        }

        // check if match was close enough
        return min < EPS_COMP;
    }
    
    /**
     * Checks if a point is inside this winding.
     * 
     * @param pt point to test
     * @return true if the point lies inside this winding
     */
    public boolean isInside(Vector3f pt) {
        if (isEmpty() || size() < 2) {
            // "Is not possible!"
            return false;
        }
        
        // get the first normal to test
        Vector3f toPt = pt.sub(get(0));
        Vector3f edge = get(1).sub(get(0));
        Vector3f testCross = edge.cross(toPt).normalize();
        Vector3f cross;
        
        int size = size();
        
        for (int i = 1; i < size; i++) {
            toPt = pt.sub(get(i));
            edge = get((i + 1) % size).sub(get(i));
            cross = edge.cross(toPt).normalize();
            
            if (cross.dot(testCross) < 0) {
                return false;
            }
        }
        
        return true;
    }
    
    public Vector3f[] getBounds() {
        final float M = Float.MAX_VALUE;
        
        Vector3f mins = new Vector3f(M, M, M);
        Vector3f maxs = new Vector3f(-M, -M, -M);
        
        for (Vector3f vert : verts) {
            mins = mins.min(vert);
            maxs = maxs.max(vert);
        }
        
        return new Vector3f[] {mins, maxs};
    }
    
    public Vector3f getSize() {
        Vector3f[] bounds = getBounds();
        return bounds[1].sub(bounds[0]);
    }
        
    
    /**
     * Returns the center point (barycenter) of this winding.
     * 
     * Equals WindingCenter() in polylib.cpp
     * 
     * @return 
     */
    public Vector3f getCenter() {
        Vector3f sum = Vector3f.NULL;
        
        // add all verts
        for (Vector3f vert : verts) {
            sum = sum.add(vert);
        }
        
        // average vertex position
        return sum.scalar(1f / verts.size());
    }
    
    /**
     * Returns the plane points of this winding in form of a triangle.
     * 
     * @return Vector3f array with three points of the triangle
     */
    public Vector3f[] getVertexPlane() {
        Vector3f[] vertsNew = new Vector3f[verts.size()];
        Vector3f[] plane = new Vector3f[3];
        
        // 1st vert is always base vertex
        plane[0] = get(0);

        // build vector list
        for (int i = 0; i < vertsNew.length; i++) {
            // the vector from start vertex to i'th
            vertsNew[i] = get(i).sub(plane[0]);
        }
        
        // the largest modulus of cross product found between ixj
        float maxmcp = -1;
        // the i index of largest cp
        int imax = -1;
        // the j index of largest cp
        int jmax = -1;

        // loop through all i x j combinations
        for (int i = 1; i < vertsNew.length; i++) {
            // ensures j>i
            for (int j = i + 1; j < vertsNew.length; j++) {
                float mcp = vertsNew[i].cross(vertsNew[j]).length();
                if (mcp > maxmcp) {
                    maxmcp = mcp;
                    imax = i;
                    jmax = j;
                }
            }
        }
        
        // choose other two such that cross product is maximum
        plane[1] = get(imax);
        plane[2] = get(jmax);
        
        return plane;
    }

    /**
     * Removes degenerated(?) vertices from this winding.
     * 
     * @return number of removed vertices
     */
    public int removeDegenerated() {
        if (verts.isEmpty()) {
            return 0;
        }
        
        ArrayList<Vector3f> vertsNew = new ArrayList<Vector3f>();
        
        int size = verts.size();

        for (int i = 0; i < size; i++) {
            int j = (i + 1) % size;
            Vector3f v0 = verts.get(i);
            Vector3f v1 = verts.get(j);

            if (v0.sub(v1).length() > EPS_DEGEN) {
                vertsNew.add(v0);
            }
        }
        
        int removed = verts.size() - vertsNew.size();

        if (removed != 0) {
            verts = vertsNew;
        }
        
        return removed;
    }
    
    /**
     * Removes collinear vertices from this winding.
     * 
     * @return number of removed vertices
     */
    public int removeCollinear() {
        if (verts.isEmpty()) {
            return 0;
        }
        
        ArrayList<Vector3f> vertsNew = new ArrayList<Vector3f>();
        
        int size = verts.size();

        for (int i = 0; i < size; i++) {
            int j = (i + 1) % size;
            int k = (i + size - 1) % size;
            Vector3f v1 = verts.get(j).sub(verts.get(i)).normalize();
            Vector3f v2 = verts.get(i).sub(verts.get(k)).normalize();

            if (v1.dot(v2) < 0.999) {
                vertsNew.add(verts.get(i));
            }
        }
        
        int removed = size - vertsNew.size();

        if (removed != 0) {
            verts = vertsNew;
        }
        
        return removed;
    }
    
    /**
     * Returns the total area of this winding.
     * 
     * @return total area
     */
    public float getArea() {
        float total = 0;
        int size = verts.size();

        for (int i = 2; i < size; i++) {
            Vector3f v1 = verts.get(i - 1).sub(verts.get(0));
            Vector3f v2 = verts.get(i).sub(verts.get(0));
            total += v1.cross(v2).length();
        }
        
        return total * 0.5f;
    }

    /**
     * Rotates all vertices in this winding by the given euler angles.
     * 
     * @param angles rotation angles
     */
    public void rotate(Vector3f angles) {
        if (verts.isEmpty()) {
            return;
        }

        ArrayList<Vector3f> vNew = new ArrayList<Vector3f>();
        
        for (Vector3f vert : verts) {
            vNew.add(vert.rotate(angles));
        }

        verts = vNew;
    }
    
    public void translate(Vector3f offset) {
        if (verts.isEmpty()) {
            return;
        }
        
        ArrayList<Vector3f> vNew = new ArrayList<Vector3f>();
        
        for (Vector3f vert : verts) {
            vNew.add(vert.add(offset));
        }

        verts = vNew;
    }
    
    public void addBackface() {
        List<Vector3f> vertsNew = new ArrayList<Vector3f>();
        
        int size = verts.size();
        
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                vertsNew.add(verts.get(i));
            }
            if (i != size) {
                vertsNew.add(verts.get(i));
            }
        }
        
        vertsNew.add(verts.get(0));
        
        verts = vertsNew;
    }
    
    public int size() {
        return verts.size();
    }

    public boolean isEmpty() {
        return verts.isEmpty();
    }

    public boolean contains(Object o) {
        return verts.contains(o);
    }

    public Iterator<Vector3f> iterator() {
        return verts.iterator();
    }

    public Object[] toArray() {
        return verts.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return verts.toArray(a);
    }

    public boolean add(Vector3f e) {
        return verts.add(e);
    }

    public boolean remove(Object o) {
        return verts.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return verts.containsAll(c);
    }

    public boolean addAll(Collection<? extends Vector3f> c) {
        return verts.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends Vector3f> c) {
        return verts.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return verts.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return verts.retainAll(c);
    }

    public void clear() {
        verts.clear();
    }

    public Vector3f get(int index) {
        return verts.get(index);
    }

    public Vector3f set(int index, Vector3f element) {
        int size = verts.size();

        if (size == index) {
            verts.add(element);
            return verts.get(index);
        } else if (size > index) {
            return verts.set(index, element);
        } else {
            throw new IllegalArgumentException("Vertex " + element + ": index "
                    + index + " > size " + size);
        }
    }

    public void add(int index, Vector3f element) {
        verts.add(index, element);
    }

    public Vector3f remove(int index) {
        return verts.remove(index);
    }

    public int indexOf(Object o) {
        return verts.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return verts.lastIndexOf(o);
    }

    public ListIterator<Vector3f> listIterator() {
        return verts.listIterator();
    }

    public ListIterator<Vector3f> listIterator(int index) {
        return verts.listIterator(index);
    }

    public List<Vector3f> subList(int fromIndex, int toIndex) {
        return verts.subList(fromIndex, toIndex);
    }
    
    @Override
    public String toString() {
        return verts.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Winding other = (Winding) obj;
        if (this.verts != other.verts && (this.verts == null || !this.verts.equals(other.verts))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.verts != null ? this.verts.hashCode() : 0);
        return hash;
    }
}
