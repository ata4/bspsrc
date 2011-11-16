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
 * Occluder polygon data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOccluderPolyData implements DStruct {
    
    public int firstvertexindex;
    public int vertexcount;
    public int planenum;

    public int getSize() {
        return 12;
    }

    public void read(LumpDataInput li) throws IOException {
        firstvertexindex = li.readInt();
        vertexcount = li.readInt();
        planenum = li.readInt();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(firstvertexindex);
        lo.writeInt(vertexcount);
        lo.writeInt(planenum);
    }
}
