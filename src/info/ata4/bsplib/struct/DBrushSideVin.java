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

import info.ata4.bsplib.lump.LumpInput;
import info.ata4.bsplib.lump.LumpOutput;
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
    public void read(LumpInput lio) throws IOException {
        pnum = lio.readInt();
        texinfo = (short) lio.readInt();
        dispinfo = (short) lio.readInt();
        bevel = (short) lio.readInt() == 1;
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeInt(pnum);
        lio.writeInt(texinfo);
        lio.writeInt(dispinfo);
        lio.writeInt(bevel ? 1 : 0);
    }
}
