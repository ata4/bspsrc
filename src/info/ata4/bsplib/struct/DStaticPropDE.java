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
import info.ata4.util.EnumConverter;
import java.io.IOException;

/**
 * DStaticProp variant for Dear Esther
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropDE extends DStaticPropV8 {

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
        li.skipBytes(4);
        skin = li.readInt();
        fademin = li.readFloat();
        fademax = li.readFloat();
//        lightingOrigin = li.readVector3f();
        li.skipBytes(12); // invalid lighting origin vector?
        forcedFadeScale = li.readFloat();
        minCPULevel = li.readByte();
        maxCPULevel = li.readByte();
        minGPULevel = li.readByte();
        maxGPULevel = li.readByte();
        li.skipBytes(1);
        diffuseModulation = li.readColor32();
        li.skipBytes(3);
    }
    
    @Override
    public boolean usesLightingOrigin() {
        // workaround for the invalid lighting origin vector
        return false;
    }

    @Override
    public void write(LumpDataOutput lo) throws IOException {
        throw new UnsupportedOperationException();
    }
}
