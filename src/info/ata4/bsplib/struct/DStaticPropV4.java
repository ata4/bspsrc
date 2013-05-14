/*
 ** 2011 September 26
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.struct;

import info.ata4.bsplib.lump.LumpIO;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.util.EnumConverter;
import java.io.IOException;
import java.util.Set;

/**
 * Static prop data structure for version 4.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV4 implements DStaticProp {
    
    public Vector3f origin;
    public Vector3f angles;
    public int propType;
    public int firstLeaf;
    public int leafCount;
    public int solid;
    public Set<StaticPropFlag> flags;
    public int skin;
    public float fademin;
    public float fademax;
    public Vector3f lightingOrigin;

    @Override
    public int getSize() {
        return 56;
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
        skin = lio.readInt();
        fademin = lio.readFloat();
        fademax = lio.readFloat();
        lightingOrigin = lio.readVector3f();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeVector3f(origin);
        lio.writeVector3f(angles);
        lio.writeShort(propType);
        lio.writeShort(firstLeaf);
        lio.writeShort(leafCount);
        lio.writeByte(solid);
        lio.writeByte(EnumConverter.toInteger(flags));
        lio.writeInt(skin);
        lio.writeFloat(fademin);
        lio.writeFloat(fademax);
        lio.writeVector3f(lightingOrigin);
    }
    
    public boolean usesLightingOrigin() {
        return flags.contains(StaticPropFlag.STATIC_PROP_USE_LIGHTING_ORIGIN);
    }

    public boolean hasNoShadowing() {
        return flags.contains(StaticPropFlag.STATIC_PROP_NO_SHADOW);
    }
    
    public boolean hasNoSelfShadowing() {
        return flags.contains(StaticPropFlag.STATIC_PROP_NO_SELF_SHADOWING);
    }

    public boolean hasNoPerVertexLighting() {
        return flags.contains(StaticPropFlag.STATIC_PROP_NO_PER_VERTEX_LIGHTING);
    }
    
    public boolean hasIgnoreNormals() {
        return flags.contains(StaticPropFlag.STATIC_PROP_IGNORE_NORMALS);
    }
}
