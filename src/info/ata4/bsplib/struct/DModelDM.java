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
    public void read(LumpDataInput li) throws IOException {
        mins = li.readVector3f();
        maxs = li.readVector3f();
        origin = li.readVector3f();
        unknown = li.readInt();
        headnode = li.readInt();
        fstface = li.readInt();
        numface = li.readInt();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(mins);
        lo.writeVector3f(maxs);
        lo.writeVector3f(origin);
        lo.writeInt(unknown);
        lo.writeInt(headnode);
        lo.writeInt(fstface);
        lo.writeInt(numface);
    }
}
