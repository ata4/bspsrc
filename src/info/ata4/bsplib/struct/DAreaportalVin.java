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
    public void read(LumpInput lio) throws IOException {
        portalKey = (short) lio.readInt();
        otherportal = (short) lio.readInt();
        firstClipPortalVert = (short) lio.readInt();
        clipPortalVerts = (short) lio.readInt();
        planenum = lio.readInt();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeInt(portalKey);
        lio.writeInt(otherportal);
        lio.writeInt(firstClipPortalVert);
        lio.writeInt(clipPortalVerts);
        lio.writeInt(planenum);
    }
}
