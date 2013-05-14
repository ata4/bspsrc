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
 * Plane data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DPlane implements DStruct {

    public Vector3f normal;
    public float dist;
    public int type;
    
    @Override
    public String toString() {
        return "DPlane[n:" + normal + ", d:" + dist + ", t:" + type + "]";
    }

    @Override
    public int getSize() {
        return 20;
    }

    @Override
    public void read(LumpIO lio) throws IOException {
        normal = lio.readVector3f();
        dist = lio.readFloat();
        type = lio.readInt();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeVector3f(normal);
        lio.writeFloat(dist);
        lio.writeInt(type);
    }
}
