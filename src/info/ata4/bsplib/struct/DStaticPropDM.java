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
public class DStaticPropDM extends DStaticPropV6 {
    
    protected byte[] unknown = new byte[72];
    
    @Override
    public int getSize() {
        return super.getSize() + 72;
    }
    
    @Override
    public void read(LumpInput lio) throws IOException {
        super.read(lio);
        lio.readFully(unknown);
    }
    
    @Override
    public void write(LumpOutput lio) throws IOException {
        super.write(lio);
        lio.write(unknown);
    }
}
