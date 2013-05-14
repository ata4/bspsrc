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

import info.ata4.bsplib.lump.LumpIO;
import java.io.IOException;

/**
 * DOverlay variant for Vindictus that uses integers in place of shorts.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOverlayVin extends DOverlay {
    
    @Override
    public int getSize() {
        return 356;
    }

    @Override
    public void read(LumpIO lio) throws IOException {
        id = lio.readInt();
        texinfo = (short) lio.readInt();
        faceCountAndRenderOrder = lio.readInt();

        for (int j = 0; j < OVERLAY_BSP_FACE_COUNT; j++) {
            ofaces[j] = lio.readInt();
        }

        u[0] = lio.readFloat();
        u[1] = lio.readFloat();
        v[0] = lio.readFloat();
        v[1] = lio.readFloat();

        for (int j = 0; j < 4; j++) {
            uvpoints[j] = lio.readVector3f();
        }

        origin = lio.readVector3f();
        basisNormal = lio.readVector3f();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeInt(id);
        lio.writeInt(texinfo);
        lio.writeInt(faceCountAndRenderOrder);
        
        for (int j = 0; j < OVERLAY_BSP_FACE_COUNT; j++) {
            lio.writeInt(ofaces[j]);
        }
        
        lio.writeFloat(u[0]);
        lio.writeFloat(u[1]);
        lio.writeFloat(v[0]);
        lio.writeFloat(v[1]);
        
        for (int j = 0; j < 4; j++) {
            lio.writeVector3f(origin);
        }
    }
}
