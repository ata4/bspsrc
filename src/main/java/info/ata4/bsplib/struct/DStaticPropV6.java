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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV6 extends DStaticPropV5 {
    
    public int minDXLevel;
    public int maxDXLevel;
    
    @Override
    public int getSize() {
        return super.getSize() + 4; // 64
    }
    
    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        minDXLevel = in.readUnsignedShort();
        maxDXLevel = in.readUnsignedShort();
    }
    
    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeUnsignedShort(minDXLevel);
        out.writeUnsignedShort(maxDXLevel);
    }
}
