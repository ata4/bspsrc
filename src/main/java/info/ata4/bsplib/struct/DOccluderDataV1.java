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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DOccluderDataV1 extends DOccluderData {

    public int area;

    @Override
    public int getSize() {
        return super.getSize() + 4;
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        area = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(area);
    }
}
