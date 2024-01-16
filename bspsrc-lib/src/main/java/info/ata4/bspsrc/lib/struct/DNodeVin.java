/*
** 2013 February 14
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
 * DNode variant for Vindictus that uses integers in place of shorts.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DNodeVin extends DNode {

    @Override
    public int getSize() {
        return 48;
    }

    @Override
    public void read(DataReader in) throws IOException {
        planenum = in.readInt();
        children[0] = in.readInt();
        children[1] = in.readInt();
        mins = new Vector3f((short)in.readInt(), (short)in.readInt(), (short)in.readInt());
        maxs = new Vector3f((short)in.readInt(), (short)in.readInt(), (short)in.readInt());
        fstface = in.readInt();
        numface = in.readInt();
        in.readInt(); // padding
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(planenum);
        out.writeInt(children[0]);
        out.writeInt(children[1]);
        out.writeInt((int)mins.x);
        out.writeInt((int)mins.y);
        out.writeInt((int)mins.z);
        out.writeInt((int)maxs.x);
        out.writeInt((int)maxs.y);
        out.writeInt((int)maxs.z);
        out.writeInt(fstface);
        out.writeInt(numface);
        out.writeInt(0); // padding
    }
}
