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

import info.ata4.bsplib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DVertex implements DStruct {
    
    public Vector3f point;

    @Override
    public int getSize() {
        return 12;
    }

    @Override
    public void read(DataReader in) throws IOException {
        point = Vector3f.read(in);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        Vector3f.write(out, point);
    }
    
}
