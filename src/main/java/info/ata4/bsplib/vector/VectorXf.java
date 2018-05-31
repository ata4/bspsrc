/*
 ** 2012 August 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.vector;

import java.util.Iterator;

/**
 * Base class for immutable float vectors of a specific size.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class VectorXf implements Iterable<Float> {

    public final int size;

    public VectorXf(int SIZE) {
        this.size = SIZE;
    }

    public abstract float get(int index);

    public abstract VectorXf set(int index, float value);

    /**
     * Checks if the vector has NaN values.
     * 
     * @return true if one value is NaN
     */
    public boolean isNaN() {
        for (float value : this) {
            if (Float.isNaN(value)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the vector has infinite values.
     * 
     * @return true if one value is infinite
     */
    public boolean isInfinite() {
        for (float value : this) {
            if (Float.isInfinite(value)) {
                return true;
            }
        }

        return false;
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
    public boolean equals(Object obj) {
        if(!(obj instanceof VectorXf)) {
            return false;
        }

        VectorXf that = (VectorXf) obj;

        if (this.size != that.size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (this.get(i) != that.get(i)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = size;

        for (float value : this) {
            hash = size * size * hash + Float.floatToIntBits(value);
        }

        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("(");

        for (int i = 0; i < size; i++) {
            sb.append(get(i));
            if (i != size - 1) {
                sb.append(", ");
            }
        }

        sb.append(")");

        return sb.toString();
    }

    @Override
    public Iterator<Float> iterator() {
        return new ValueIterator();
    }

    /**
     * Private value iterator for the Iterable interface
     */
    private class ValueIterator implements Iterator<Float> {

        private int index;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public Float next() {
            return get(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Can't remove immutable vector component");
        }

    }
}
