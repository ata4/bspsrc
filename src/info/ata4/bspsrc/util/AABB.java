/*
 ** 2014 June 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.util;

import info.ata4.bsplib.vector.Vector3f;

/**
 * Class for axis-aligned bounding boxes.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AABB {
    
    private Vector3f min;
    private Vector3f max;

    public AABB(Vector3f mins, Vector3f maxs) {
        this.min = mins;
        this.max = maxs;
    }
    
    public AABB() {
        this(Vector3f.MAX_VALUE, Vector3f.MIN_VALUE);
    }
    
    public Vector3f getMin() {
        return min;
    }

    public void setMin(Vector3f min) {
        this.min = min;
    }

    public Vector3f getMax() {
        return max;
    }

    public void setMax(Vector3f max) {
        this.max = max;
    }
    
    public Vector3f getSize() {
        return max.sub(min);
    }
    
    public boolean intersectsWith(AABB that) {
        return that.max.x > this.min.x && that.min.x < this.max.x &&
               that.max.y > this.min.y && that.min.y < this.max.y &&
               that.max.z > this.min.z && that.min.z < this.max.z;
    }

    public void include(AABB that) {
        min = min.min(that.min);
        max = max.max(that.max);
    }
    
    public void expand(Vector3f v) {
        min = min.sub(v);
        max = max.add(v);
    }

    @Override
    public String toString() {
        return min.toString() + " -> " + max.toString();
    }
}
