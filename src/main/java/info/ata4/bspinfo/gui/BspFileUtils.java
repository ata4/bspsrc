/*
 ** 2012 June 7
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspinfo.gui;

import info.ata4.bsplib.BspFile;
import info.ata4.bsplib.lump.GameLump;
import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.log.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFileUtils {
    
    private static final Logger L = LogUtils.getLogger();
    
    private BspFileUtils() {
    }
    
    static void extractLump(BspFile bspFile, File destDir, LumpType type) throws IOException {
        FileUtils.forceMkdir(destDir);

        List<Lump> lumps = bspFile.getLumps();

        for (Lump lump : lumps) {
            if (type != null && lump.getType() != type) {
                continue;
            }

            String fileName = String.format("%02d_%s.bin", lump.getIndex(),
                    lump.getName());
            File lumpFile = new File(destDir, fileName);

            L.log(Level.INFO, "Extracting {0}", lump);

            try {
                InputStream is = lump.getInputStream();
                FileUtils.copyInputStreamToFile(is, lumpFile);
            } catch (IOException ex) {
                throw new BspFileException("Can't extract lump", ex);
            }
        }
    }
        
    public static void extractLumps(BspFile bspFile, File destDir) throws IOException {
        extractLump(bspFile, destDir, null);
    }
    
    static void extractGameLump(BspFile bspFile, File destDir, String type) throws IOException {
        FileUtils.forceMkdir(destDir);

        List<GameLump> gameLumps = bspFile.getGameLumps();

        for (GameLump gameLump : gameLumps) {
            if (type != null && !gameLump.getName().equalsIgnoreCase(type)) {
                continue;
            }
            
            String fileName = String.format("%s_v%d.bin", gameLump.getName(), gameLump.getVersion());
            File lumpFile = new File(destDir, fileName);

            L.log(Level.INFO, "Extracting {0}", gameLump);

            try {
                InputStream is = gameLump.getInputStream();
                FileUtils.copyInputStreamToFile(is, lumpFile);
            } catch (IOException ex) {
                throw new BspFileException("Can't extract lump", ex);
            }
        }
    }
    
    public static void extractGameLumps(BspFile bspFile, File destDir) throws IOException {
        extractGameLump(bspFile, destDir, null);
    }
}
