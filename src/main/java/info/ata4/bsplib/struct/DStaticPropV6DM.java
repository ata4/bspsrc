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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

/**
 * DStaticProp V6 variant for Dark Messiah.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV6DM extends DStaticPropV6 {

    protected byte[] unknown = new byte[72];

    @Override
    public int getSize() {
        return super.getSize() + 72; // 136
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        in.readBytes(unknown);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeBytes(unknown);
    }
}
