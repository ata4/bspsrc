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

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
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
        return super.getSize() + 128;
    }
    
    @Override
    public void read(LumpDataInput li) throws IOException {
        super.read(li);
        targetname = li.readString(128);
    }
    
    @Override
    public void write(LumpDataOutput lo) throws IOException {
        // TODO: needs LumpDataOutput.writeString(int len)
        throw new UnsupportedOperationException();
    }
}
