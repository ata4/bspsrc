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
import info.ata4.bsplib.PakFile;
import info.ata4.bsplib.app.SourceAppID;
import info.ata4.bsplib.nmo.NmoException;
import info.ata4.bsplib.nmo.NmoFile;
import info.ata4.bspsrc.modules.BspDecompiler;
import info.ata4.log.LogUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger L = LogUtils.getLogger();

    public static final String VERSION = "1.4.0";

    private final BspSourceConfig config;

    public BspSource(BspSourceConfig config) {
        this.config = config;
    }

    /**
     * Starts BSPSource
     */
    @Override
    public void run() {
        // some benchmarking
        long startTime = System.currentTimeMillis();

        // log all config fields in debug mode
        if (config.isDebug()) {
            config.dumpToLog();
        }

        // acquire list of files
        Set<BspFileEntry> entries = config.getFileSet();

        if (entries.isEmpty()) {
            L.severe("No BSP files found");
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

        // Only used for 'No More Room in Hell'
        File nmoFile = entry.getNmoFile();
        File nmosFile = entry.getNmosFile();

        // load BSP
        BspFileReader reader;

        L.log(Level.INFO, "Loading {0}", bspFile);

        try {
            BspFile bsp = new BspFile();
            bsp.setSourceApp(config.defaultApp);
            bsp.load(bspFile.toPath());

            if (config.loadLumpFiles) {
                bsp.loadLumpFiles();
            }

            // extract embedded files
            if (config.unpackEmbedded) {
                try {
                    bsp.getPakFile().unpack(
                            entry.getPakDir().toPath(),
                            fileName -> !config.smartUnpack || !PakFile.isVBSPGeneratedFile(bsp.getName(), fileName)
                    );
                } catch (IOException ex) {
                    L.log(Level.WARNING, "Can't extract embedded files", ex);
                }
            }

            reader = new BspFileReader(bsp);
            reader.loadAll();
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't load " + bspFile, ex);
            return;
        }

        // load NMO if game is 'No More Room in Hell'
        NmoFile nmo = null;
        if (reader.getBspFile().getSourceApp().getAppID() == SourceAppID.NO_MORE_ROOM_IN_HELL) {
            if (nmoFile.exists()) {
                try {
                    nmo = new NmoFile();
                    nmo.load(nmoFile.toPath(), true);

	                // write nmos
	                try {
		                nmo.writeAsNmos(nmosFile.toPath());
	                } catch (IOException ex) {
		                L.log(Level.SEVERE, "Error while writing nmos", ex);
	                }
                } catch (IOException | NmoException ex) {
                    L.log(Level.SEVERE, "Can't load " + nmoFile, ex);
                    nmo = null;
                }
            } else {
                L.warning("Missing .nmo file! If the bsp is for the objective game mode, its objectives will be missing");
            }
        }

        if (!config.isDebug()) {
            L.log(Level.INFO, "BSP version: {0}", reader.getBspFile().getVersion());
            L.log(Level.INFO, "Game: {0}", reader.getBspFile().getSourceApp());
        }

        // create and configure decompiler and start decompiling
        try (VmfWriter writer = getVmfWriter(vmfFile)) {
            BspDecompiler decompiler = new BspDecompiler(reader, writer, config);

            if (nmo != null)
                decompiler.setNmoData(nmo);

            decompiler.start();
            L.log(Level.INFO, "Finished decompiling {0}", bspFile);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't decompile " + bspFile + " to " + vmfFile, ex);
        }
    }

    private VmfWriter getVmfWriter(File vmfFile) throws IOException {
        // write to file or omit output?
        if (config.nullOutput) {
            return new VmfWriter(new NullOutputStream());
        } else {
            return new VmfWriter(vmfFile);
        }
    }

    public BspSourceConfig getConfig() {
        return config;
    }
}
