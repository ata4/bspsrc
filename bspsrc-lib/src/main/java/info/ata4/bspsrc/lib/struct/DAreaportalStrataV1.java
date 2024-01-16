package info.ata4.bspsrc.lib.struct;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

public class DAreaportalStrataV1 extends DAreaportal {

    @Override
    public int getSize() {
        return 20;
    }

    @Override
    public void read(DataReader in) throws IOException {
        portalKey = in.readInt();
        otherportal = in.readInt();
        firstClipPortalVert = in.readInt();
        clipPortalVerts = in.readInt();
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