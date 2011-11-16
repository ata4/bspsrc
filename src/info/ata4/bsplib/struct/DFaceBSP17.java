/*
 ** 2011 September 25
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
 * DFace variant for the very old BSP v17 format.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DFaceBSP17 extends DFace {
    public int[] avgLightColor = new int[MAXLIGHTMAPS];
    
    @Override
    public int getSize() {
        return 68;
    }
    
    @Override
    public void read(LumpDataInput li) throws IOException {
        for (int i = 0; i < MAXLIGHTMAPS; i++) {
            avgLightColor[i] = li.readInt();
        }
        
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
        smoothingGroups = li.readInt();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        for (int i = 0; i < MAXLIGHTMAPS; i++) {
           lo.writeInt(avgLightColor[i]);
        }
        
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
        lo.writeInt(smoothingGroups);
    }
}
