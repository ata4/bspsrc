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

import info.ata4.bsplib.lump.LumpInput;
import info.ata4.bsplib.lump.LumpOutput;
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
    public void read(LumpInput lio) throws IOException {
        startPos = lio.readVector3f();
        dispVertStart = lio.readInt();
        dispTriStart = lio.readInt();
        power = lio.readInt();
        smoothingAngle = lio.readFloat();
        unknown = lio.readInt();
        contents = lio.readInt();
        mapFace = lio.readUnsignedShort();
        lightmapAlphaStart = lio.readInt();
        lightmapSamplePositionStart = lio.readInt();
        lio.readFully(neighborsVin);

        for (int i = 0; i < allowedVerts.length; i++) {
            allowedVerts[i] = lio.readInt();
        }
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeVector3f(startPos);
        lio.writeInt(dispVertStart);
        lio.writeInt(dispTriStart);
        lio.writeInt(power);
        lio.writeFloat(smoothingAngle);
        lio.writeInt(unknown);
        lio.writeInt(contents);
        lio.writeShort(mapFace);
        lio.writeInt(lightmapAlphaStart);
        lio.writeInt(lightmapSamplePositionStart);
        lio.write(neighborsVin);

        for (int i = 0; i < allowedVerts.length; i++) {
            lio.writeInt(allowedVerts[i]);
        }
    }
}
