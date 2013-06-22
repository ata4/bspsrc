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
import info.ata4.util.EnumConverter;
import java.io.IOException;
import java.util.Set;

/**
 * Texture info data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DTexInfo implements DStruct {

    public static final int TEXINFO_NODE = -1;

    public float[][] textureVecsTexels = new float[2][4];   // [s/t][xyz offset]
    public float[][] lightmapVecsLuxels = new float[2][4];  // [s/t][xyz offset] - length is in units of texels/area
    public Set<SurfaceFlag> flags;  // miptex flags + overrides
    public int texdata;             // Pointer to texture name, size, etc.

    @Override
    public int getSize() {
        return 72;
    }

    @Override
    public void read(LumpInput lio) throws IOException {
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 4; k++) {
                textureVecsTexels[j][k] = lio.readFloat();
            }
        }
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 4; k++) {
                lightmapVecsLuxels[j][k] = lio.readFloat();
            }
        }
        flags = EnumConverter.fromInteger(SurfaceFlag.class, lio.readInt());
        texdata = lio.readInt();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 4; k++) {
                lio.writeFloat(textureVecsTexels[j][k]);
            }
        }
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 4; k++) {
                lio.writeFloat(lightmapVecsLuxels[j][k]);
            }
        }
        lio.writeInt(EnumConverter.toInteger(flags));
        lio.writeInt(texdata);
    }
}
