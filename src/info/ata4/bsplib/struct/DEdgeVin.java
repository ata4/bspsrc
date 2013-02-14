/*
 ** 2013 February 14
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
 * DEdge variant for Vindictus that uses integers in place of shorts.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DEdgeVin extends DEdge {
    
    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void read(LumpDataInput li) throws IOException {
        v[0] = li.readInt();
        v[1] = li.readInt();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(v[0]);
        lo.writeInt(v[1]);
    }
}
