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

import info.ata4.bsplib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import static info.ata4.io.Seekable.Origin.CURRENT;
import info.ata4.util.EnumConverter;
import java.io.IOException;

/**
 * DStaticProp V9 variant for Dear Esther
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV9DE extends DStaticPropV8 {

    @Override
    public int getSize() {
        return super.getSize() + 8; // 76
    }

    @Override
    public void read(DataReader in) throws IOException {
        origin = Vector3f.read(in);
        angles = Vector3f.read(in);
        propType = in.readUnsignedShort();
        firstLeaf = in.readUnsignedShort();
        leafCount = in.readUnsignedShort();
        solid = in.readUnsignedByte();
        flags = EnumConverter.fromInteger(StaticPropFlag.class, in.readUnsignedByte());
        in.seek(4, CURRENT);
        skin = in.readInt();
        fademin = in.readFloat();
        fademax = in.readFloat();
//        lightingOrigin = lio.readVector3f();
        in.seek(12, CURRENT); // invalid lighting origin vector?
        forcedFadeScale = in.readFloat();
        minCPULevel = in.readByte();
        maxCPULevel = in.readByte();
        minGPULevel = in.readByte();
        maxGPULevel = in.readByte();
        in.seek(1, CURRENT);
        diffuseModulation = new Color32(in.readInt());
        in.seek(3, CURRENT);
    }
    
    @Override
    public boolean usesLightingOrigin() {
        // workaround for the invalid lighting origin vector
        return false;
    }

    @Override
    public void write(DataWriter out) throws IOException {
        throw new UnsupportedOperationException();
    }
}
