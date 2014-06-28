/*
 ** 2011 November 4
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib;

import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

/**
 * Class to read BSP-embedded zip files (pakiles).
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PakFile {
    
    private static final Logger L = Logger.getLogger(PakFile.class.getName());
    
    private final Lump pakLump;
    
    public PakFile(BspFile bspFile) {
        pakLump = bspFile.getLump(LumpType.LUMP_PAKFILE);
    }
    
    public ZipArchiveInputStream getArchiveInputStream() {
        return new ZipArchiveInputStream(pakLump.getInputStream(), "Cp437", false);
    }
    
    public void unpack(Path dest) throws IOException {
        unpack(dest, false);
    }

    public void unpack(Path dest, boolean direct) throws IOException {
        if (direct) {
            L.log(Level.INFO, "Extracting pakfile to {0}", dest);

            try (InputStream is = pakLump.getInputStream()) {
                Files.copy(is, dest);
            }
        } else {
            unpack(dest, null);
        }
    }
    
    public void unpack(Path dest, List<String> names) throws IOException {
        Files.createDirectories(dest);
        
        try (ZipArchiveInputStream zis = getArchiveInputStream()) {
            for (ZipArchiveEntry ze; (ze = zis.getNextZipEntry()) != null;) {
                String zipName = ze.getName();

                if (names != null && !names.contains(zipName)) {
                    continue;
                }

                // some maps have embedded files with absolute paths, for
                // whatever reason...
                zipName = zipName.replace(':', '_');

                Path entryFile = dest.resolve(zipName);
                
                if (Files.notExists(entryFile.getParent())) {
                    Files.createDirectories(entryFile.getParent());
                }
                
                // don't overwrite any files
                if (Files.exists(entryFile)) {
                    L.log(Level.INFO, "Skipped {0}", ze.getName());
                    continue;
                }

                L.log(Level.INFO, "Extracting {0}", ze.getName());

                Files.copy(zis, entryFile);
            }
        }
    }
}
