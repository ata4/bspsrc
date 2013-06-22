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

import info.ata4.bsplib.lump.LumpInput;
import info.ata4.bsplib.lump.LumpOutput;
import java.io.IOException;

/**
 * Face data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DFace implements DStruct {
    
    public static final int MAXLIGHTMAPS = 4;

    public int pnum;
    public byte side;
    public byte onnode;
    public int fstedge;
    public short numedge;
    public short texinfo;
    public short dispInfo;
    public int surfaceFogVolumeID;
    public byte[] styles = new byte[MAXLIGHTMAPS];
    public int lightofs;
    public float area;
    public int[] lightmapTextureMinsInLuxels = new int[2];
    public int[] lightmapTextureSizeInLuxels = new int[2];
    public int origFace;
    public int firstPrimID;
    public int numPrims;
    public int smoothingGroups;

    @Override
    public int getSize() {
        return 56;
    }

    @Override
    public void read(LumpInput lio) throws IOException {
        pnum = lio.readUnsignedShort();
        side = lio.readByte();
        onnode = lio.readByte();
        fstedge = lio.readInt();
        numedge = lio.readShort();
        texinfo = lio.readShort();
        dispInfo = lio.readShort();
        surfaceFogVolumeID = lio.readUnsignedShort();
        lio.readFully(styles);
        lightofs = lio.readInt();
        area = lio.readFloat();
        lightmapTextureMinsInLuxels[0] = lio.readInt();
        lightmapTextureMinsInLuxels[1] = lio.readInt();
        lightmapTextureSizeInLuxels[0] = lio.readInt();
        lightmapTextureSizeInLuxels[1] = lio.readInt();
        origFace = lio.readInt();
        firstPrimID = lio.readUnsignedShort();
        numPrims = lio.readUnsignedShort();
        smoothingGroups = lio.readInt();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeShort(pnum);
        lio.writeByte(side);
        lio.writeByte(onnode);
        lio.writeInt(fstedge);
        lio.writeShort(numedge);
        lio.writeShort(texinfo);
        lio.writeShort(dispInfo);
        lio.writeShort(surfaceFogVolumeID);
        lio.write(styles);
        lio.writeInt(lightofs);
        lio.writeFloat(area);
        lio.writeInt(lightmapTextureMinsInLuxels[0]);
        lio.writeInt(lightmapTextureMinsInLuxels[1]);
        lio.writeInt(lightmapTextureSizeInLuxels[0]);
        lio.writeInt(lightmapTextureSizeInLuxels[1]);
        lio.writeInt(origFace);
        lio.writeShort(firstPrimID);
        lio.writeShort(numPrims);
        lio.writeInt(smoothingGroups);
    }
}
