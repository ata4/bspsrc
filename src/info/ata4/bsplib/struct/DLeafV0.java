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

import info.ata4.bsplib.lump.LumpIO;
import java.io.IOException;

/**
 * DLeaf structure variant used in the release version of Half-Life 2 only.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DLeafV0 extends DLeaf {
    
    public byte[] ambientLighting = new byte[24];
    
    @Override
    public int getSize() {
        return super.getSize() + ambientLighting.length + 2;
    }
    
    @Override
    public void read(LumpIO lio) throws IOException {
        super.read(lio);
        lio.readFully(ambientLighting);
        lio.readShort(); // padding
    }
   
    @Override
    public void write(LumpIO lio) throws IOException {
        super.write(lio);
        lio.write(ambientLighting);
        lio.writeShort(0); // padding
    }
}
