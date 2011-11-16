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
 * Brush side data stucture.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DBrushSide implements DStruct {

    public int pnum;
    public short texinfo;
    public short dispinfo;
    public boolean bevel;

    public int getSize() {
        return 8;
    }

    public void read(LumpDataInput li) throws IOException {
        pnum = li.readUnsignedShort();
        texinfo = li.readShort();
        dispinfo = li.readShort();
        bevel = li.readShort() == 1;
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeShort(pnum);
        lo.writeShort(texinfo);
        lo.writeShort(dispinfo);
        lo.writeShort(bevel ? 1 : 0);
    }
}
