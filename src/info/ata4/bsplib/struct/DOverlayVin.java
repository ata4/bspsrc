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
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOverlayVin extends DOverlay {
    
    @Override
    public int getSize() {
        return 356;
    }

    @Override
    public void read(LumpDataInput li) throws IOException {
        id = li.readInt();
        texinfo = (short) li.readInt();
        faceCountAndRenderOrder = li.readInt();

        for (int j = 0; j < OVERLAY_BSP_FACE_COUNT; j++) {
            ofaces[j] = li.readInt();
        }

        u[0] = li.readFloat();
        u[1] = li.readFloat();
        v[0] = li.readFloat();
        v[1] = li.readFloat();

        for (int j = 0; j < 4; j++) {
            uvpoints[j] = li.readVector3f();
        }

        origin = li.readVector3f();
        basisNormal = li.readVector3f();
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(id);
        lo.writeInt(texinfo);
        lo.writeInt(faceCountAndRenderOrder);
        
        for (int j = 0; j < OVERLAY_BSP_FACE_COUNT; j++) {
            lo.writeInt(ofaces[j]);
        }
        
        lo.writeFloat(u[0]);
        lo.writeFloat(u[1]);
        lo.writeFloat(v[0]);
        lo.writeFloat(v[1]);
        
        for (int j = 0; j < 4; j++) {
            lo.writeVector3f(origin);
        }
    }
}
