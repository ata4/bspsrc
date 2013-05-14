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

import info.ata4.bsplib.lump.LumpIO;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DBrushSideV2 extends DBrushSide {
    
    public boolean thin;
    
    @Override
    public void read(LumpIO lio) throws IOException {
        pnum = lio.readUnsignedShort();
        texinfo = lio.readShort();
        dispinfo = lio.readShort();
        bevel = lio.readBoolean();
        thin = lio.readBoolean();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeShort(pnum);
        lio.writeShort(texinfo);
        lio.writeShort(dispinfo);
        lio.writeBoolean(bevel);
        lio.writeBoolean(thin);
    }
}
