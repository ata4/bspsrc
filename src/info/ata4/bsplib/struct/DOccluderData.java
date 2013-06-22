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

import info.ata4.bsplib.lump.LumpInput;
import info.ata4.bsplib.lump.LumpOutput;
import info.ata4.bsplib.vector.Vector3f;
import java.io.IOException;

/**
 * Occluder data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOccluderData implements DStruct {
    
    public int flags;
    public int firstpoly;
    public int polycount;
    public Vector3f mins;
    public Vector3f maxs;

    @Override
    public int getSize() {
        return 36;
    }

    @Override
    public void read(LumpInput lio) throws IOException {
        flags = lio.readInt();
        firstpoly = lio.readInt();
        polycount = lio.readInt();
        mins = lio.readVector3f();
        maxs = lio.readVector3f();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeInt(flags);
        lio.writeInt(firstpoly);
        lio.writeInt(polycount);
        lio.writeVector3f(mins);
        lio.writeVector3f(maxs);
    }
}
