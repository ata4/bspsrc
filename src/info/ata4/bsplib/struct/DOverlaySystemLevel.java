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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
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
    public void read(DataInputReader in) throws IOException {
        minCPULevel = in.readUnsignedByte();
        maxCPULevel = in.readUnsignedByte();
        minGPULevel = in.readUnsignedByte();
        maxGPULevel = in.readUnsignedByte();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeByte(minCPULevel);
        out.writeByte(maxCPULevel);
        out.writeByte(minGPULevel);
        out.writeByte(maxGPULevel);
    }
    
}
