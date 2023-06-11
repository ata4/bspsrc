/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.decompiler;

import info.ata4.bspsrc.decompiler.modules.BspDecompiler;
import info.ata4.bspsrc.decompiler.modules.texture.TextureSource;
import info.ata4.bspsrc.lib.BspFile;
import info.ata4.bspsrc.lib.BspFileReader;
import info.ata4.bspsrc.lib.PakFile;
import info.ata4.bspsrc.lib.app.SourceAppDB;
import info.ata4.bspsrc.lib.app.SourceAppId;
import info.ata4.bspsrc.lib.nmo.NmoException;
import info.ata4.bspsrc.lib.nmo.NmoFile;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

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
public class BspSource {

    private static final Logger L = LogManager.getLogger();
    public static final String DECOMPILE_TASK_ID_IDENTIFIER = "decompile_id";

    public static final String VERSION = "1.4.4-DEV";

    private final BspSourceConfig config;
    private final List<BspFileEntry> entries;
    private final List<UUID> entryUuids;

    public BspSource(BspSourceConfig config, List<BspFileEntry> entries) {
        this.config = requireNonNull(config);
        this.entries = List.copyOf(entries);
        this.entryUuids = Stream.generate(UUID::randomUUID)
                .limit(entries.size())
                .toList();
    }

    /**
     * Starts BSPSource
     */
    public void run(Consumer<Signal> signalConsumer) {
        // some benchmarking
        long startTime = System.currentTimeMillis();

        // log all config fields in debug mode
        if (config.debug) {
            config.dumpToLog();
        }

        if (entries.isEmpty())
            return;

        L.info("Starting...");

        var outputQueue = new LinkedBlockingQueue<Signal>();
        try (var executorService = Executors.newWorkStealingPool()) {
            for (int i = 0; i < entries.size(); i++) {
                int finalI = i; // java....
                executorService.submit(() -> decompile(finalI, outputQueue));
            }

            try {
                int remainingTasks = entries.size();
                while (remainingTasks > 0) {
                    var signal = outputQueue.take();
                    signalConsumer.accept(signal);

                    if (signal instanceof Signal.TaskFinished || signal instanceof Signal.TaskFailed)
                        remainingTasks--;
                }
            } catch (InterruptedException e) {
                // interuppted. Set interrupt flag which causes subsequent
                // Executor.close to not wait for tasks to finish
                Thread.currentThread().interrupt();
            }
        }

        // get total execution time
        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        L.info("Processed %d file(s) in %.4f seconds".formatted(entries.size(), duration));
    }

    private void decompile(int index, BlockingQueue<Signal> outputQueue) {
        var entry = entries.get(index);
        var uuid = entryUuids.get(index);

        try (var closeable = CloseableThreadContext.put(DECOMPILE_TASK_ID_IDENTIFIER, uuid.toString())) {
            outputQueue.add(new Signal.TaskStarted(index));
            try {
                var warnings = decompile(entry);
                outputQueue.add(new Signal.TaskFinished(index, warnings));
            } catch (Throwable e) {
                outputQueue.add(new Signal.TaskFailed(index, e));
            }
        }
    }

    /**
     * Starts the decompiling process
     */
    private Set<Warning> decompile(BspFileEntry entry)
            throws BspSourceException {

        var warnings = new HashSet<Warning>();

        Path bspFile = entry.getBspFile();
        Path vmfFile = entry.getVmfFile();

        // Only used for 'No More Room in Hell'
        Path nmoFile = entry.getNmoFile();
        Path nmosFile = entry.getNmosFile();

        // load BSP
        L.info("Loading {}", bspFile);

        var bsp = new BspFile();
        bsp.setAppId(config.defaultAppId);

        try {
            bsp.load(bspFile);
        } catch (IOException ex) {
            // Log AND throw exception. Logging is for decompilation log and exception for task result
            String message = "Error loading '%s'".formatted(bspFile);
            L.error(message, ex);
            throw new BspSourceException(message, ex);
        }

        if (config.loadLumpFiles) {
            bsp.loadLumpFiles();
        }

        Predicate<String> fileFilter = filename -> !config.smartUnpack ||
                (!PakFile.isVBSPGeneratedFile(filename) && !TextureSource.isPatchedMaterial(filename));

        // extract embedded files
        if (config.unpackEmbedded) {
            try {
                bsp.getPakFile().unpack(entry.getPakDir(), fileFilter);
            } catch (IOException ex) {
                warnings.add(Warning.ExtractEmbedded);
                L.error("Can't extract embedded files", ex);
            }
        }

        var reader = new BspFileReader(bsp);
        reader.loadAll();

        // load NMO if game is 'No More Room in Hell'
        NmoFile nmo = null;
        if (reader.getBspFile().getAppId() == SourceAppId.NO_MORE_ROOM_IN_HELL) {
            if (Files.exists(nmoFile)) {
                try {
                    nmo = new NmoFile();
                    nmo.load(nmoFile, true);

	                // write nmos
	                try {
		                nmo.writeAsNmos(nmosFile);
	                } catch (IOException ex) {
                        warnings.add(Warning.WriteNmos);
		                L.error("Error while writing nmos", ex);
	                }
                } catch (IOException | NmoException ex) {
                    warnings.add(Warning.LoadNmo);
                    L.error("Can't load " + nmoFile, ex);
                    nmo = null;
                }
            } else {
                L.warn("Missing .nmo file! If the bsp is for the objective game mode, its objectives will be missing");
            }
        }

        if (!config.debug) {
            int appId = reader.getBspFile().getAppId();
            String gameName = SourceAppDB.getInstance().getName(appId)
                    .orElse(String.valueOf(appId));

            L.info("BSP version: {}", reader.getBspFile().getVersion());
            L.info("Game: {}", gameName);
        }

        // create and configure decompiler and start decompiling
        try (VmfWriter writer = getVmfWriter(vmfFile.toFile())) {
            BspDecompiler decompiler = new BspDecompiler(reader, writer, config);

            if (nmo != null)
                decompiler.setNmoData(nmo);

            decompiler.start();
            L.info("Finished decompiling {}", bspFile);
        } catch (IOException ex) {
            // Log AND throw exception. Logging is for decompilation log and exception for task result
            String message = "Error decompiling '%s' to '%s'".formatted(bspFile, vmfFile);
            L.error(message, ex);
            throw new BspSourceException(message, ex);
        }

        return warnings;
    }

    private VmfWriter getVmfWriter(File vmfFile) throws IOException {
        // write to file or omit output?
        if (config.nullOutput) {
            return new VmfWriter(OutputStream.nullOutputStream());
        } else {
            return new VmfWriter(vmfFile);
        }
    }

    public List<UUID> getEntryUuids() {
        return entryUuids;
    }

    public sealed interface Signal {
        record TaskStarted(int index) implements Signal {}
        record TaskFinished(int index, Set<Warning> warnings) implements Signal {}
        record TaskFailed(int index, Throwable exception) implements Signal {}
    }

    public enum Warning {
        ExtractEmbedded,
        LoadNmo,
        WriteNmos
    }
}
