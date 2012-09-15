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
import info.ata4.util.io.EnumConverter;
import java.io.IOException;

/**
 * DTexInfo structure variant for Dark Messiah (single player only).
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DTexInfoDM extends DTexInfo {
    
    private byte[] unknown = new byte[24];
    
    @Override
    public int getSize() {
        return 96;
    }
    
    @Override
    public void read(LumpDataInput li) throws IOException {
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 4; k++) {
                textureVecsTexels[j][k] = li.readFloat();
            }
        }
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 4; k++) {
                lightmapVecsLuxels[j][k] = li.readFloat();
            }
        }
        li.readFully(unknown);
        flags = EnumConverter.fromInteger(SurfaceFlag.class, li.readInt());
        texdata = li.readInt();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 4; k++) {
                lo.writeFloat(textureVecsTexels[j][k]);
            }
        }
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 4; k++) {
                lo.writeFloat(lightmapVecsLuxels[j][k]);
            }
        }
        lo.write(unknown);
        lo.writeInt(EnumConverter.toInteger(flags));
        lo.writeInt(texdata);
    }
}
