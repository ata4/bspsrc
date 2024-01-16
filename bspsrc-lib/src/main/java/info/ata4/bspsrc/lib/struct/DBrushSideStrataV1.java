package info.ata4.bspsrc.lib.struct;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

public class DBrushSideStrataV1 extends DBrushSideV0New {

    @Override
    public int getSize() {
        return 16;
    }

    @Override
    public void read(DataReader in) throws IOException {
        pnum = (int)in.readUnsignedInt();
        texinfo = in.readInt();
        dispinfo = in.readInt();
        bevel = in.readBoolean();        
        thin = in.readBoolean();
        in.readShort(); // padding
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(pnum);
        out.writeInt(texinfo);
        out.writeInt(dispinfo);
        out.writeBoolean(bevel);
        out.writeBoolean(thin);
        out.writeUnsignedShort(0); // padding
    }
}