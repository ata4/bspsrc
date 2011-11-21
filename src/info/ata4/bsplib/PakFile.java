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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;

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
    
    public void extract(File dest) throws IOException {
        extract(dest, true);
    }

    public void extract(File dest, boolean direct) throws IOException {
        if (direct) {
            ZipArchiveInputStream zis = getArchiveInputStream();
            ZipArchiveEntry ze;

            try {
                if (!dest.exists()) {
                    dest.mkdir();
                }
                
                while ((ze = zis.getNextZipEntry()) != null) {
                    String zipName = ze.getName();
                    
                    // some maps have embedded files with absolute paths, for
                    // whatever reason...
                    zipName = zipName.replace(':', '_');
                    
                    File entryFile = new File(dest, zipName);

                    // don't overwrite any files
                    if (entryFile.exists()) {
                        L.log(Level.INFO, "Skipped {0}", ze.getName());
                        continue;
                    }
                    
                    L.log(Level.INFO, "Extracting {0}", ze.getName());

                    try {
                        InputStream cszis = new CloseShieldInputStream(zis);
                        FileUtils.copyInputStreamToFile(cszis, entryFile);
                    } catch (IOException ex) {
                        L.log(Level.WARNING, "Couldn''t extract file", ex);
                    }
                }
            } finally {
                IOUtils.closeQuietly(zis);
            }
        } else {
            try {
                L.log(Level.INFO, "Extracting pakfile to {0}", dest);
                InputStream is = pakLump.getInputStream();
                FileUtils.copyInputStreamToFile(is, dest);
            } catch (Exception ex) {
                L.log(Level.SEVERE, "Couldn't extract the pakfile", ex);
            }
        }
    }
}
