/*
 ** 2012 May 28
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

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
        for (Lump lump : bspFile.getLumps()) {
            if (lump.getType() == LumpType.LUMP_ENTITIES) {
                continue;
            }

            try (InputStream in = new CheckedInputStream(lump.getInputStream(), crc)) {
                // copy to /dev/null, we need the checksum only
                IOUtils.copy(in, NullOutputStream.NULL_OUTPUT_STREAM);
            }
        }

        return crc.getValue();
    }

    public long getFileCRC() throws IOException {
        return FileUtils.checksumCRC32(bspFile.getFile().toFile());
    }
}
