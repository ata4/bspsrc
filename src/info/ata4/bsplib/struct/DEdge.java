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
public class DEdge implements DStruct {
    
    public int[] v = new int[2]; // vertex numbers

    public int getSize() {
        return 4;
    }

    public void read(LumpDataInput li) throws IOException {
        v[0] = li.readUnsignedShort();
        v[1] = li.readUnsignedShort();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeShort(v[0]);
        lo.writeShort(v[1]);
    }
}
