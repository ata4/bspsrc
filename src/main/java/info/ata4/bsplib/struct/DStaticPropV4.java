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

import info.ata4.bsplib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
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
    public void read(DataReader in) throws IOException {
        origin = Vector3f.read(in);
        angles = Vector3f.read(in);
        propType = in.readUnsignedShort();
        firstLeaf = in.readUnsignedShort();
        leafCount = in.readUnsignedShort();
        solid = in.readUnsignedByte();
        flags = EnumConverter.fromInteger(StaticPropFlag.class, in.readUnsignedByte());
        skin = in.readInt();
        fademin = in.readFloat();
        fademax = in.readFloat();
        lightingOrigin = Vector3f.read(in);
    }

    @Override
    public void write(DataWriter out) throws IOException {
        Vector3f.write(out, origin);
        Vector3f.write(out, angles);
        out.writeUnsignedShort(propType);
        out.writeUnsignedShort(firstLeaf);
        out.writeUnsignedShort(leafCount);
        out.writeUnsignedByte(solid);
        out.writeUnsignedByte(EnumConverter.toInteger(flags));
        out.writeInt(skin);
        out.writeFloat(fademin);
        out.writeFloat(fademax);
        Vector3f.write(out, lightingOrigin);
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
    
    public boolean hasNoPerTexelLighting() {
        return flags.contains(StaticPropFlag.STATIC_PROP_NO_PER_TEXEL_LIGHTING);
    }
    
    public boolean hasIgnoreNormals() {
        return flags.contains(StaticPropFlag.STATIC_PROP_IGNORE_NORMALS);
    }
}
