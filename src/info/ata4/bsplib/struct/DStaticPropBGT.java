/*
 ** 2011 Oktober 20
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
 * DStaticProp variant for Bloody Good Time
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropBGT extends DStaticPropV6 {
    
    public String targetname;
    
    @Override
    public int getSize() {
        return super.getSize() + DStaticPropShip.TARGETNAME_LEN;
    }
    
    @Override
    public void read(LumpInput lio) throws IOException {
        super.read(lio);
        targetname = lio.readString(DStaticPropShip.TARGETNAME_LEN);
    }
    
    @Override
    public void write(LumpOutput lio) throws IOException {
        super.write(lio);
        lio.writeString(targetname, DStaticPropShip.TARGETNAME_LEN);
    }
}
