/*
 ** 2011 September 12
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package com.nuclearvelocity.barracuda.bsplib.vector;

/**
 * An immutable three-dimensional vector class for double values.
 * 
 * Mostly copy-pasta of Vector3f. C'est la vie.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class Vector3d {

    // frequently used pre-defined vectors
    public static final Vector3d NULL = new Vector3d(0, 0, 0);
    public static final Vector3d INVALID = new Vector3d(Double.NaN, Double.NaN, Double.NaN);
    public static final Vector3d MAX_VALUE = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
    public static final Vector3d MIN_VALUE = MAX_VALUE.scalar(-1); // don't use Double.MIN_VALUE here
    
    // number of axes, since no arrays/collections are used
    public static final int AXES = 3;
    
    // vector values
    public final double x;
    public final double y;
    public final double z;

    /**
     * Default constructor, equivalent to Vector3d(0, 0, 0).
     * 
     * @deprecated Use Vector3d.NULL instead
     */
    public Vector3d() {
        this(0, 0, 0);
    }

    /**
     * Replicating contructor
     * Constructs new Vector3d by copying an existing Vector3d
     * 
     * @param v The Vector3d to copy
     */
    public Vector3d(Vector3d v) {
        this(v.x, v.y, v.z);
    }
    
    /**
     * Replicating contructor
     * Constructs new Vector3d by copying an existing Vector3f
     * 
     * @param v The Vector3d to copy
     */
    public Vector3d(Vector3f v) {
        this(v.x, v.y, v.z);
    }
    
    /**
     * Constructs a new Vector3d using the values out of an array.
     * The array must have a size of at least 3.
     * 
     * @param v double array
     */
    public Vector3d(double[] v) {
        this(v[0], v[1], v[2]);
    }
    
    /**
     * Constructs a new Vector3d using the values out of an array.
     * The array must have a size of at least 3.
     * 
     * @param v float array
     */
    public Vector3d(float[] v) {
        this(v[0], v[1], v[2]);
    }

    /**
     * Constructs a new Vector3d from x, y and z components
     * 
     * @param x the vector x component
     * @param y the vector y component
     * @param z the vector z component
     */
    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Returns the value of the n'th axis.
     * 
     * @param axis axis number
     * @return axis value
     */
    public double getAxis(int axis) {
        switch (axis) {
            case 0:
                return this.x;
            case 1:
                return this.y;
            case 2:
                return this.z;
            default:
                return 0;
        }
    }

    /**
     * Set the value of the n'th axis.
     * 
     * @param axis axis number
     * @param value new axis value
     * @return vector with new value
     */
    public Vector3d setAxis(int axis, double value) {
        switch (axis) {
            case 0:
                return new Vector3d(value, y, z);
            case 1:
                return new Vector3d(x, value, z);
            case 2:
                return new Vector3d(x, y, value);
            default:
                return this;
        }
    }

    /**
     * Vector dot product: this . that
     * 
     * @param that the Vector3d to take dot product with
     * @return the dot product of the two vectors
     */
    public double dot(Vector3d that) {
        return this.x * that.x + this.y * that.y + this.z * that.z;
    }

    /**
     * Vector cross product: this x that
     * 
     * @param that the vector to take a cross product
     * @return the cross-product vector
     */
    public Vector3d cross(Vector3d that) {
        double rx = this.y * that.z - this.z * that.y;
        double ry = this.z * that.x - this.x * that.z;
        double rz = this.x * that.y - this.y * that.x;

        return new Vector3d(rx, ry, rz);
    }

    /**
     * Vector normalisation: ^this
     * 
     * @return the normalised vector
     */
    public Vector3d normalize() {
        double len = length();
        double rx = x / len;
        double ry = y / len;
        double rz = z / len;

        return new Vector3d(rx, ry, rz);
    }

    /**
     * Vector addition: this + that
     * 
     * @param that The vector to add
     * @return The sum of the two vectors
     */
    public Vector3d add(Vector3d that) {
        double rx = this.x + that.x;
        double ry = this.y + that.y;
        double rz = this.z + that.z;

        return new Vector3d(rx, ry, rz);
    }

    /**
     * Vector subtraction: this - that
     * 
     * @param that The vector to subtract
     * @return The difference of the two vectors
     */
    public Vector3d sub(Vector3d that) {
        double rx = this.x - that.x;
        double ry = this.y - that.y;
        double rz = this.z - that.z;
        
        return new Vector3d(rx, ry, rz);
    }

    /**
     * Snap vector to nearest value: round(this / value) * value
     * 
     * @param value snap value
     * @return This vector snapped to the nearest values of 'value'
     */
    public Vector3d snap(double value) {
        double rx = Math.round(x / value) * value;
        double ry = Math.round(y / value) * value;
        double rz = Math.round(z / value) * value;

        return new Vector3d(rx, ry, rz);
    }

    /**
     * Calculate the length of this vector
     * 
     * @return length of this vector
     */
    public double length() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
    }

    /**
     * Performs a scalar multiplication on this vector: this * mul
     * 
     * @param mul multiplicator
     * @return scalar multiplied vector
     */
    public Vector3d scalar(double mul) {
        double rx = this.x * mul;
        double ry = this.y * mul;
        double rz = this.z * mul;

        return new Vector3d(rx, ry, rz);
    }

    /**
     * Vector multiplication-addition: this + scale*that
     * 
     * @param scale
     * @param that
     * @return 
     */
    public Vector3d addMult(double scale, Vector3d that) {
        // same as this.add(that.scalar(scale)) with just one new instance?
        double rx = this.x + scale * that.x;
        double ry = this.y + scale * that.y;
        double rz = this.z + scale * that.z;

        return new Vector3d(rx, ry, rz);
    }

    /**
     * Rotates the vector.
     * 
     * @param angles angles vector in degrees
     * @return rotated vector
     */
    public Vector3d rotate(Vector3d angles) {
        if (angles.x == 0 && angles.y == 0 && angles.z == 0) {
            // nothing to do
            return this;
        }

        double rx = x;
        double ry = y;
        double rz = z;

        // rotate x
        if (angles.x != 0) {
            Point2d p = new Point2d(ry, rz).rotate(angles.x);
            ry = p.x;
            rz = p.y;
        }

        // rotate y
        if (angles.y != 0) {
            Point2d p = new Point2d(rx, rz).rotate(angles.y);
            rx = p.x;
            rz = p.y;
        }

        // rotate z
        if (angles.z != 0) {
            Point2d p = new Point2d(rx, ry).rotate(angles.z);
            rx = p.x;
            ry = p.y;
        }

        return new Vector3d(rx, ry, rz);
    }
    
    /**
     * Returns the minima between this vector and another vector.
     * 
     * @param that other vector to compare
     * @return minima of this and that vector
     */
    public Vector3d min(Vector3d that) {
        double rx = Math.min(this.x, that.x);
        double ry = Math.min(this.y, that.y);
        double rz = Math.min(this.z, that.z);

        return new Vector3d(rx, ry, rz);
    }

    /**
     * Returns the maxima between this vector and another vector.
     * 
     * @param that other vector to compare
     * @return maxima of this and that vector
     */
    public Vector3d max(Vector3d that) {
        double rx = Math.max(this.x, that.x);
        double ry = Math.max(this.y, that.y);
        double rz = Math.max(this.z, that.z);

        return new Vector3d(rx, ry, rz);
    }
    
    /**
     * Checks if the vector has NaN values.
     * 
     * @return true if one value is NaN
     */
    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }

    /**
     * Checks if the vector has infinite values.
     * 
     * @return true if one value is infinite
     */
    public boolean isInfinite() {
        return Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z);
    }
    
    /**
     * Checks if the vector has NaN or infinite values.
     * 
     * @return true if one value is NaN or infinite
     */
    public boolean isValid() {
        return !isNaN() && !isInfinite();
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Vector3d)) {
            return false;
        }

        Vector3d that = (Vector3d) obj;

        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }
}
