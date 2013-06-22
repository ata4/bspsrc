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

import info.ata4.bsplib.lump.LumpInput;
import info.ata4.bsplib.lump.LumpOutput;
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

    @Override
    public int getSize() {
        return 10;
    }

    @Override
    public void read(LumpInput lio) throws IOException {
        type = lio.readUnsignedShort();
        firstIndex = lio.readUnsignedShort();
        indexCount = lio.readUnsignedShort();
        firstVert = lio.readUnsignedShort();
        vertCount = lio.readUnsignedShort();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeShort(type);
        lio.writeShort(firstIndex);
        lio.writeShort(indexCount);
        lio.writeShort(firstVert);
        lio.writeShort(vertCount);
    }
}
