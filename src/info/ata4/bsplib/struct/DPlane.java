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

    public int getSize() {
        return 20;
    }

    public void read(LumpDataInput li) throws IOException {
        normal = li.readVector3f();
        dist = li.readFloat();
        type = li.readInt();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(normal);
        lo.writeFloat(dist);
        lo.writeInt(type);
    }
}
