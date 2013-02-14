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
 * DFace variant for Vindictus that uses integers rather than shorts.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DFaceVin extends DFace {
    
    private short unknown;
    
    @Override
    public int getSize() {
        return 72;
    }

    @Override
    public void read(LumpDataInput li) throws IOException {
        pnum = li.readInt();
        side = li.readByte();
        onnode = li.readByte();
        unknown = li.readShort();
        fstedge = li.readInt();
        numedge = (short) li.readInt();
        texinfo = (short) li.readInt();
        dispInfo = (short) li.readInt();
        surfaceFogVolumeID = li.readInt();
        li.readFully(styles);
        lightofs = li.readInt();
        area = li.readFloat();
        lightmapTextureMinsInLuxels[0] = li.readInt();
        lightmapTextureMinsInLuxels[1] = li.readInt();
        lightmapTextureSizeInLuxels[0] = li.readInt();
        lightmapTextureSizeInLuxels[1] = li.readInt();
        origFace = li.readInt();
        firstPrimID = li.readInt();
        numPrims = li.readInt();
        smoothingGroups = li.readInt();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(pnum);
        lo.writeByte(side);
        lo.writeByte(onnode);
        lo.writeShort(unknown);
        lo.writeInt(fstedge);
        lo.writeInt(numedge);
        lo.writeInt(texinfo);
        lo.writeInt(dispInfo);
        lo.writeInt(surfaceFogVolumeID);
        lo.write(styles);
        lo.writeInt(lightofs);
        lo.writeFloat(area);
        lo.writeInt(lightmapTextureMinsInLuxels[0]);
        lo.writeInt(lightmapTextureMinsInLuxels[1]);
        lo.writeInt(lightmapTextureSizeInLuxels[0]);
        lo.writeInt(lightmapTextureSizeInLuxels[1]);
        lo.writeInt(origFace);
        lo.writeInt(firstPrimID);
        lo.writeInt(numPrims);
        lo.writeInt(smoothingGroups);
    }
}
