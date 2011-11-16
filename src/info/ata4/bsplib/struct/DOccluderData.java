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

    public int getSize() {
        return 36;
    }

    public void read(LumpDataInput li) throws IOException {
        flags = li.readInt();
        firstpoly = li.readInt();
        polycount = li.readInt();
        mins = li.readVector3f();
        maxs = li.readVector3f();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(flags);
        lo.writeInt(firstpoly);
        lo.writeInt(polycount);
        lo.writeVector3f(mins);
        lo.writeVector3f(maxs);
    }
}
