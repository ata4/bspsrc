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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
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
    public void read(DataInputReader in) throws IOException {
        pnum = in.readUnsignedShort();
        side = in.readByte();
        onnode = in.readByte();
        fstedge = in.readInt();
        numedge = in.readShort();
        texinfo = in.readShort();
        dispInfo = in.readShort();
        surfaceFogVolumeID = in.readUnsignedShort();
        in.readFully(styles);
        lightofs = in.readInt();
        area = in.readFloat();
        lightmapTextureMinsInLuxels[0] = in.readInt();
        lightmapTextureMinsInLuxels[1] = in.readInt();
        lightmapTextureSizeInLuxels[0] = in.readInt();
        lightmapTextureSizeInLuxels[1] = in.readInt();
        origFace = in.readInt();
        firstPrimID = in.readUnsignedShort();
        numPrims = in.readUnsignedShort();
        smoothingGroups = in.readInt();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeShort(pnum);
        out.writeByte(side);
        out.writeByte(onnode);
        out.writeInt(fstedge);
        out.writeShort(numedge);
        out.writeShort(texinfo);
        out.writeShort(dispInfo);
        out.writeShort(surfaceFogVolumeID);
        out.write(styles);
        out.writeInt(lightofs);
        out.writeFloat(area);
        out.writeInt(lightmapTextureMinsInLuxels[0]);
        out.writeInt(lightmapTextureMinsInLuxels[1]);
        out.writeInt(lightmapTextureSizeInLuxels[0]);
        out.writeInt(lightmapTextureSizeInLuxels[1]);
        out.writeInt(origFace);
        out.writeShort(firstPrimID);
        out.writeShort(numPrims);
        out.writeInt(smoothingGroups);
    }
}
