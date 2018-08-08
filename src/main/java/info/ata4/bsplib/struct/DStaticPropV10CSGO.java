/*
 ** 2012 April 24
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
 * Old V10 structure found in CS:GO.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV10CSGO extends DStaticPropV9 {

    protected int unknown;

    @Override
    public int getSize() {
        return super.getSize() + 4; // 76
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        unknown = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeInt(unknown);
    }
}
