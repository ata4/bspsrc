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

import info.ata4.bsplib.lump.LumpIO;
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
    public void read(LumpIO lio) throws IOException {
        contents = lio.readInt();
        cluster = lio.readShort();
        areaFlags = lio.readShort();
        mins[0] = lio.readShort();
        mins[1] = lio.readShort();
        mins[2] = lio.readShort();
        maxs[0] = lio.readShort();
        maxs[1] = lio.readShort();
        maxs[2] = lio.readShort();
        fstleafface = lio.readUnsignedShort();
        numleafface = lio.readUnsignedShort();
        fstleafbrush = lio.readUnsignedShort();
        numleafbrush = lio.readUnsignedShort();
        leafWaterDataID = lio.readShort();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeInt(contents);
        lio.writeShort(cluster);
        lio.writeShort(areaFlags);
        lio.writeShort(mins[0]);
        lio.writeShort(mins[1]);
        lio.writeShort(mins[2]);
        lio.writeShort(maxs[0]);
        lio.writeShort(maxs[1]);
        lio.writeShort(maxs[2]);
        lio.writeShort(fstleafface);
        lio.writeShort(numleafface);
        lio.writeShort(fstleafbrush);
        lio.writeShort(numleafbrush);
        lio.writeShort(leafWaterDataID);
    }
}
