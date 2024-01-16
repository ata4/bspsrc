/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.lib.struct;

import info.ata4.bspsrc.lib.vector.Vector3f;
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
    public Vector3f mins;
    public Vector3f maxs;
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
        mins = new Vector3f(in.readShort(), in.readShort(), in.readShort());        
        maxs = new Vector3f(in.readShort(), in.readShort(), in.readShort());
        fstface = in.readUnsignedShort();
        numface = in.readUnsignedShort();
        area = in.readShort();
        in.readUnsignedShort(); // padding
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(planenum);
        out.writeInt(children[0]);
        out.writeInt(children[1]);
        out.writeShort((short)mins.x);
        out.writeShort((short)mins.y);
        out.writeShort((short)mins.z);
        out.writeShort((short)maxs.x);
        out.writeShort((short)maxs.y);
        out.writeShort((short)maxs.z);
        out.writeUnsignedShort(fstface);
        out.writeUnsignedShort(numface);
        out.writeShort(area);
        out.writeUnsignedShort(0); // padding
    }
}
