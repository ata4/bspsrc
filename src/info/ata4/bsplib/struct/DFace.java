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

    public int getSize() {
        return 56;
    }

    public void read(LumpDataInput li) throws IOException {
        pnum = li.readUnsignedShort();
        side = li.readByte();
        onnode = li.readByte();
        fstedge = li.readInt();
        numedge = li.readShort();
        texinfo = li.readShort();
        dispInfo = li.readShort();
        surfaceFogVolumeID = li.readUnsignedShort();
        li.readFully(styles);
        lightofs = li.readInt();
        area = li.readFloat();
        lightmapTextureMinsInLuxels[0] = li.readInt();
        lightmapTextureMinsInLuxels[1] = li.readInt();
        lightmapTextureSizeInLuxels[0] = li.readInt();
        lightmapTextureSizeInLuxels[1] = li.readInt();
        origFace = li.readInt();
        firstPrimID = li.readUnsignedShort();
        numPrims = li.readUnsignedShort();
        smoothingGroups = li.readInt();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeShort(pnum);
        lo.writeByte(side);
        lo.writeByte(onnode);
        lo.writeInt(fstedge);
        lo.writeShort(numedge);
        lo.writeShort(texinfo);
        lo.writeShort(dispInfo);
        lo.writeShort(surfaceFogVolumeID);
        lo.write(styles);
        lo.writeInt(lightofs);
        lo.writeFloat(area);
        lo.writeInt(lightmapTextureMinsInLuxels[0]);
        lo.writeInt(lightmapTextureMinsInLuxels[1]);
        lo.writeInt(lightmapTextureSizeInLuxels[0]);
        lo.writeInt(lightmapTextureSizeInLuxels[1]);
        lo.writeInt(origFace);
        lo.writeShort(firstPrimID);
        lo.writeShort(numPrims);
        lo.writeInt(smoothingGroups);
    }
}
