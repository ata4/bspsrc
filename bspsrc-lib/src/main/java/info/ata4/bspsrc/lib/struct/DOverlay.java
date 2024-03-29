/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.lib.struct;

import info.ata4.bspsrc.lib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

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
    public int texinfo;
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
    public void read(DataReader in) throws IOException {
        id = in.readInt();
        texinfo = in.readShort();
        faceCountAndRenderOrder = in.readUnsignedShort();

        for (int j = 0; j < OVERLAY_BSP_FACE_COUNT; j++) {
            ofaces[j] = in.readInt();
        }

        u[0] = in.readFloat();
        u[1] = in.readFloat();
        v[0] = in.readFloat();
        v[1] = in.readFloat();

        for (int j = 0; j < 4; j++) {
            uvpoints[j] = Vector3f.read(in);
        }

        origin = Vector3f.read(in);
        basisNormal = Vector3f.read(in);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(id);
        out.writeShort((short)texinfo);
        out.writeUnsignedShort(faceCountAndRenderOrder);

        for (int j = 0; j < OVERLAY_BSP_FACE_COUNT; j++) {
            out.writeInt(ofaces[j]);
        }

        out.writeFloat(u[0]);
        out.writeFloat(u[1]);
        out.writeFloat(v[0]);
        out.writeFloat(v[1]);

        for (int j = 0; j < 4; j++) {
            Vector3f.write(out, uvpoints[j]);
        }

        Vector3f.write(out, origin);
        Vector3f.write(out, basisNormal);
    }
}
