/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.struct;

/**
 * A simple class to store RGBA values.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Color32 {

    public final int r;
    public final int g;
    public final int b;
    public final int a;
    public final int rgba;

    /**
     * Creates a new RGBA value with the given colors
     *
     * @param red red value, 0-255
     * @param green green value, 0-255
     * @param blue blue value, 0-255
     * @param alpha alpha value, 0-255
     */
    public Color32(int red, int green, int blue, int alpha) {
        r = red & 0xFF;
        g = green & 0xFF;
        b = blue & 0xFF;
        a = alpha & 0xFF;
        rgba = (((((a << 8) + b) << 8) + g) << 8) + r;
    }

    /**
     * Creates a new RGBA value with the given integer
     *
     * @param value RGBA integer
     */
    public Color32(int value) {
        r = value & 0xFF;
        g = (value >> 8) & 0xFF;
        b = (value >> 16) & 0xFF;
        a = (value >> 24) & 0xFF;
        rgba = value;
    }
}
