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

import info.ata4.bsplib.lump.LumpInput;
import info.ata4.bsplib.lump.LumpOutput;
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
    public void read(LumpInput lio) throws IOException {
        contents = lio.readInt();
        cluster = (short) lio.readInt();
        areaFlags = (short) lio.readInt();
        mins[0] = (short) lio.readInt();
        mins[1] = (short) lio.readInt();
        mins[2] = (short) lio.readInt();
        maxs[0] = (short) lio.readInt();
        maxs[1] = (short) lio.readInt();
        maxs[2] = (short) lio.readInt();
        fstleafface = lio.readInt();
        numleafface = lio.readInt();
        fstleafbrush = lio.readInt();
        numleafbrush = lio.readInt();
        leafWaterDataID = (short) lio.readInt();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeInt(contents);
        lio.writeInt(cluster);
        lio.writeInt(areaFlags);
        lio.writeInt(mins[0]);
        lio.writeInt(mins[1]);
        lio.writeInt(mins[2]);
        lio.writeInt(maxs[0]);
        lio.writeInt(maxs[1]);
        lio.writeInt(maxs[2]);
        lio.writeInt(fstleafface);
        lio.writeInt(numleafface);
        lio.writeInt(fstleafbrush);
        lio.writeInt(numleafbrush);
        lio.writeInt(leafWaterDataID);
    }
}
