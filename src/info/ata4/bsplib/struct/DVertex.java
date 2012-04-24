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

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
import info.ata4.bsplib.vector.Vector3f;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DVertex implements DStruct {
    
    public Vector3f point;

    public int getSize() {
        return 12;
    }

    public void read(LumpDataInput li) throws IOException {
        point = li.readVector3f();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(point);
    }
    
}
