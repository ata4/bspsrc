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

    public int getSize() {
        return 352;
    }

    public void read(LumpDataInput li) throws IOException {
        id = li.readInt();
        texinfo = li.readShort();
        faceCountAndRenderOrder = li.readUnsignedShort();

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

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(id);
        lo.writeShort(texinfo);
        lo.writeShort(faceCountAndRenderOrder);
        
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
