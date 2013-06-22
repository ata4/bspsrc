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
import java.io.IOException;

/**
 * Cubemap data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DCubemapSample implements DStruct {

    public int[] origin = new int[3];
    public byte size;

    @Override
    public int getSize() {
        return 16;
    }

    @Override
    public void read(LumpInput lio) throws IOException {
        origin[0] = lio.readInt();
        origin[1] = lio.readInt();
        origin[2] = lio.readInt();
        size = (byte) lio.readInt();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeInt(origin[0]);
        lio.writeInt(origin[1]);
        lio.writeInt(origin[2]);
        lio.writeInt(size);
    }
}
