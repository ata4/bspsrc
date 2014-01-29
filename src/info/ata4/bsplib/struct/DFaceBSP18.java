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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import java.io.IOException;

/**
 * DFace variant for the old BSP v18 format (HL2, not VTMB).
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DFaceBSP18 extends DFace {
    
    public int[] avgLightColor = new int[MAXLIGHTMAPS];
    
    @Override
    public int getSize() {
        return 72;
    }
    
    @Override
    public void read(DataInputReader in) throws IOException {
        for (int i = 0; i < MAXLIGHTMAPS; i++) {
            avgLightColor[i] = in.readInt();
        }
        
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
        for (int i = 0; i < MAXLIGHTMAPS; i++) {
           out.writeInt(avgLightColor[i]);
        }
        
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
