/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.vector;

/**
 * An immutable three-dimensional vector class for float values.
 * 
 * Original class name: unmap.Vec
 * Original author: Bob (Mellish?)
 * Original creation date: January 20, 2005, 7:41 PM
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class Vector3f {

    // frequently used pre-defined vectors
    public static final Vector3f NULL = new Vector3f(0, 0, 0);
    public static final Vector3f INVALID = new Vector3f(Float.NaN, Float.NaN, Float.NaN);
    public static final Vector3f MAX_VALUE = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    public static final Vector3f MIN_VALUE = MAX_VALUE.scalar(-1); // don't use Float.MIN_VALUE here
    
    // number of axes, since no arrays/collections are used
    public static final int AXES = 3;
    
    // vector values
    public final float x;
    public final float y;
    public final float z;

    /**
     * Default constructor, equivalent to Vector3f(0, 0, 0).
     * 
     * @deprecated Use Vector3f.NULL instead
     */
    public Vector3f() {
        this(0, 0, 0);
    }

    /**
     * Replicating contructor
     * Constructs new Vector3f by copying an existing Vector3f
     * 
     * @param v The Vector3f to copy
     */
    public Vector3f(Vector3f v) {
        this(v.x, v.y, v.z);
    }
    
    /**
     * Constructs a new Vector3f using the values out of an array.
     * The array must have a size of at least 3.
     * 
     * @param v float array
     */
    public Vector3f(float[] v) {
        this(v[0], v[1], v[2]);
    }

    /**
     * Constructs a new Vector3f from x, y and z components
     * 
     * @param x the vector x component
     * @param y the vector y component
     * @param z the vector z component
     */
    public Vector3f(float x, float y, float z) {
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
    public float getAxis(int axis) {
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
    public Vector3f setAxis(int axis, float value) {
        switch (axis) {
            case 0:
                return new Vector3f(value, y, z);
            case 1:
                return new Vector3f(x, value, z);
            case 2:
                return new Vector3f(x, y, value);
            default:
                return this;
        }
    }

    /**
     * Vector dot product: this . that
     * 
     * @param that the Vector3f to take dot product with
     * @return the dot product of the two vectors
     */
    public float dot(Vector3f that) {
        return this.x * that.x + this.y * that.y + this.z * that.z;
    }

    /**
     * Vector cross product: this x that
     * 
     * @param that the vector to take a cross product
     * @return the cross-product vector
     */
    public Vector3f cross(Vector3f that) {
        float rx = this.y * that.z - this.z * that.y;
        float ry = this.z * that.x - this.x * that.z;
        float rz = this.x * that.y - this.y * that.x;

        return new Vector3f(rx, ry, rz);
    }

    /**
     * Vector normalisation: ^this
     * 
     * @return the normalised vector
     */
    public Vector3f normalize() {
        float len = length();
        float rx = x / len;
        float ry = y / len;
        float rz = z / len;

        return new Vector3f(rx, ry, rz);
    }

    /**
     * Vector addition: this + that
     * 
     * @param that The vector to add
     * @return The sum of the two vectors
     */
    public Vector3f add(Vector3f that) {
        float rx = this.x + that.x;
        float ry = this.y + that.y;
        float rz = this.z + that.z;

        return new Vector3f(rx, ry, rz);
    }

    /**
     * Vector subtraction: this - that
     * 
     * @param that The vector to subtract
     * @return The difference of the two vectors
     */
    public Vector3f sub(Vector3f that) {
        float rx = this.x - that.x;
        float ry = this.y - that.y;
        float rz = this.z - that.z;
        
        return new Vector3f(rx, ry, rz);
    }

    /**
     * Snap vector to nearest value: round(this / value) * value
     * 
     * @param value snap value
     * @return This vector snapped to the nearest values of 'value'
     */
    public Vector3f snap(float value) {
        float rx = Math.round(x / value) * value;
        float ry = Math.round(y / value) * value;
        float rz = Math.round(z / value) * value;

        return new Vector3f(rx, ry, rz);
    }

    /**
     * Calculate the length of this vector
     * 
     * @return length of this vector
     */
    public float length() {
        return (float) Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
    }

    /**
     * Performs a scalar multiplication on this vector: this * mul
     * 
     * @param mul multiplicator
     * @return scalar multiplied vector
     */
    public Vector3f scalar(float mul) {
        float rx = this.x * mul;
        float ry = this.y * mul;
        float rz = this.z * mul;

        return new Vector3f(rx, ry, rz);
    }
    
    /**
     * Performs a scalar multiplication on this vector: this * that
     * 
     * @param that multiplicator vector
     * @return scalar multiplied vector
     */
    public Vector3f scalar(Vector3f that) {
        float rx = this.x * that.x;
        float ry = this.y * that.y;
        float rz = this.z * that.z;

        return new Vector3f(rx, ry, rz);
    }

    /**
     * Vector multiplication-addition: this + scale*that
     * 
     * @param scale
     * @param that
     * @return 
     */
    public Vector3f addMult(float scale, Vector3f that) {
        // same as this.add(that.scalar(scale)) with just one new instance?
        float rx = this.x + scale * that.x;
        float ry = this.y + scale * that.y;
        float rz = this.z + scale * that.z;

        return new Vector3f(rx, ry, rz);
    }

    /**
     * Rotates the vector.
     * 
     * @param angles angles for each axis in degrees
     * @return rotated vector
     */
    public Vector3f rotate(Vector3f angles) {
        if (angles.x == 0 && angles.y == 0 && angles.z == 0) {
            // nothing to do here
            return this;
        }

        double rx = x;
        double ry = y;
        double rz = z;

        // rotate x (roll)
        if (angles.x != 0) {
            Point2d p = new Point2d(ry, rz).rotate(angles.x);
            ry = p.x;
            rz = p.y;
        }

        // rotate y (pitch)
        if (angles.y != 0) {
            Point2d p = new Point2d(rx, rz).rotate(angles.y);
            rx = p.x;
            rz = p.y;
        }

        // rotate z (yaw)
        if (angles.z != 0) {
            Point2d p = new Point2d(rx, ry).rotate(angles.z);
            rx = p.x;
            ry = p.y;
        }

        return new Vector3f((float)rx, (float)ry, (float)rz);
    }
    
    /**
     * Returns the minima between this vector and another vector.
     * 
     * @param that other vector to compare
     * @return minima of this and that vector
     */
    public Vector3f min(Vector3f that) {
        float rx = Math.min(this.x, that.x);
        float ry = Math.min(this.y, that.y);
        float rz = Math.min(this.z, that.z);

        return new Vector3f(rx, ry, rz);
    }

    /**
     * Returns the maxima between this vector and another vector.
     * 
     * @param that other vector to compare
     * @return maxima of this and that vector
     */
    public Vector3f max(Vector3f that) {
        float rx = Math.max(this.x, that.x);
        float ry = Math.max(this.y, that.y);
        float rz = Math.max(this.z, that.z);

        return new Vector3f(rx, ry, rz);
    }
    
    /**
     * Checks if the vector has NaN values.
     * 
     * @return true if one value is NaN
     */
    public boolean isNaN() {
        return Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z);
    }

    /**
     * Checks if the vector has infinite values.
     * 
     * @return true if one value is infinite
     */
    public boolean isInfinite() {
        return Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z);
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
        return "(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Vector3f)) {
            return false;
        }

        Vector3f that = (Vector3f) obj;

        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Float.floatToIntBits(this.x);
        hash = 17 * hash + Float.floatToIntBits(this.y);
        hash = 17 * hash + Float.floatToIntBits(this.z);
        return hash;
    }
    
    // private helper class for rotation
    private class Point2d {

        private final double x;
        private final double y;

        private Point2d(double x, double y) {
            this.x = x;
            this.y = y;
        }

        private Point2d rotate(double angle) {
            // normalize angle
            angle %= 360;
            
            // special cases
            if (angle == 0) {
                return this;
            }
            if (angle == 90) {
                return new Point2d(-y, x);
            }
            if (angle == 180) {
                return new Point2d(-x, -y);
            }
            if (angle == 270) {
                return new Point2d(y, -x);
            }

            // convert degrees to radians
            angle = Math.toRadians(angle);

            double r = Math.hypot(x, y);
            double theta = Math.atan2(y, x);

            double rx = r * Math.cos(theta + angle);
            double ry = r * Math.sin(theta + angle);

            return new Point2d(rx, ry);
        }
    }
}
