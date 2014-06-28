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

import info.ata4.bsplib.vector.Vector3f;
import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import java.io.IOException;

/**
 * DDispInfo variant for Vindictus that uses integers in place of shorts.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDispInfoVin extends DDispInfo {
    
    private int unknown;
    private byte[] neighborsVin = new byte[146];
    
    @Override
    public int getSize() {
        return 232;
    }
    
    @Override
    public void read(DataInputReader in) throws IOException {
        startPos = Vector3f.read(in);
        dispVertStart = in.readInt();
        dispTriStart = in.readInt();
        power = in.readInt();
        smoothingAngle = in.readFloat();
        unknown = in.readInt();
        contents = in.readInt();
        mapFace = in.readUnsignedShort();
        lightmapAlphaStart = in.readInt();
        lightmapSamplePositionStart = in.readInt();
        in.readFully(neighborsVin);

        for (int i = 0; i < allowedVerts.length; i++) {
            allowedVerts[i] = in.readInt();
        }
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        Vector3f.write(out, startPos);
        out.writeInt(dispVertStart);
        out.writeInt(dispTriStart);
        out.writeInt(power);
        out.writeFloat(smoothingAngle);
        out.writeInt(unknown);
        out.writeInt(contents);
        out.writeShort(mapFace);
        out.writeInt(lightmapAlphaStart);
        out.writeInt(lightmapSamplePositionStart);
        out.write(neighborsVin);

        for (int i = 0; i < allowedVerts.length; i++) {
            out.writeInt(allowedVerts[i]);
        }
    }
}
