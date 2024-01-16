package info.ata4.bspsrc.lib.struct;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

public class DPrimitiveStrataV1 extends DPrimitive {

    @Override
    public int getSize() {
        return 20;
    }

    @Override
    public void read(DataReader in) throws IOException {
        type = in.readUnsignedByte();
        in.readUnsignedByte();
        in.readUnsignedByte();
        in.readUnsignedByte(); // padding
        firstIndex = (int)in.readUnsignedInt();
        indexCount = (int)in.readUnsignedInt();
        firstVert = (int)in.readUnsignedInt();
        vertCount = (int)in.readUnsignedInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedByte(type);
        out.writeUnsignedByte(0);
        out.writeUnsignedByte(0);
        out.writeUnsignedByte(0); // padding
        out.writeUnsignedInt(firstIndex);
        out.writeUnsignedInt(indexCount);
        out.writeUnsignedInt(firstVert);
        out.writeUnsignedInt(vertCount);
    }
}
