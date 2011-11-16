/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package com.nuclearvelocity.barracuda.bsplib.vector;

/**
 * An immutable two-dimensional vector class for float values.
 * Currently used for Vector3f rotation only.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class Point2f {
    
    public final float x;
    public final float y;

    public Point2f() {
        this(0, 0);
    }

    public Point2f(Point2f p) {
        this(p.x, p.y);
    }
    
    public Point2f(float[] v) {
        this(v[0], v[1]);
    }

    public Point2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean isNaN() {
        return Float.isNaN(x) || Float.isNaN(y);
    }

    public boolean isInfinite() {
        return Float.isInfinite(x) || Float.isInfinite(y);
    }
    
    public boolean isInvalid() {
        return isNaN() || isInfinite();
    }

    public Point2f rotate(double angle) {
        // rotate with Point2d to reduce rounding errors
        return new Point2f(new Point2d(this).rotate(angle));
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Point2f)) {
            return false;
        }

        Point2f that = (Point2f) obj;

        return this.x == that.x && this.y == that.y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Float.floatToIntBits(this.x);
        hash = 79 * hash + Float.floatToIntBits(this.y);
        return hash;
    }
}
