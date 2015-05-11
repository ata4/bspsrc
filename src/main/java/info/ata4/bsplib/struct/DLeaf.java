/*
** 2011 April 5
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
 * Leaf data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DLeaf implements DStruct {

    public int contents;
    public short cluster;
    public short areaFlags;
    public short[] mins = new short[3];
    public short[] maxs = new short[3];
    public int fstleafface;
    public int numleafface;
    public int fstleafbrush;
    public int numleafbrush;
    public short leafWaterDataID;

    @Override
    public int getSize() {
        return 30;
    }

    @Override
    public void read(DataReader in) throws IOException {
        contents = in.readInt();
        cluster = in.readShort();
        areaFlags = in.readShort();
        mins[0] = in.readShort();
        mins[1] = in.readShort();
        mins[2] = in.readShort();
        maxs[0] = in.readShort();
        maxs[1] = in.readShort();
        maxs[2] = in.readShort();
        fstleafface = in.readUnsignedShort();
        numleafface = in.readUnsignedShort();
        fstleafbrush = in.readUnsignedShort();
        numleafbrush = in.readUnsignedShort();
        leafWaterDataID = in.readShort();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(contents);
        out.writeShort(cluster);
        out.writeShort(areaFlags);
        out.writeShort(mins[0]);
        out.writeShort(mins[1]);
        out.writeShort(mins[2]);
        out.writeShort(maxs[0]);
        out.writeShort(maxs[1]);
        out.writeShort(maxs[2]);
        out.writeUnsignedShort(fstleafface);
        out.writeUnsignedShort(numleafface);
        out.writeUnsignedShort(fstleafbrush);
        out.writeUnsignedShort(numleafbrush);
        out.writeShort(leafWaterDataID);
    }
}
