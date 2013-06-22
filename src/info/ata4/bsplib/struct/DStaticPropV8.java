/*
 ** 2011 September 26
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
public class DStaticPropV8 extends DStaticPropV5 {
    
    public byte minCPULevel;
    public byte maxCPULevel;
    public byte minGPULevel;
    public byte maxGPULevel;
    public Color32 diffuseModulation;
    
    @Override
    public int getSize() {
        return super.getSize() + 8;
    }
    
    @Override
    public void read(LumpInput lio) throws IOException {
        super.read(lio);
        minCPULevel = lio.readByte();
        maxCPULevel = lio.readByte();
        minGPULevel = lio.readByte();
        maxGPULevel = lio.readByte();
        diffuseModulation = lio.readColor32();
    }
    
    @Override
    public void write(LumpOutput lio) throws IOException {
        super.write(lio);
        lio.writeByte(minCPULevel);
        lio.writeByte(maxCPULevel);
        lio.writeByte(minGPULevel);
        lio.writeByte(maxGPULevel);
        lio.writeColor32(diffuseModulation);
    }
}
