/*
 ** 2011 September 25
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.lib.struct;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDispTri implements DStruct {

    public int tags;

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public void read(DataReader in) throws IOException {
        tags = in.readUnsignedShort();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedShort(tags);
    }
}
