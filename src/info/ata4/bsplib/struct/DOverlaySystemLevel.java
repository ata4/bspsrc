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
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOverlaySystemLevel implements DStruct {
    
    public int minCPULevel;
    public int maxCPULevel;
    public int minGPULevel;
    public int maxGPULevel;

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public void read(LumpInput lio) throws IOException {
        minCPULevel = lio.readUnsignedByte();
        maxCPULevel = lio.readUnsignedByte();
        minGPULevel = lio.readUnsignedByte();
        maxGPULevel = lio.readUnsignedByte();
    }

    @Override
    public void write(LumpOutput lio) throws IOException {
        lio.writeByte(minCPULevel);
        lio.writeByte(maxCPULevel);
        lio.writeByte(minGPULevel);
        lio.writeByte(maxGPULevel);
    }
    
}
