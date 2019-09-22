/*
** 2018 May 31
**
** The author disclaims copyright to this source code. In place of
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
 * V11 structure found in Black Mesa, xengine(cu5) branch and later releases
 * (introduced with the December 2017 Update.)
 * 
 * Possibly found in recent Source 2013 games as well.
 */
public class DStaticPropV11 extends DStaticPropV11lite {

    // m_FlagsEx
    // Additional flags? Purpose and use unknown. Usually 0.
    public int flagsEx;

    @Override
    public int getSize() {
        return super.getSize() + 4; // 80
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        flagsEx = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeInt(flagsEx);
   }
}
