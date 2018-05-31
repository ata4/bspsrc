/*
 ** 2012 Januar 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.util;

import info.ata4.util.EnumConverter;

/**
 * Map source format enumeration.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum SourceFormat {
    
    AUTO("Automatic"),
    OLD("Source 2004 to 2009"),
    NEW("Source 2010 and later");
    
    private final String name;

    SourceFormat(String name) {
        this.name = name;
    }

    public static SourceFormat fromOrdinal(int index) {
        return EnumConverter.fromOrdinal(SourceFormat.class, index);
    }

    @Override
    public String toString() {
        return name;
    }
}
