/*
 ** 2011 November 28
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
 * DDispInfo structure variant for BSP v23 maps.
 * TODO: could be Dota 2 only?
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDispInfoBSP23 extends DDispInfo {
    
    protected int unknown1;
    protected int unknown2;
    
    @Override
    public int getSize() {
        return 184;
    }
    
    @Override
    public void read(LumpInput lio) throws IOException {
        startPos = lio.readVector3f();
        dispVertStart = lio.readInt();
        dispTriStart = lio.readInt();
        power = lio.readInt();
        minTess = lio.readInt();
        smoothingAngle = lio.readFloat();
        contents = lio.readInt();
        unknown1 = lio.readInt();
        mapFace = lio.readUnsignedShort();
        lightmapAlphaStart = lio.readInt();
        lightmapSamplePositionStart = lio.readInt();
        unknown2 = lio.readInt();
        lio.readFully(neighbors);
        
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
        lio.writeInt(minTess);
        lio.writeFloat(smoothingAngle);
        lio.writeInt(contents);
        lio.write(unknown1);
        lio.writeShort(mapFace);
        lio.writeInt(lightmapAlphaStart);
        lio.writeInt(lightmapSamplePositionStart);
        lio.write(unknown2);
        lio.write(neighbors);
        
        for (int i = 0; i < allowedVerts.length; i++) {
            lio.writeInt(allowedVerts[i]);
        }
    }
}
