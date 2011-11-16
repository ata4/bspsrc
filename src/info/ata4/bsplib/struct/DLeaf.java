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

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
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

    public int getSize() {
        return 30;
    }

    public void read(LumpDataInput li) throws IOException {
        contents = li.readInt();
        cluster = li.readShort();
        areaFlags = li.readShort();
        mins[0] = li.readShort();
        mins[1] = li.readShort();
        mins[2] = li.readShort();
        maxs[0] = li.readShort();
        maxs[1] = li.readShort();
        maxs[2] = li.readShort();
        fstleafface = li.readUnsignedShort();
        numleafface = li.readUnsignedShort();
        fstleafbrush = li.readUnsignedShort();
        numleafbrush = li.readUnsignedShort();
        leafWaterDataID = li.readShort();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(contents);
        lo.writeShort(cluster);
        lo.writeShort(areaFlags);
        lo.writeShort(mins[0]);
        lo.writeShort(mins[1]);
        lo.writeShort(mins[2]);
        lo.writeShort(maxs[0]);
        lo.writeShort(maxs[1]);
        lo.writeShort(maxs[2]);
        lo.writeShort(fstleafface);
        lo.writeShort(numleafface);
        lo.writeShort(fstleafbrush);
        lo.writeShort(numleafbrush);
        lo.writeShort(leafWaterDataID);
    }
}
