/*
** 2019 Sep 3
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
 * Older V11 structure found in Black Mesa, surface-tension-update(cu3)
 * and halloween-update(cu4) branches.
 */
public class DStaticPropV11lite extends DStaticPropV10 {

    // m_DiffuseModulation
    // Contains the "rendercolor" and "renderamt" key-values, which set the
    // fade color (R G B) and fade alpha (A), respectively.
    // Usually set to 0xffffff as the default values are "255 255 255" for
    // rendercolor and "255" for renderamt.
    // It's unclear if these key-values have any practical use.
    // bm_c2a5g is the only BM stock map where these values have been set to
    // non-default values for a few props.
    public Color32 diffuseModulation;

    @Override
    public int getSize() {
        return super.getSize() + 4; // 76
    }

    @Override
    public void read(DataReader in) throws IOException {
        super.read(in);
        diffuseModulation = new Color32(in.readInt());
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        out.writeInt(diffuseModulation.rgba);
   }
}
