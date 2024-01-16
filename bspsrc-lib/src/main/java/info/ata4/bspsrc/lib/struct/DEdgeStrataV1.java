package info.ata4.bspsrc.lib.struct;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

public class DEdgeStrataV1 extends DEdge {

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void read(DataReader in) throws IOException {
        v[0] = (int)in.readUnsignedInt();
        v[1] = (int)in.readUnsignedInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(v[0]);
        out.writeUnsignedInt(v[1]);
    }
}
