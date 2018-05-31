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

    @Override
    public int getSize() {
        return 32;
    }

    @Override
    public void read(DataReader in) throws IOException {
        planenum = in.readInt();
        children[0] = in.readInt();
        children[1] = in.readInt();
        mins[0] = in.readShort();
        mins[1] = in.readShort();
        mins[2] = in.readShort();
        maxs[0] = in.readShort();
        maxs[1] = in.readShort();
        maxs[2] = in.readShort();
        fstface = in.readUnsignedShort();
        numface = in.readUnsignedShort();
        area = in.readShort();
        in.readUnsignedShort(); // paddding
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(planenum);
        out.writeInt(children[0]);
        out.writeInt(children[1]);
        out.writeShort(mins[0]);
        out.writeShort(mins[1]);
        out.writeShort(mins[2]);
        out.writeShort(maxs[0]);
        out.writeShort(maxs[1]);
        out.writeShort(maxs[2]);
        out.writeUnsignedShort(fstface);
        out.writeUnsignedShort(numface);
        out.writeShort(area);
        out.writeUnsignedShort(0); // paddding
    }
}
