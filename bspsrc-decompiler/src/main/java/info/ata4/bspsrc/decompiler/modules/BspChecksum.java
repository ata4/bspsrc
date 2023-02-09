/*
 ** 2012 May 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.decompiler.modules;

import info.ata4.bspsrc.lib.BspFileReader;
import info.ata4.bspsrc.lib.lump.Lump;
import info.ata4.bspsrc.lib.lump.LumpType;

import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.CRC32;

/**
 * BSP checksum calculator based on Source's server map CRC check.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspChecksum extends ModuleRead {

    public BspChecksum(BspFileReader reader) {
        super(reader);
    }

    public long getMapCRC() {
        CRC32 crc = new CRC32();

        // CRC across all lumps except for the Entities lump
        for (Lump lump : bspFile.getLumps()) {
            if (lump.getType() == LumpType.LUMP_ENTITIES) {
                continue;
            }

            crc.update(lump.getBuffer());
        }

        return crc.getValue();
    }

    public long getFileCRC() throws IOException {
        var bytes = Files.readAllBytes(bspFile.getFile());

        var crc = new CRC32();
        crc.update(bytes);
        return crc.getValue();
    }
}
