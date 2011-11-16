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

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
import info.ata4.bsplib.util.EnumConverter;
import info.ata4.bsplib.vector.Vector3f;
import java.io.IOException;
import java.util.Set;

/**
 * Static prop data structure.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticProp implements DStruct {
    
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

    public int getSize() {
        return 68;
    }

    public void read(LumpDataInput li) throws IOException {
        origin = li.readVector3f();
        angles = li.readVector3f();
        propType = li.readUnsignedShort();
        firstLeaf = li.readUnsignedShort();
        leafCount = li.readUnsignedShort();
        solid = li.readUnsignedByte();
        flags = EnumConverter.fromInteger(StaticPropFlag.class, li.readUnsignedByte());
        skin = li.readInt();
        fademin = li.readFloat();
        fademax = li.readFloat();
        lightingOrigin = li.readVector3f();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(origin);
        lo.writeVector3f(angles);
        lo.writeShort(propType);
        lo.writeShort(firstLeaf);
        lo.writeShort(leafCount);
        lo.writeByte(solid);
        lo.writeByte(EnumConverter.toInteger(flags));
        lo.writeInt(skin);
        lo.writeFloat(fademin);
        lo.writeFloat(fademax);
        lo.writeVector3f(lightingOrigin);
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
