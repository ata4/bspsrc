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
 * DAreaportal variant for Vindictus that uses integers in place of shorts.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DAreaportalVin extends DAreaportal {

    @Override
    public int getSize() {
        return 20;
    }

    @Override
    public void read(LumpDataInput li) throws IOException {
        portalKey = (short) li.readInt();
        otherportal = (short) li.readInt();
        firstClipPortalVert = (short) li.readInt();
        clipPortalVerts = (short) li.readInt();
        planenum = li.readInt();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(portalKey);
        lo.writeInt(otherportal);
        lo.writeInt(firstClipPortalVert);
        lo.writeInt(clipPortalVerts);
        lo.writeInt(planenum);
    }
}
