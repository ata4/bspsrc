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
 * Areaportal data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DAreaportal implements DStruct {
    
    public short portalKey;
    public short otherportal;
    public short firstClipPortalVert;
    public short clipPortalVerts;
    public int planenum;

    public int getSize() {
        return 12;
    }

    public void read(LumpDataInput li) throws IOException {
        portalKey = li.readShort();
        otherportal = li.readShort();
        firstClipPortalVert = li.readShort();
        clipPortalVerts = li.readShort();
        planenum = li.readInt();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeShort(portalKey);
        lo.writeShort(otherportal);
        lo.writeShort(firstClipPortalVert);
        lo.writeShort(clipPortalVerts);
        lo.writeInt(planenum);
    }
}
