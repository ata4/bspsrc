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
    public void read(LumpInput lio) throws IOException {
        vector = lio.readVector3f();
        dist = lio.readFloat();
        alpha = lio.readFloat();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeVector3f(vector);
        lio.writeFloat(dist);
        lio.writeFloat(alpha);
    }
}
