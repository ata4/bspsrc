/*
 ** 2014 October 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.struct;

import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import java.io.IOException;

/**
 * Variant of V7 with lightmap resolution fields found in newer TF2 maps.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV7TF2 extends DStaticPropV6 {
    
    public int lightmapFlags;
    public int lightmapResX;
    public int lightmapResY;
    
    @Override
    public int getSize() {
        return super.getSize() + 8;
    }
    
    @Override
    public void read(DataInputReader in) throws IOException {
        super.read(in);
        lightmapFlags = in.readInt();
        lightmapResX = in.readUnsignedShort();
        lightmapResY = in.readUnsignedShort();
    }
    
    @Override
    public void write(DataOutputWriter out) throws IOException {
        super.write(out);
        out.writeInt(lightmapFlags);
        out.writeShort(lightmapResX);
        out.writeShort(lightmapResY);
    }
}
