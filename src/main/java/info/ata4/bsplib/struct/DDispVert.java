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
 * Displacement vertex data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDispVert implements DStruct {

    public Vector3f vector;
    public float dist;
    public float alpha;

    @Override
    public int getSize() {
        return 20;
    }

    @Override
    public void read(DataReader in) throws IOException {
        vector = Vector3f.read(in);
        dist = in.readFloat();
        alpha = in.readFloat();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        Vector3f.write(out, vector);
        out.writeFloat(dist);
        out.writeFloat(alpha);
    }
}
