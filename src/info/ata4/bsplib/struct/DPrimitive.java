/*
 ** 2011 September 24
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
public class DPrimitive implements DStruct {
    
    public int type;
    public int firstIndex;
    public int indexCount;
    public int firstVert;
    public int vertCount;

    public int getSize() {
        return 10;
    }

    public void read(LumpDataInput li) throws IOException {
        type = li.readUnsignedShort();
        firstIndex = li.readUnsignedShort();
        indexCount = li.readUnsignedShort();
        firstVert = li.readUnsignedShort();
        vertCount = li.readUnsignedShort();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeShort(type);
        lo.writeShort(firstIndex);
        lo.writeShort(indexCount);
        lo.writeShort(firstVert);
        lo.writeShort(vertCount);
    }
}
