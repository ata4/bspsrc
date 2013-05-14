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

import info.ata4.bsplib.lump.LumpIO;
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
    public void read(LumpIO lio) throws IOException {
        origin = lio.readVector3f();
        angles = lio.readVector3f();
        propType = lio.readUnsignedShort();
        firstLeaf = lio.readUnsignedShort();
        leafCount = lio.readUnsignedShort();
        solid = lio.readUnsignedByte();
        flags = EnumConverter.fromInteger(StaticPropFlag.class, lio.readUnsignedByte());
        lio.skipBytes(4);
        skin = lio.readInt();
        fademin = lio.readFloat();
        fademax = lio.readFloat();
//        lightingOrigin = lio.readVector3f();
        lio.skipBytes(12); // invalid lighting origin vector?
        forcedFadeScale = lio.readFloat();
        minCPULevel = lio.readByte();
        maxCPULevel = lio.readByte();
        minGPULevel = lio.readByte();
        maxGPULevel = lio.readByte();
        lio.skipBytes(1);
        diffuseModulation = lio.readColor32();
        lio.skipBytes(3);
    }
    
    @Override
    public boolean usesLightingOrigin() {
        // workaround for the invalid lighting origin vector
        return false;
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        throw new UnsupportedOperationException();
    }
}
