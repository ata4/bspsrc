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
    public void read(LumpIO lio) throws IOException {
        planenum = lio.readInt();
        children[0] = lio.readInt();
        children[1] = lio.readInt();
        mins[0] = lio.readShort();
        mins[1] = lio.readShort();
        mins[2] = lio.readShort();
        maxs[0] = lio.readShort();
        maxs[1] = lio.readShort();
        maxs[2] = lio.readShort();
        fstface = lio.readUnsignedShort();
        numface = lio.readUnsignedShort();
        area = lio.readShort();
        lio.readShort(); // paddding
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeInt(planenum);
        lio.writeInt(children[0]);
        lio.writeInt(children[1]);
        lio.writeShort(mins[0]);
        lio.writeShort(mins[1]);
        lio.writeShort(mins[2]);
        lio.writeShort(maxs[0]);
        lio.writeShort(maxs[1]);
        lio.writeShort(maxs[2]);
        lio.writeShort(fstface);
        lio.writeShort(numface);
        lio.writeShort(area);
        lio.writeShort(0); // paddding
    }
}
