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
 * Node data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DNode implements DStruct {

    public int planenum;
    public int[] children = new int[2];
    public short[] mins = new short[3];
    public short[] maxs = new short[3];
    public int fstface;
    public int numface;
    public short area;

    public int getSize() {
        return 32;
    }

    public void read(LumpDataInput li) throws IOException {
        planenum = li.readInt();
        children[0] = li.readInt();
        children[1] = li.readInt();
        mins[0] = li.readShort();
        mins[1] = li.readShort();
        mins[2] = li.readShort();
        maxs[0] = li.readShort();
        maxs[1] = li.readShort();
        maxs[2] = li.readShort();
        fstface = li.readUnsignedShort();
        numface = li.readUnsignedShort();
        area = li.readShort();
        li.readShort(); // paddding
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(planenum);
        lo.writeInt(children[0]);
        lo.writeInt(children[1]);
        lo.writeShort(mins[0]);
        lo.writeShort(mins[1]);
        lo.writeShort(mins[2]);
        lo.writeShort(maxs[0]);
        lo.writeShort(maxs[1]);
        lo.writeShort(maxs[2]);
        lo.writeShort(fstface);
        lo.writeShort(numface);
        lo.writeShort(area);
        lo.writeShort(0); // paddding
    }
}
