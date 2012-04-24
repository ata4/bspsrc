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

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV5 extends DStaticPropV4 {
    
    public float forcedFadeScale;
    
    @Override
    public int getSize() {
        return super.getSize() + 4;
    }
    
    @Override
    public void read(LumpDataInput li) throws IOException {
        super.read(li);
        forcedFadeScale = li.readFloat();
    }
    
    @Override
    public void write(LumpDataOutput lo) throws IOException {
        super.write(lo);
        lo.writeFloat(forcedFadeScale);
    }
}
