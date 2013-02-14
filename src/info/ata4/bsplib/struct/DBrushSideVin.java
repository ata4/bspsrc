/*
 ** 2013 February 14
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
 * DBrushSide variant for Vindictus that uses integers in place of shorts.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DBrushSideVin extends DBrushSide {
    
    @Override
    public int getSize() {
        return 16;
    }

    @Override
    public void read(LumpDataInput li) throws IOException {
        pnum = li.readInt();
        texinfo = (short) li.readInt();
        dispinfo = (short) li.readInt();
        bevel = (short) li.readInt() == 1;
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(pnum);
        lo.writeInt(texinfo);
        lo.writeInt(dispinfo);
        lo.writeInt(bevel ? 1 : 0);
    }
}
