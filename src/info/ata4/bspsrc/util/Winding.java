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
    
    private static final Winding EMPTY = new Winding(Collections.unmodifiableList(new ArrayList<Vector3f>()));

    public static final int MAX_LEN = 56756; // sqrt(3)*32768
    public static final int MAX_COORD = 32768;
    public static final int SIDE_FRONT = 0;
    public static final int SIDE_BACK = 1;
    public static final int SIDE_ON = 2;

    // epsilon values
    public static final float EPS_SPLIT = 0.01f;
    public static final float EPS_COMP = 0.5f;
    public static final float EPS_DEGEN = 0.1f;
    
    // list of vectors to vertex points
    private final List<Vector3f> verts;
    
    public Winding(Winding that) {
        this.verts = that.verts;
    }
    
    public Winding(List<Vector3f> verts) {
        this.verts = Collections.unmodifiableList(verts);
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
    public Winding clipEpsilon(Vector3f normal, float dist, float eps, boolean back) {        
        // counts number of front, back and on vertices
        int[] counts = new int[] {0, 0, 0};
        final int size = verts.size();
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
                return EMPTY;
            } else {
                return this;
            }
        }
        if (counts[SIDE_BACK] == 0) {
            // no vertices in back - all in front of clip plane
            if (back) {
                return EMPTY;
            } else {
                return this;
            }
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

            for (int j = 0; j < normal.size; j++) {
                // avoid round off error when possible
                if (normal.get(j) == 1) {
                    mv = mv.set(j, dist);
                } else if (normal.get(j) == -1) {
                    mv = mv.set(j, -dist);
                } else {
                    // check it! MSH
                    mv = mv.set(j, p1.get(j) + dot * (p2.get(j) - p1.get(j)));
                }
            }

            // write the output vertex
            vertsNew.add(mv);
        }
        
        return new Winding(vertsNew);
    }

    /**
     * Clips this winding to a plane and removes all vertices behind or in front
     * of it.
     * 
     * @param pl plane to clip to
     * @param back keep vertices behind the plane?
     */
    public Winding clipPlane(DPlane pl, boolean back) {
        return clipEpsilon(pl.normal, pl.dist, EPS_SPLIT, back);
    }
    
   /**
     * Removes degenerated vertices from this winding. A vertex is degenerated
     * when its distance to the previous vertex is smaller than {@link EPS_DEGEN}.
     * 
     * @return number of removed vertices
     */
    public Winding removeDegenerated() {
        if (verts.isEmpty()) {
            return this;
        }
        
        ArrayList<Vector3f> vertsNew = new ArrayList<>();
        
        final int size = verts.size();

        for (int i = 0; i < size; i++) {
            int j = (i + 1) % size;
            Vector3f v1 = verts.get(i);
            Vector3f v2 = verts.get(j);

            if (v1.sub(v2).length() > EPS_DEGEN) {
                vertsNew.add(v1);
            }
        }

        return new Winding(vertsNew);
    }
    
    /**
     * Removes collinear vertices from this winding.
     * 
     * @return number of removed vertices
     */
    public Winding removeCollinear() {
        if (verts.isEmpty()) {
            return this;
        }
        
        ArrayList<Vector3f> vertsNew = new ArrayList<>();
        
        final int size = verts.size();

        for (int i = 0; i < size; i++) {
            int j = (i + 1) % size;
            int k = (i + size - 1) % size;
            Vector3f v1 = verts.get(j).sub(verts.get(i)).normalize();
            Vector3f v2 = verts.get(i).sub(verts.get(k)).normalize();

            if (v1.dot(v2) < 0.999) {
                vertsNew.add(verts.get(i));
            }
        }
        
        return new Winding(vertsNew);
    }
    
    /**
     * Rotates all vertices in this winding by the given euler angles.
     * 
     * @param angles rotation angles
     */
    public Winding rotate(Vector3f angles) {
        if (verts.isEmpty()) {
            return this;
        }

        ArrayList<Vector3f> vertsNew = new ArrayList<>();
        
        for (Vector3f vert : verts) {
            vertsNew.add(vert.rotate(angles));
        }

        return new Winding(vertsNew);
    }
    
    public Winding translate(Vector3f offset) {
        if (verts.isEmpty()) {
            return this;
        }
        
        ArrayList<Vector3f> vertsNew = new ArrayList<>();
        
        for (Vector3f vert : verts) {
            vertsNew.add(vert.add(offset));
        }

        return new Winding(vertsNew);
    }
    
    public Winding addBackface() {
        if (verts.isEmpty()) {
            return this;
        }
        
        List<Vector3f> vertsNew = new ArrayList<>();
        
        final int size = verts.size();
        
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                vertsNew.add(verts.get(i));
            }
            if (i != size) {
                vertsNew.add(verts.get(i));
            }
        }
        
        return new Winding(vertsNew);
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
            for (float value : point) {
                if (Math.abs(value) > MAX_COORD) {
                    return true;
                }
            }
        }

        return false;
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
            float mdist = 0;
            
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
        Vector3f mins = Vector3f.MAX_VALUE;
        Vector3f maxs = Vector3f.MIN_VALUE;
        
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
    public Vector3f[] buildPlane() {
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
     * Checks if this winding contains any duplicate vertices.
     * 
     * @return true if this winding contains duplicate vertices
     */
    public boolean hasDuplicates() {
        final int size = verts.size();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    continue;
                }

                Vector3f v1 = verts.get(i);
                Vector3f v2 = verts.get(j);

                if (v1.equals(v2)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Returns the total area of this winding.
     * 
     * @return total area
     */
    public float getArea() {
        float total = 0;
        final int size = verts.size();

        for (int i = 2; i < size; i++) {
            Vector3f v1 = verts.get(i - 1).sub(verts.get(0));
            Vector3f v2 = verts.get(i).sub(verts.get(0));
            total += v1.cross(v2).length();
        }
        
        return total * 0.5f;
    }
    
    @Override
    public int size() {
        return verts.size();
    }

    @Override
    public boolean isEmpty() {
        return verts.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return verts.contains(o);
    }

    @Override
    public Iterator<Vector3f> iterator() {
        return verts.iterator();
    }

    @Override
    public Object[] toArray() {
        return verts.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return verts.toArray(a);
    }

    @Override
    public boolean add(Vector3f e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return verts.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Vector3f> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Vector3f> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector3f get(int index) {
        return verts.get(index);
    }

    @Override
    public Vector3f set(int index, Vector3f element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, Vector3f element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector3f remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return verts.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return verts.lastIndexOf(o);
    }

    @Override
    public ListIterator<Vector3f> listIterator() {
        return verts.listIterator();
    }

    @Override
    public ListIterator<Vector3f> listIterator(int index) {
        return verts.listIterator(index);
    }

    @Override
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
