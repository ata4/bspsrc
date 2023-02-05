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

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

/**
 * DStaticProp V6 variant for Bloody Good Time.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV6BGT extends DStaticPropV6 {

    public String targetname;

    @Override
    public int getSize() {
        return super.getSize() + DStaticPropV5Ship.TARGETNAME_LEN; // 192
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        targetname = in.readStringFixed(DStaticPropV5Ship.TARGETNAME_LEN);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeStringFixed(targetname, DStaticPropV5Ship.TARGETNAME_LEN);
    }
}
