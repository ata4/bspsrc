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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
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
    public void read(DataInputReader in) throws IOException {
        type = in.readUnsignedShort();
        firstIndex = in.readUnsignedShort();
        indexCount = in.readUnsignedShort();
        firstVert = in.readUnsignedShort();
        vertCount = in.readUnsignedShort();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeShort(type);
        out.writeShort(firstIndex);
        out.writeShort(indexCount);
        out.writeShort(firstVert);
        out.writeShort(vertCount);
    }
}
