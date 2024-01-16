package info.ata4.bspsrc.lib.struct;

import info.ata4.bspsrc.lib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

public class DNodeStrataV1 extends DNode {

    @Override
    public int getSize() {
        return 48;
    }

    @Override
    public void read(DataReader in) throws IOException {
        planenum = in.readInt();
        children[0] = in.readInt();
        children[1] = in.readInt();
        mins = Vector3f.read(in);
        maxs = Vector3f.read(in);       
        fstface = (int)in.readUnsignedInt();
        numface = (int)in.readUnsignedInt();
        area = in.readShort();
        in.readUnsignedShort(); // padding
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(planenum);
        out.writeInt(children[0]);
        out.writeInt(children[1]);
        Vector3f.write(out, mins);
        Vector3f.write(out, maxs);
        out.writeUnsignedInt(fstface);
        out.writeUnsignedInt(numface);
        out.writeShort(area);
        out.writeUnsignedShort(0); // padding
    }
}