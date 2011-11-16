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
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOverlaySystemLevel implements DStruct {
    
    public int minCPULevel;
    public int maxCPULevel;
    public int minGPULevel;
    public int maxGPULevel;

    public int getSize() {
        return 4;
    }

    public void read(LumpDataInput li) throws IOException {
        minCPULevel = li.readUnsignedByte();
        maxCPULevel = li.readUnsignedByte();
        minGPULevel = li.readUnsignedByte();
        maxGPULevel = li.readUnsignedByte();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeByte(minCPULevel);
        lo.writeByte(maxCPULevel);
        lo.writeByte(minGPULevel);
        lo.writeByte(maxGPULevel);
    }
    
}
