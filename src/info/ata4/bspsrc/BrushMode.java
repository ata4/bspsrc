/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc;

import info.ata4.bsplib.util.EnumConverter;

/**
 * Enumeration for brush modes.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum BrushMode {

    BRUSHPLANES("Brushes and planes"),
    ORIGFACE("Original faces"),
    ORIGFACE_PLUS("Original plus split faces"),
    SPLITFACE("Split faces");
    
    private final String name;

    BrushMode(String name) {
        this.name = name;
    }

    public static BrushMode fromOrdinal(int index) {
        return EnumConverter.fromOrdinal(BrushMode.class, index);
    }

    @Override
    public String toString() {
        return name;
    }
}
