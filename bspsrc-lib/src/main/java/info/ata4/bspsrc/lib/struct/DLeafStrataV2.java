package info.ata4.bspsrc.lib.struct;

import info.ata4.bspsrc.lib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

public class DLeafStrataV2 extends DLeaf  {

    @Override
    public int getSize() {
        return 56;
    }

    @Override
    public void read(DataReader in) throws IOException {
        contents = in.readInt();
        cluster = in.readInt();
        areaFlags = in.readInt();
        mins = Vector3f.read(in);
        maxs = Vector3f.read(in);
        fstleafface = (int)in.readUnsignedInt();
        numleafface = (int)in.readUnsignedInt();
        fstleafbrush = (int)in.readUnsignedInt();
        numleafbrush = (int)in.readUnsignedInt();
        leafWaterDataID = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(contents);
        out.writeInt(cluster);
        out.writeInt(areaFlags);
        Vector3f.write(out, mins);
        Vector3f.write(out, maxs);
        out.writeUnsignedInt(fstleafface);
        out.writeUnsignedInt(numleafface);
        out.writeUnsignedInt(fstleafbrush);
        out.writeUnsignedInt(numleafbrush);
        out.writeInt(leafWaterDataID);
    }
}
