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
 * Displacement info data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDispInfo implements DStruct {

    public static final int DISP_INFO_FLAG_HAS_MULTIBLEND = 0x40000000;
    public static final int DISP_INFO_FLAG_MAGIC = 0x80000000;

    public Vector3f startPos;           // start position
    public int dispVertStart;           // index into disp verts
    public int dispTriStart;            // index into disp tris
    public int power;                   // power (size)
    public int minTess;                 // min tesselation
    public float smoothingAngle;        // smoothing angle
    public int contents;                // surf contents
    public int mapFace;                 // map face
    public int lightmapAlphaStart;
    public int lightmapSamplePositionStart;
    protected byte[] neighbors = new byte[90]; // TODO: use structures
    public int[] allowedVerts = new int[10]; // allowed verts

    public int getPowerSize() {
        return 1 << power;
    }

    public int getVertexCount() {
        return (getPowerSize() + 1) * (getPowerSize() + 1);
    }

    public int getTriangleTagCount() {
        return 2 * getPowerSize() * getPowerSize();
    }

    public int getSize() {
        return 176;
    }

    public void read(LumpDataInput li) throws IOException {
        startPos = li.readVector3f();
        dispVertStart = li.readInt();
        dispTriStart = li.readInt();
        power = li.readInt();
        minTess = li.readInt();
        smoothingAngle = li.readFloat();
        contents = li.readInt();
        mapFace = li.readUnsignedShort();
        lightmapAlphaStart = li.readInt();
        lightmapSamplePositionStart = li.readInt();
        li.readFully(neighbors);

        for (int i = 0; i < allowedVerts.length; i++) {
            allowedVerts[i] = li.readInt();
        }
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(startPos);
        lo.writeInt(dispVertStart);
        lo.writeInt(dispTriStart);
        lo.writeInt(power);
        lo.writeInt(minTess);
        lo.writeFloat(smoothingAngle);
        lo.writeInt(contents);
        lo.writeShort(mapFace);
        lo.writeInt(lightmapAlphaStart);
        lo.writeInt(lightmapSamplePositionStart);
        lo.write(neighbors);

        for (int i = 0; i < allowedVerts.length; i++) {
            lo.writeInt(allowedVerts[i]);
        }
    }

    public boolean hasMultiBlend() {
        return ((minTess + DDispInfo.DISP_INFO_FLAG_MAGIC) & DDispInfo.DISP_INFO_FLAG_HAS_MULTIBLEND) != 0;
    }
}
