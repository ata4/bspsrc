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
import info.ata4.bsplib.vector.Vector3f;
import java.io.IOException;

/**
 * Model data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DModel implements DStruct {

    public Vector3f mins, maxs;
    public Vector3f origin;
    public int headnode;
    public int fstface;
    public int numface;

    public int getSize() {
        return 48;
    }

    public void read(LumpDataInput li) throws IOException {
        mins = li.readVector3f();
        maxs = li.readVector3f();
        origin = li.readVector3f();
        headnode = li.readInt();
        fstface = li.readInt();
        numface = li.readInt();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(mins);
        lo.writeVector3f(maxs);
        lo.writeVector3f(origin);
        lo.writeInt(headnode);
        lo.writeInt(fstface);
        lo.writeInt(numface);
    }
}
