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
    public int headnode;    // the head node of the model's BSP tree
    public int fstface;
    public int numface;

    @Override
    public int getSize() {
        return 48;
    }

    @Override
    public void read(LumpIO lio) throws IOException {
        mins = lio.readVector3f();
        maxs = lio.readVector3f();
        origin = lio.readVector3f();
        headnode = lio.readInt();
        fstface = lio.readInt();
        numface = lio.readInt();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeVector3f(mins);
        lio.writeVector3f(maxs);
        lio.writeVector3f(origin);
        lio.writeInt(headnode);
        lio.writeInt(fstface);
        lio.writeInt(numface);
    }
}
