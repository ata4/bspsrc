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
 * An immutable two-dimensional vector class for double values.
 * Currently used for Vector3f rotation only.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class Point2d {
    
    public final double x;
    public final double y;

    public Point2d() {
        this(0, 0);
    }

    public Point2d(Point2d p) {
        this(p.x, p.y);
    }
    
    public Point2d(double[] v) {
        this(v[0], v[1]);
    }

    public Point2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y);
    }

    public boolean isInfinite() {
        return Double.isInfinite(x) || Double.isInfinite(y);
    }
    
    public boolean isInvalid() {
        return isNaN() || isInfinite();
    }

    public Point2d rotate(double angle) {
        // special cases
        if (angle == 0.0 || angle == 360.0) {
            return this;
        }
        if (angle == 90.0) {
            return new Point2d(-y, x);
        }
        if (angle == 180.0) {
            return new Point2d(-x, -y);
        }
        if (angle == 270.0) {
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

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Point2d)) {
            return false;
        }

        Point2d that = (Point2d) obj;

        return this.x == that.x && this.y == that.y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }
}

