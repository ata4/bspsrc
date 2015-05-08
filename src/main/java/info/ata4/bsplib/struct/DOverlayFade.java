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

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOverlayFade implements DStruct {
    
    public float fadeDistMinSq;
    public float fadeDistMaxSq;

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void read(DataInputReader in) throws IOException {
        fadeDistMinSq = in.readFloat();
        fadeDistMaxSq = in.readFloat();
    }

    @Override
    public void write(DataOutputWriter out) throws IOException {
        out.writeFloat(fadeDistMinSq);
        out.writeFloat(fadeDistMaxSq);
    }
}
