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
 * DStaticProp V7 variant for Zeno Clash.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV7ZC extends DStaticPropV6 {
    
    protected int unknown;
    
    @Override
    public int getSize() {
        return super.getSize() + 4; // 68
    }
    
    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        unknown = in.readInt();
    }
    
    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeInt(unknown);
    }
}
