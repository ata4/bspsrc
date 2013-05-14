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

import info.ata4.bsplib.lump.LumpIO;
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

    @Override
    public int getSize() {
        return 12;
    }

    @Override
    public void read(LumpIO lio) throws IOException {
        portalKey = lio.readShort();
        otherportal = lio.readShort();
        firstClipPortalVert = lio.readShort();
        clipPortalVerts = lio.readShort();
        planenum = lio.readInt();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeShort(portalKey);
        lio.writeShort(otherportal);
        lio.writeShort(firstClipPortalVert);
        lio.writeShort(clipPortalVerts);
        lio.writeInt(planenum);
    }
}
