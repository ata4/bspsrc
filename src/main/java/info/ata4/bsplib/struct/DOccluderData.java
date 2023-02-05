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
    public void read(DataReader in) throws IOException {
        flags = in.readInt();
        firstpoly = in.readInt();
        polycount = in.readInt();
        mins = Vector3f.read(in);
        maxs = Vector3f.read(in);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(flags);
        out.writeInt(firstpoly);
        out.writeInt(polycount);
        Vector3f.write(out, mins);
        Vector3f.write(out, maxs);
    }
}
