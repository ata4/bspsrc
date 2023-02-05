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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

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
    public void read(DataReader in) throws IOException {
        portalKey = (short) in.readInt();
        otherportal = (short) in.readInt();
        firstClipPortalVert = (short) in.readInt();
        clipPortalVerts = (short) in.readInt();
        planenum = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(portalKey);
        out.writeInt(otherportal);
        out.writeInt(firstClipPortalVert);
        out.writeInt(clipPortalVerts);
        out.writeInt(planenum);
    }
}
