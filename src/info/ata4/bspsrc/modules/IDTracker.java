/*
 ** 2013 May 23
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.modules;

import info.ata4.bsplib.BspFileReader;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class IDTracker extends ModuleRead {
    
    private int uid = 0;
    
    public IDTracker(BspFileReader reader) {
        super(reader);
    }

    public int getUID() {
        return uid++;
    }
}
