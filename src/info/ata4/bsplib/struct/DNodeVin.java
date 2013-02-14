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
 * DNode variant for Vindictus that uses integers in place of shorts.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DNodeVin extends DNode {

    @Override
    public int getSize() {
        return 48;
    }

    @Override
    public void read(LumpDataInput li) throws IOException {
        planenum = li.readInt();
        children[0] = li.readInt();
        children[1] = li.readInt();
        mins[0] = (short) li.readInt();
        mins[1] = (short) li.readInt();
        mins[2] = (short) li.readInt();
        maxs[0] = (short) li.readInt();
        maxs[1] = (short) li.readInt();
        maxs[2] = (short) li.readInt();
        fstface = li.readInt();
        numface = li.readInt();
        li.readInt(); // paddding
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(planenum);
        lo.writeInt(children[0]);
        lo.writeInt(children[1]);
        lo.writeInt(mins[0]);
        lo.writeInt(mins[1]);
        lo.writeInt(mins[2]);
        lo.writeInt(maxs[0]);
        lo.writeInt(maxs[1]);
        lo.writeInt(maxs[2]);
        lo.writeInt(fstface);
        lo.writeInt(numface);
        lo.writeInt(0); // paddding
    }
}
