/*
 ** 2011 November 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.lib.struct;

import info.ata4.bspsrc.lib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;

import java.io.IOException;

/**
 * DDispInfo structure variant for BSP v23 maps.
 * TODO: could be Dota 2 only?
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDispInfoBSP23 extends DDispInfo {

    protected int unknown1;
    protected int unknown2;

    @Override
    public int getSize() {
        return 184;
    }

    @Override
    public void read(DataReader in) throws IOException {
        startPos = Vector3f.read(in);
        dispVertStart = in.readInt();
        dispTriStart = in.readInt();
        power = in.readInt();
        minTess = in.readInt();
        smoothingAngle = in.readFloat();
        contents = in.readInt();
        unknown1 = in.readInt();
        mapFace = in.readUnsignedShort();
        in.readUnsignedShort(); // padding
        lightmapAlphaStart = in.readInt();
        lightmapSamplePositionStart = in.readInt();
        unknown2 = in.readInt();
        in.readBytes(neighbors);

        for (int i = 0; i < allowedVerts.length; i++) {
            allowedVerts[i] = in.readInt();
        }
    }

    @Override
    public void write(DataWriter out) throws IOException {
        Vector3f.write(out, startPos);
        out.writeInt(dispVertStart);
        out.writeInt(dispTriStart);
        out.writeInt(power);
        out.writeInt(minTess);
        out.writeFloat(smoothingAngle);
        out.writeInt(contents);
        out.writeInt(unknown1);
        out.writeUnsignedShort(mapFace);
        out.writeUnsignedShort(0); // padding
        out.writeInt(lightmapAlphaStart);
        out.writeInt(lightmapSamplePositionStart);
        out.writeInt(unknown2);
        out.writeBytes(neighbors);

        for (int i = 0; i < allowedVerts.length; i++) {
            out.writeInt(allowedVerts[i]);
        }
    }
}
