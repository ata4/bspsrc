/*
 ** 2012 March 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.modules;

import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * BSP checksum calculator based on Source's server map CRC check.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspChecksum extends ModuleRead {

    public BspChecksum(BspFileReader reader) {
        super(reader);
    }
    
    public long getMapCRC() throws IOException {
        CRC32 crc = new CRC32();
        
        // CRC across all lumps except for the Entities lump
        for (Lump l : bspFile.getLumps()) {
            if (l.getType() == LumpType.LUMP_ENTITIES) {
                continue;
            }
            
            InputStream is = l.getInputStream();

            try {
                // read in 1K chunks
                for (byte[] buf = new byte[1024]; is.read(buf) != -1;) {
                    crc.update(buf);
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        
        return crc.getValue();
    }
    
    public long getFileCRC() throws IOException {
        return FileUtils.checksumCRC32(bspFile.getFile());
    }
}
