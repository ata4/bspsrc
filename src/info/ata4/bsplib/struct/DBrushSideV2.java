/*
 ** 2011 September 25
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
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DBrushSideV2 extends DBrushSide {
    
    public boolean thin;
    
    @Override
    public void read(LumpDataInput li) throws IOException {
        pnum = li.readUnsignedShort();
        texinfo = li.readShort();
        dispinfo = li.readShort();
        bevel = li.readBoolean();
        thin = li.readBoolean();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeShort(pnum);
        lo.writeShort(texinfo);
        lo.writeShort(dispinfo);
        lo.writeBoolean(bevel);
        lo.writeBoolean(thin);
    }
}
