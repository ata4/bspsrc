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
 * DLeaf structure variant used in Vindictus that uses integers in place of shorts.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DLeafVin extends DLeaf {
    
    @Override
    public int getSize() {
        return 56;
    }

    @Override
    public void read(LumpDataInput li) throws IOException {
        contents = li.readInt();
        cluster = (short) li.readInt();
        areaFlags = (short) li.readInt();
        mins[0] = (short) li.readInt();
        mins[1] = (short) li.readInt();
        mins[2] = (short) li.readInt();
        maxs[0] = (short) li.readInt();
        maxs[1] = (short) li.readInt();
        maxs[2] = (short) li.readInt();
        fstleafface = li.readInt();
        numleafface = li.readInt();
        fstleafbrush = li.readInt();
        numleafbrush = li.readInt();
        leafWaterDataID = (short) li.readInt();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(contents);
        lo.writeInt(cluster);
        lo.writeInt(areaFlags);
        lo.writeInt(mins[0]);
        lo.writeInt(mins[1]);
        lo.writeInt(mins[2]);
        lo.writeInt(maxs[0]);
        lo.writeInt(maxs[1]);
        lo.writeInt(maxs[2]);
        lo.writeInt(fstleafface);
        lo.writeInt(numleafface);
        lo.writeInt(fstleafbrush);
        lo.writeInt(numleafbrush);
        lo.writeInt(leafWaterDataID);
    }
}
