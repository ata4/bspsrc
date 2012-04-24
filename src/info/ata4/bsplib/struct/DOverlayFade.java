/*
 ** 2011 September 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.struct;

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOverlayFade implements DStruct {
    
    public float fadeDistMinSq;
    public float fadeDistMaxSq;

    public int getSize() {
        return 8;
    }

    public void read(LumpDataInput li) throws IOException {
        fadeDistMinSq = li.readFloat();
        fadeDistMaxSq = li.readFloat();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeFloat(fadeDistMinSq);
        lo.writeFloat(fadeDistMaxSq);
    }
}
