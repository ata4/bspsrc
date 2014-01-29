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
 * DStaticProp variant for The Ship
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropShip extends DStaticPropV5 {
    
    public String targetname;
    
    public static final int TARGETNAME_LEN = 128;
    
    @Override
    public int getSize() {
        return super.getSize() + TARGETNAME_LEN;
    }
    
    @Override
    public void read(DataInputReader in) throws IOException {
        super.read(in);
        targetname = in.readStringPadded(TARGETNAME_LEN);
    }
    
    @Override
    public void write(DataOutputWriter out) throws IOException {
        super.write(out);
        out.writeStringPadded(targetname, TARGETNAME_LEN);
    }
}
