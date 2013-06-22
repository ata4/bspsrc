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

import info.ata4.bsplib.lump.LumpInput;
import info.ata4.bsplib.lump.LumpOutput;
import java.io.IOException;

/**
 * DFace variant for Vampire: The Masquerade – Bloodlines
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DFaceVTMB extends DFace {
    
    public static final int MAXLIGHTMAPS = 8;
    
    public int[] avgLightColor = new int[MAXLIGHTMAPS];
    public byte[] styles = new byte[MAXLIGHTMAPS];	// lighting info
    public byte[] day = new byte[MAXLIGHTMAPS];		// Nightime lightmapping system
    public byte[] night = new byte[MAXLIGHTMAPS];	// Nightime lightmapping system
    
    @Override
    public int getSize() {
        return 104;
    }

    @Override
    public void read(LumpInput lio) throws IOException {
        for (int i = 0; i < MAXLIGHTMAPS; i++) {
            avgLightColor[i] = lio.readInt();
        }
        
        pnum = lio.readUnsignedShort();
        side = lio.readByte();
        onnode = lio.readByte();
        fstedge = lio.readInt();
        numedge = lio.readShort();
        texinfo = lio.readShort();
        dispInfo = lio.readShort();
        surfaceFogVolumeID = lio.readUnsignedShort();
        lio.readFully(styles);
        lio.readFully(day);
        lio.readFully(night);
        lightofs = lio.readInt();
        area = lio.readFloat();
        lightmapTextureMinsInLuxels[0] = lio.readInt();
        lightmapTextureMinsInLuxels[1] = lio.readInt();
        lightmapTextureSizeInLuxels[0] = lio.readInt();
        lightmapTextureSizeInLuxels[1] = lio.readInt();
        origFace = lio.readInt();
        smoothingGroups = lio.readInt();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        for (int i = 0; i < MAXLIGHTMAPS; i++) {
           lio.writeInt(avgLightColor[i]);
        }
        
        lio.writeShort(pnum);
        lio.writeByte(side);
        lio.writeByte(onnode);
        lio.writeInt(fstedge);
        lio.writeShort(numedge);
        lio.writeShort(texinfo);
        lio.writeShort(dispInfo);
        lio.writeShort(surfaceFogVolumeID);
        lio.write(styles);
        lio.write(day);
        lio.write(night);
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
