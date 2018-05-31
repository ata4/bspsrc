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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 * Brush side data stucture.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DBrushSide implements DStruct {

    public int pnum;
    public short texinfo;
    public short dispinfo;
    public boolean bevel;

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void read(DataReader in) throws IOException {
        pnum = in.readUnsignedShort();
        texinfo = in.readShort();
        dispinfo = in.readShort();
        bevel = in.readShort() == 1;
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedShort(pnum);
        out.writeShort(texinfo);
        out.writeShort(dispinfo);
        out.writeUnsignedShort(bevel ? 1 : 0);
    }
}
