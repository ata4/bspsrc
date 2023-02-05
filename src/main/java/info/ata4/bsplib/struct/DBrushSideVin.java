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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

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
    public void read(DataReader in) throws IOException {
        pnum = in.readInt();
        texinfo = (short) in.readInt();
        dispinfo = (short) in.readInt();
        bevel = (short) in.readInt() == 1;
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(pnum);
        out.writeInt(texinfo);
        out.writeInt(dispinfo);
        out.writeInt(bevel ? 1 : 0);
    }
}
