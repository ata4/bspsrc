/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc;

import info.ata4.bsplib.BspFile;
import info.ata4.bsplib.BspFileReader;
import info.ata4.bspsrc.modules.BspDecompiler;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Main control class for all decompiling modules.
 * 
 * <i>"A simple decompiler for HL2 bsp files"</i>
 * 
 * Original class name: unmap.Vmex
 * Original author: Rof
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSource implements Runnable {
    
    private static final Logger L = Logger.getLogger(BspSource.class.getName());

    public static final String VERSION = "1.3.3";
    
    private final BspSourceConfig config;
    
    public BspSource(BspSourceConfig config) {
        this.config = config;
    }

    /**
     * Starts BSPSource
     */
    public void run() {
        // some benchmarking
        long startTime = System.currentTimeMillis();

        // acquire list of files
        Set<BspFileEntry> entries = config.getFileSet();

        if (entries.isEmpty()) {
            L.severe("No BSP files found");
            return;
        } else {
            for (BspFileEntry entry : entries) {
                try {
                    decompile(entry);
                    System.gc(); // try to free some resources
                } catch (Exception ex) {
                    // likely to be a critical error, but maybe it will work
                    // with other files
                    L.log(Level.SEVERE, "Decompiling error", ex);
                }
            }

            // get total execution time
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            L.log(Level.INFO, "Processed {0} file(s) in {1} seconds",
                    new Object[]{entries.size(), String.format("%.4f", duration)});
        }
    }

    /**
     * Starts the decompiling process
     */
    private void decompile(BspFileEntry entry) {
        File bspFile = entry.getBspFile();
        File vmfFile = entry.getVmfFile();
        
        // load BSP
        BspFileReader reader;

        try {
            BspFile bsp = new BspFile();
            bsp.setAppID(config.defaultAppID);
            bsp.load(bspFile);
            
            if (config.loadLumpFiles) {
                bsp.loadLumpFiles();
            }
            
            // extract embedded files
            if (config.unpackEmbedded) {
                try {
                    bsp.getPakFile().extract(entry.getPakDir());
                } catch (IOException ex) {
                    L.log(Level.WARNING, "Can't extract embedded files", ex);
                }
            }
            
            reader = new BspFileReader(bsp);
            reader.loadAll();
            
            L.log(Level.INFO, "Loaded {0}", bspFile);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't load " + bspFile, ex);
            return;
        }

        if (!config.isDebug()) {
            L.log(Level.INFO, "BSP version: {0}", reader.getBspFile().getVersion());
        }
        
        // create VMF
        VmfWriter writer;
        
        try {
            // write to file or omit output?
            if (config.nullOutput) {
                writer = new VmfWriter(new NullOutputStream());
            } else {
                writer = new VmfWriter(vmfFile);
            }
            L.log(Level.INFO, "Opened {0}", vmfFile);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't write " + vmfFile, ex);
            return;
        }
        
        try {
            // create and configure decompiler, then start decompiling
            BspDecompiler decompiler = new BspDecompiler(config, reader, writer);
            decompiler.setComment("Decompiled by BSPSource v" + VERSION + " from " + bspFile.getName());
            decompiler.start();
        } finally {
            // "Cave Johnson, we're done here."
            IOUtils.closeQuietly(writer);
            L.log(Level.INFO, "Closed {0}", vmfFile);
            L.log(Level.INFO, "Finished {0}", bspFile);
        }
    }

    public BspSourceConfig getConfig() {
        return config;
    }
}
