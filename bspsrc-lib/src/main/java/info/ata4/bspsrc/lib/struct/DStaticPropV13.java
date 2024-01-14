package info.ata4.bspsrc.lib.struct;

import info.ata4.bspsrc.lib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

// Strata added non-uniform scaling, replacing the uniformScale member
public class DStaticPropV13 extends DStaticPropV10CSGO {

    public Vector3f scale;

    @Override
    public int getSize() {
        return super.getSize() + 12; // 88
    }

    @Override
    public void read(DataReader in) throws IOException
    {
        super.read(in);
        scale = Vector3f.read(in);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        super.write(out);
        Vector3f.write(out, scale);
    }
}
