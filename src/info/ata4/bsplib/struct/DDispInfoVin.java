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
    public void read(LumpDataInput li) throws IOException {
        startPos = li.readVector3f();
        dispVertStart = li.readInt();
        dispTriStart = li.readInt();
        power = li.readInt();
        smoothingAngle = li.readFloat();
        unknown = li.readInt();
        contents = li.readInt();
        mapFace = li.readUnsignedShort();
        lightmapAlphaStart = li.readInt();
        lightmapSamplePositionStart = li.readInt();
        li.readFully(neighborsVin);

        for (int i = 0; i < ALLOWEDVERTS_SIZE; i++) {
            allowedVerts[i] = li.readInt();
        }
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(startPos);
        lo.writeInt(dispVertStart);
        lo.writeInt(dispTriStart);
        lo.writeInt(power);
        lo.writeFloat(smoothingAngle);
        lo.writeInt(unknown);
        lo.writeInt(contents);
        lo.writeShort(mapFace);
        lo.writeInt(lightmapAlphaStart);
        lo.writeInt(lightmapSamplePositionStart);
        lo.write(neighborsVin);

        for (int i = 0; i < ALLOWEDVERTS_SIZE; i++) {
            lo.writeInt(allowedVerts[i]);
        }
    }
}
