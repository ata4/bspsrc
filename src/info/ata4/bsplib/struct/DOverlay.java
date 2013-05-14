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

import info.ata4.bsplib.lump.LumpIO;
import info.ata4.bsplib.vector.Vector3f;
import java.io.IOException;

/**
 * Overlay data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOverlay implements DStruct {
    
    public static final int OVERLAY_BSP_FACE_COUNT = 64;
    public static final int OVERLAY_RENDER_ORDER_NUM_BITS = 2;
    public static final int OVERLAY_RENDER_ORDER_MASK = 0xC000; // top 2 bits set
    
    public int id;
    public short texinfo;
    public int faceCountAndRenderOrder;
    public int[] ofaces = new int[OVERLAY_BSP_FACE_COUNT];
    public float[] u = new float[2];
    public float[] v = new float[2];
    public Vector3f[] uvpoints = new Vector3f[4];
    public Vector3f origin;
    public Vector3f basisNormal;
    
    public int getFaceCount() {
        return faceCountAndRenderOrder & ~OVERLAY_RENDER_ORDER_MASK;
    }
    
    public int getRenderOrder() {
        return faceCountAndRenderOrder >> (16 - OVERLAY_RENDER_ORDER_NUM_BITS);
    }

    @Override
    public int getSize() {
        return 352;
    }

    @Override
    public void read(LumpIO lio) throws IOException {
        id = lio.readInt();
        texinfo = lio.readShort();
        faceCountAndRenderOrder = lio.readUnsignedShort();

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

    public void write(LumpIO lio) throws IOException {
        lio.writeInt(id);
        lio.writeShort(texinfo);
        lio.writeShort(faceCountAndRenderOrder);
        
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
