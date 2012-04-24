/*
** 2012 April 24
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.tools;

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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

/**
 * BSP lump splitting tool.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspLumpSplit {
    
    private static final Logger L = Logger.getLogger(BspLumpSplit.class.getName());
    
    public static void main(String[] args) {
        LogUtils.configure();
        
        Options opts = new Options();
        
        if (args.length == 0) {
            System.out.println("BSP lump splitter v1.0");
            new HelpFormatter().printHelp("bsplumpsplit <file> [file...]", opts);
            return;
        }
        
        for (String arg : args) {
            File file = new File(arg);
            BspFile bspFile = new BspFile();
            
            try {
                bspFile.load(file);
            } catch (Exception ex) {
                L.log(Level.SEVERE, "Can't load BSP file", ex);
                continue;
            }
            
            String bspName = bspFile.getName();
            File baseDir = bspFile.getFile().getParentFile();
            File lumpsDir = new File(baseDir, bspName + "_lumps");
            lumpsDir.mkdir();

            List<Lump> lumps = bspFile.getLumps();

            for (Lump lump : lumps) {
                try {
                    if (lump.getType() == LumpType.LUMP_UNKNOWN) {
                        continue;
                    }

                    String fileName = String.format("%02d_%s_v%d.bin", lump.getIndex(),
                            lump.getName(), lump.getVersion());
                    File lumpFile = new File(lumpsDir, fileName);

                    L.log(Level.INFO, "Extracting {0}", lump);

                    InputStream is = lump.getInputStream();
                    FileUtils.copyInputStreamToFile(is, lumpFile);
                } catch (IOException ex) {
                    L.log(Level.SEVERE, "Can't extract lump", ex);
                }
            }

            File gameLumpsDir = new File(lumpsDir, "game");
            gameLumpsDir.mkdir();

            List<GameLump> gameLumps = bspFile.getGameLumps();

            for (GameLump lump : gameLumps) {
                try {
                    String fileName = String.format("%s_v%d.bin", lump.getName(), lump.getVersion());
                    File lumpFile = new File(gameLumpsDir, fileName);

                    L.log(Level.INFO, "Extracting {0}", lump);

                    InputStream is = lump.getInputStream();
                    FileUtils.copyInputStreamToFile(is, lumpFile);
                } catch (IOException ex) {
                    L.log(Level.SEVERE, "Can't extract lump", ex);
                }
            }
        }
    }
}
