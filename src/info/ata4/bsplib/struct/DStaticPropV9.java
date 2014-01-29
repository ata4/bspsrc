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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV9 extends DStaticPropV8 {
    
    public boolean disableX360;
    
    @Override
    public int getSize() {
        return super.getSize() + 4;
    }
    
    @Override
    public void read(DataInputReader in) throws IOException {
        super.read(in);
        disableX360 = in.readBoolean();
        in.skipBytes(3); // non-zero garbage?
    }
    
    @Override
    public void write(DataOutputWriter out) throws IOException {
        super.write(out);
        out.writeInt(disableX360 ? 1 : 0);
    }
}
