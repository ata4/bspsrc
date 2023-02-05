package info.ata4.bspsrc.lib.vector;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

public class Vector2f extends VectorXf {

	public static Vector2f read(DataReader in) throws IOException {
		float x = in.readFloat();
		float y = in.readFloat();
		return new Vector2f(x, y);
	}

	public static void write(DataWriter out, Vector2f vec) throws IOException {
		out.writeFloat(vec.x);
		out.writeFloat(vec.y);
	}

	// frequently used pre-defined vectors
	public static final Vector2f NULL = new Vector2f(0, 0);
	public static final Vector2f MAX_VALUE = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
	public static final Vector2f MIN_VALUE = MAX_VALUE.scalar(-1); // don't use Float.MIN_VALUE here

	// vector values
	public final float x;
	public final float y;

	/**
	 * Replicating contructor
	 * Constructs new Vector2f by copying an existing Vector2f
	 *
	 * @param v The Vector2f to copy
	 */
	public Vector2f(Vector2f v) {
		this(v.x, v.y);
	}

	/**
	 * Constructs a new Vector3f using the values out of an array.
	 * The array must have a size of at least 2.
	 *
	 * @param v float array
	 */
	public Vector2f(float[] v) {
		this(v[0], v[1]);
	}

	/**
	 * Constructs a new Vector2f from x and y components
	 *
	 * @param x the vector x component
	 * @param y the vector y component
	 */
	public Vector2f(float x, float y) {
		super(2);
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the value of the n'th component.
	 *
	 * @param index component index
	 * @return component value
	 */
	@Override
	public float get(int index) {
		switch (index) {
			case 0:
				return this.x;
			case 1:
				return this.y;
			default:
				return 0;
		}
	}

	/**
	 * Set the value of the n'th component.
	 *
	 * @param index component index
	 * @param value new component value
	 * @return vector with new value
	 */
	@Override
	public Vector2f set(int index, float value) {
		switch (index) {
			case 0:
				return new Vector2f(value, y);
			case 1:
				return new Vector2f(x, value);
			default:
				return this;
		}
	}

	/**
	 * Vector dot product: this . that
	 *
	 * @param that the Vector2f to take dot product with
	 * @return the dot product of the two vectors
	 */
	public float dot(Vector2f that) {
		return this.x * that.x + this.y * that.y;
	}

	/**
	 * Z-Component of the cross product with the 2 vectors lying on a 3d xy-plane
	 *
	 * @param that the vector to take a cross product
	 * @return the z component of the cross-product vector
	 */
	public float cross(Vector2f that) {
		return this.x * that.y - that.x * this.y;
	}

	/**
	 * Vector normalisation: ^this
	 *
	 * @return the normalised vector
	 */
	public Vector2f normalize() {
		float len = length();
		float rx = x / len;
		float ry = y / len;

		return new Vector2f(rx, ry);
	}

	/**
	 * Vector addition: this + that
	 *
	 * @param that The vector to add
	 * @return The sum of the two vectors
	 */
	public Vector2f add(Vector2f that) {
		float rx = this.x + that.x;
		float ry = this.y + that.y;

		return new Vector2f(rx, ry);
	}

	/**
	 * Vector subtraction: this - that
	 *
	 * @param that The vector to subtract
	 * @return The difference of the two vectors
	 */
	public Vector2f sub(Vector2f that) {
		float rx = this.x - that.x;
		float ry = this.y - that.y;

		return new Vector2f(rx, ry);
	}

	/**
	 * Snap vector to nearest value: round(this / value) * value
	 *
	 * @param value snap value
	 * @return This vector snapped to the nearest values of 'value'
	 */
	public Vector2f snap(float value) {
		float rx = Math.round(x / value) * value;
		float ry = Math.round(y / value) * value;

		return new Vector2f(rx, ry);
	}

	/**
	 * Calculate the length of this vector
	 *
	 * @return length of this vector
	 */
	public float length() {
		return (float) Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
	}

	/**
	 * Performs a scalar multiplication on this vector: this * mul
	 *
	 * @param mul multiplicator
	 * @return scalar multiplied vector
	 */
	public Vector2f scalar(float mul) {
		float rx = this.x * mul;
		float ry = this.y * mul;

		return new Vector2f(rx, ry);
	}

	/**
	 * Performs a scalar multiplication on this vector: this * that
	 *
	 * @param that multiplicator vector
	 * @return scalar multiplied vector
	 */
	public Vector2f scalar(Vector2f that) {
		float rx = this.x * that.x;
		float ry = this.y * that.y;

		return new Vector2f(rx, ry);
	}

	/**
	 * Rotates the vector.
	 *
	 * @param angle angle rotation in degrees
	 * @return rotated vector
	 */
	public Vector2f rotate(float angle) {
		// normalize angle
		angle %= 360;

		// special cases
		if (angle == 0)
			return this;
		if (angle == 90)
			return new Vector2f(-y, x);
		if (angle == 180)
			return new Vector2f(-x, -y);
		if (angle == 270)
			return new Vector2f(y, -x);

		// convert degrees to radians
		double radians = Math.toRadians(angle);

		double r = Math.hypot(x, y);
		double theta = Math.atan2(y, x);

		double rx = r * Math.cos(theta + radians);
		double ry = r * Math.sin(theta + radians);

		return new Vector2f((float) rx, (float) ry);
	}

	/**
	 * Returns the minima between this vector and another vector.
	 *
	 * @param that other vector to compare
	 * @return minima of this and that vector
	 */
	public Vector2f min(Vector2f that) {
		float rx = Math.min(this.x, that.x);
		float ry = Math.min(this.y, that.y);

		return new Vector2f(rx, ry);
	}

	/**
	 * Returns the maxima between this vector and another vector.
	 *
	 * @param that other vector to compare
	 * @return maxima of this and that vector
	 */
	public Vector2f max(Vector2f that) {
		float rx = Math.max(this.x, that.x);
		float ry = Math.max(this.y, that.y);

		return new Vector2f(rx, ry);
	}
}
