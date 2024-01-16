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
 * Leaf data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DLeaf implements DStruct {

    public int contents;
    public int cluster;
    public int areaFlags;
    public Vector3f mins;
    public Vector3f maxs;
    public int fstleafface;
    public int numleafface;
    public int fstleafbrush;
    public int numleafbrush;
    public int leafWaterDataID;

    @Override
    public int getSize() {
        return 30;
    }

    @Override
    public void read(DataReader in) throws IOException {
        contents = in.readInt();
        cluster = in.readShort();
        areaFlags = in.readShort();
        mins = new Vector3f(in.readShort(), in.readShort(), in.readShort());        
        maxs = new Vector3f(in.readShort(), in.readShort(), in.readShort());




        fstleafface = in.readUnsignedShort();
        numleafface = in.readUnsignedShort();
        fstleafbrush = in.readUnsignedShort();
        numleafbrush = in.readUnsignedShort();
        leafWaterDataID = in.readShort();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(contents);
        out.writeShort((short)cluster);
        out.writeShort((short)areaFlags);
        out.writeShort((short)mins.x);
        out.writeShort((short)mins.y);
        out.writeShort((short)mins.z);
        out.writeShort((short)maxs.x);
        out.writeShort((short)maxs.y);
        out.writeShort((short)maxs.z);
        out.writeUnsignedShort(fstleafface);
        out.writeUnsignedShort(numleafface);
        out.writeUnsignedShort(fstleafbrush);
        out.writeUnsignedShort(numleafbrush);
        out.writeShort((short)leafWaterDataID);
    }
}
