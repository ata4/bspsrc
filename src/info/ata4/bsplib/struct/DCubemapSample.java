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
import java.io.IOException;

/**
 * Cubemap data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DCubemapSample implements DStruct {

    public int[] origin = new int[3];
    public byte size;

    public int getSize() {
        return 16;
    }

    public void read(LumpDataInput li) throws IOException {
        origin[0] = li.readInt();
        origin[1] = li.readInt();
        origin[2] = li.readInt();
        size = (byte) li.readInt();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(origin[0]);
        lo.writeInt(origin[1]);
        lo.writeInt(origin[2]);
        lo.writeInt(size);
    }
}
