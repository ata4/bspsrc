/*
 ** 2012 Februar 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.struct;

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
import info.ata4.bsplib.util.EnumConverter;
import java.io.IOException;

/**
 * DStaticProp variant for Dear Esther
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropDE extends DStaticPropV8 {
    
    private int unknown1;
    private byte unknown2;
    private byte[] unknown3 = new byte[3];
    
    @Override
    public int getSize() {
        return super.getSize() + 8;
    }

    @Override
    public void read(LumpDataInput li) throws IOException {
        origin = li.readVector3f();
        angles = li.readVector3f();
        propType = li.readUnsignedShort();
        firstLeaf = li.readUnsignedShort();
        leafCount = li.readUnsignedShort();
        solid = li.readUnsignedByte();
        flags = EnumConverter.fromInteger(StaticPropFlag.class, li.readUnsignedByte());
        unknown1 = li.readInt();
        skin = li.readInt();
        fademin = li.readFloat();
        fademax = li.readFloat();
        lightingOrigin = li.readVector3f();
        forcedFadeScale = li.readFloat();
        minCPULevel = li.readByte();
        maxCPULevel = li.readByte();
        minGPULevel = li.readByte();
        maxGPULevel = li.readByte();
        unknown2 = li.readByte();
        diffuseModulation = li.readColor32();
        li.readFully(unknown3);
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        throw new UnsupportedOperationException();
    }
}
