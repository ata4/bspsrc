package info.ata4.bspsrc.lib.struct;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

public class DFaceStrataV2 extends DFace {
    
    @Override
    public int getSize() {
        return 72;
    }

    @Override
    public void read(DataReader in) throws IOException {
        pnum = (int)in.readUnsignedInt();
        side = in.readByte();
        onnode = in.readByte();
        in.readUnsignedShort(); // padding
        fstedge = in.readInt();
        numedge = in.readInt();
        texinfo = in.readInt();
        dispInfo = in.readInt();
        surfaceFogVolumeID = (int)in.readUnsignedInt();
        in.readBytes(styles);
        lightofs = in.readInt();
        area = in.readFloat();
        lightmapTextureMinsInLuxels[0] = in.readInt();
        lightmapTextureMinsInLuxels[1] = in.readInt();
        lightmapTextureSizeInLuxels[0] = in.readInt();
        lightmapTextureSizeInLuxels[1] = in.readInt();
        origFace = in.readInt();
        numPrims = (int)in.readUnsignedInt();
        firstPrimID = (int)in.readUnsignedInt();
        smoothingGroups = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeUnsignedInt(pnum);
        out.writeByte(side);
        out.writeByte(onnode);
        out.writeUnsignedShort(0); // padding
        out.writeInt(fstedge);
        out.writeInt(numedge);
        out.writeInt(texinfo);
        out.writeInt(dispInfo);
        out.writeUnsignedInt(surfaceFogVolumeID);
        out.writeBytes(styles);
        out.writeInt(lightofs);
        out.writeFloat(area);
        out.writeInt(lightmapTextureMinsInLuxels[0]);
        out.writeInt(lightmapTextureMinsInLuxels[1]);
        out.writeInt(lightmapTextureSizeInLuxels[0]);
        out.writeInt(lightmapTextureSizeInLuxels[1]);
        out.writeInt(origFace);
        out.writeUnsignedInt(numPrims);
        out.writeUnsignedInt(firstPrimID);
        out.writeInt(smoothingGroups);
    }
}