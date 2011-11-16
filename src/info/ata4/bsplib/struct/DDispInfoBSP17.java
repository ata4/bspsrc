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

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
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
    public void read(LumpDataInput li) throws IOException {
        startPos = li.readVector3f();
        dispVertStart = li.readInt();
        power = li.readInt();
        minTess = li.readInt();
        smoothingAngle = li.readFloat();
        contents = li.readInt();
        mapFace = li.readUnsignedShort();
        lightmapAlphaStart = li.readInt();
        lightmapSamplePositionStart = li.readInt();
        li.readFully(neighbors);
        
        for (int i = 0; i < ALLOWEDVERTS_SIZE; i++) {
            allowedVerts[i] = li.readInt();
        }
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(startPos);
        lo.writeInt(dispVertStart);
        lo.writeInt(power);
        lo.writeInt(minTess);
        lo.writeFloat(smoothingAngle);
        lo.writeInt(contents);
        lo.writeShort(mapFace);
        lo.writeInt(lightmapAlphaStart);
        lo.writeInt(lightmapSamplePositionStart);
        lo.write(neighbors);
        
        for (int i = 0; i < ALLOWEDVERTS_SIZE; i++) {
            lo.writeInt(allowedVerts[i]);
        }
    }
}
