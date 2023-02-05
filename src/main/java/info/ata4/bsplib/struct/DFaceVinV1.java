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
 * DFace variant for Vindictus that uses integers in place of shorts.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DFaceVinV1 extends DFace {

    protected short unknown1;

    @Override
    public int getSize() {
        return 72;
    }

    @Override
    public void read(DataReader in) throws IOException {
        pnum = in.readInt();
        side = in.readByte();
        onnode = in.readByte();
        unknown1 = in.readShort();
        fstedge = in.readInt();
        numedge = (short) in.readInt();
        texinfo = (short) in.readInt();
        dispInfo = (short) in.readInt();
        surfaceFogVolumeID = in.readInt();
        in.readBytes(styles);
        lightofs = in.readInt();
        area = in.readFloat();
        lightmapTextureMinsInLuxels[0] = in.readInt();
        lightmapTextureMinsInLuxels[1] = in.readInt();
        lightmapTextureSizeInLuxels[0] = in.readInt();
        lightmapTextureSizeInLuxels[1] = in.readInt();
        origFace = in.readInt();
        firstPrimID = in.readInt();
        numPrims = in.readInt();
        smoothingGroups = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(pnum);
        out.writeByte(side);
        out.writeByte(onnode);
        out.writeShort(unknown1);
        out.writeInt(fstedge);
        out.writeInt(numedge);
        out.writeInt(texinfo);
        out.writeInt(dispInfo);
        out.writeInt(surfaceFogVolumeID);
        out.writeBytes(styles);
        out.writeInt(lightofs);
        out.writeFloat(area);
        out.writeInt(lightmapTextureMinsInLuxels[0]);
        out.writeInt(lightmapTextureMinsInLuxels[1]);
        out.writeInt(lightmapTextureSizeInLuxels[0]);
        out.writeInt(lightmapTextureSizeInLuxels[1]);
        out.writeInt(origFace);
        out.writeInt(firstPrimID);
        out.writeInt(numPrims);
        out.writeInt(smoothingGroups);
    }
}
