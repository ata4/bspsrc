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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
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
    public void read(DataReader in) throws IOException {
        type = in.readUnsignedShort();
        firstIndex = in.readUnsignedShort();
        indexCount = in.readUnsignedShort();
        firstVert = in.readUnsignedShort();
        vertCount = in.readUnsignedShort();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedShort(type);
        out.writeUnsignedShort(firstIndex);
        out.writeUnsignedShort(indexCount);
        out.writeUnsignedShort(firstVert);
        out.writeUnsignedShort(vertCount);
    }
}
