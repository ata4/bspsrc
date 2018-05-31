/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.util;

import java.util.EnumSet;
import java.util.Set;

/**
 * Utility class to convert enumerations to privitives and vice versa.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EnumConverter {
    
    private EnumConverter() {
    }

    public static <E extends Enum<E>> int toInteger(Set<E> flags) {
        int mask = 0;

        for (E flag : flags) {
            mask |= 1 << flag.ordinal();
        }

        return mask;
    }

    public static <E extends Enum<E>> Set<E> fromInteger(Class<E> elementType, int mask) {
        Set<E> flags = EnumSet.noneOf(elementType);

        for (E value : elementType.getEnumConstants()) {
            if ((mask & (1 << value.ordinal())) != 0) {
                flags.add(value);
            }
        }

        return flags;
    }

    public static <E extends Enum<E>> E fromOrdinal(Class<E> elementType, int index) {
        Set<E> modes = EnumSet.allOf(elementType);

        for (E mode : modes) {
            if (mode.ordinal() == index) {
                return mode;
            }
        }

        throw new IllegalArgumentException();
    }
}
