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

import info.ata4.bsplib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 * Model data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DModel implements DStruct {

    public Vector3f mins, maxs;
    public Vector3f origin;
    public int headnode;    // the head node of the model's BSP tree
    public int fstface;
    public int numface;

    @Override
    public int getSize() {
        return 48;
    }

    @Override
    public void read(DataReader in) throws IOException {
        mins = Vector3f.read(in);
        maxs = Vector3f.read(in);
        origin = Vector3f.read(in);
        headnode = in.readInt();
        fstface = in.readInt();
        numface = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        Vector3f.write(out, mins);
        Vector3f.write(out, maxs);
        Vector3f.write(out, origin);
        out.writeInt(headnode);
        out.writeInt(fstface);
        out.writeInt(numface);
    }
}
