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
    public void read(DataReader in) throws IOException {
        normal = Vector3f.read(in);
        dist = in.readFloat();
        type = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        Vector3f.write(out, normal);
        out.writeFloat(dist);
        out.writeInt(type);
    }
}
