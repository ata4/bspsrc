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
 * DModel structure variant for Dark Messiah (single player only)
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DModelDM extends DModel {
    
    protected int unknown;
    
    @Override
    public int getSize() {
        return 52;
    }

    @Override
    public void read(LumpIO lio) throws IOException {
        mins = lio.readVector3f();
        maxs = lio.readVector3f();
        origin = lio.readVector3f();
        unknown = lio.readInt();
        headnode = lio.readInt();
        fstface = lio.readInt();
        numface = lio.readInt();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeVector3f(mins);
        lio.writeVector3f(maxs);
        lio.writeVector3f(origin);
        lio.writeInt(unknown);
        lio.writeInt(headnode);
        lio.writeInt(fstface);
        lio.writeInt(numface);
    }
}
