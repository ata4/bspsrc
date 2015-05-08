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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
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
    public void read(DataInputReader in) throws IOException {
        origin[0] = in.readInt();
        origin[1] = in.readInt();
        origin[2] = in.readInt();
        size = (byte) in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeInt(origin[0]);
        out.writeInt(origin[1]);
        out.writeInt(origin[2]);
        out.writeInt(size);
    }
}
