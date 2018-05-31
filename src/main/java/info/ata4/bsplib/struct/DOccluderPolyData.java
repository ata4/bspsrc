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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
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

    @Override
    public int getSize() {
        return 12;
    }

    @Override
    public void read(DataReader in) throws IOException {
        firstvertexindex = in.readInt();
        vertexcount = in.readInt();
        planenum = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(firstvertexindex);
        out.writeInt(vertexcount);
        out.writeInt(planenum);
    }
}
