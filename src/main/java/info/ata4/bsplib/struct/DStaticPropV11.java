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
 * V11 structure found in BM, possibly found in recent Source 2013 games as
 * well.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV11 extends DStaticPropV10 {

    public int unknown1; // usually -1
    public int unknown2; // usually 0

    @Override
    public int getSize() {
        return super.getSize() + 8; // 80
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        unknown1 = in.readInt();
        unknown2 = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeInt(unknown1);
        out.writeInt(unknown2);
    }
}
