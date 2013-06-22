/*
 ** 2011 September 25
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
 * DDispInfo structure variant for very old HL2 beta maps that don't seem to
 * have triangle tags.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDispInfoBSP17 extends DDispInfo {
    
    @Override
    public int getTriangleTagCount() {
       return 0;
    }
    
    @Override
    public int getSize() {
        return 172;
    }
    
    @Override
    public void read(LumpInput lio) throws IOException {
        startPos = lio.readVector3f();
        dispVertStart = lio.readInt();
        power = lio.readInt();
        minTess = lio.readInt();
        smoothingAngle = lio.readFloat();
        contents = lio.readInt();
        mapFace = lio.readUnsignedShort();
        lightmapAlphaStart = lio.readInt();
        lightmapSamplePositionStart = lio.readInt();
        lio.readFully(neighbors);
        
        for (int i = 0; i < allowedVerts.length; i++) {
            allowedVerts[i] = lio.readInt();
        }
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeVector3f(startPos);
        lio.writeInt(dispVertStart);
        lio.writeInt(power);
        lio.writeInt(minTess);
        lio.writeFloat(smoothingAngle);
        lio.writeInt(contents);
        lio.writeShort(mapFace);
        lio.writeInt(lightmapAlphaStart);
        lio.writeInt(lightmapSamplePositionStart);
        lio.write(neighbors);
        
        for (int i = 0; i < allowedVerts.length; i++) {
            lio.writeInt(allowedVerts[i]);
        }
    }
}
