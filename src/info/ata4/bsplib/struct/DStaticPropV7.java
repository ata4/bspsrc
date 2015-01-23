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
import info.ata4.util.EnumConverter;
import java.io.IOException;

/**
 * Newer V7 structure found in Source 2013.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DStaticPropV7 extends DStaticPropV6 {
    
    public int lightmapResolutionX;
    public int lightmapResolutionY;
    
    @Override
    public int getSize() {
        return super.getSize() + 8; // 72
    }
    
    @Override
    public void read(DataInputReader in) throws IOException {
        super.read(in);
        flags = EnumConverter.fromInteger(StaticPropFlag.class, in.readInt());
        lightmapResolutionX = in.readUnsignedShort();
        lightmapResolutionY = in.readUnsignedShort();
    }
    
    @Override
    public void write(DataOutputWriter out) throws IOException {
        super.write(out);
        out.writeInt(EnumConverter.toInteger(flags));
        out.writeShort(lightmapResolutionX);
        out.writeShort(lightmapResolutionY);
    }
}
