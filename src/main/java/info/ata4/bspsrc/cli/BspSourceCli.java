/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.cli;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppDB;
import info.ata4.bspsrc.BspFileEntry;
import info.ata4.bspsrc.BspSource;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.modules.geom.BrushMode;
import info.ata4.bspsrc.util.SourceFormat;
import info.ata4.log.LogUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Helper class for CLI parsing and handling.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceCli {

    private static final Logger L = LogUtils.getLogger();

    private Option helpOpt, versionOpt, listappidsOpt, debugOpt, outputOpt, recursiveOpt, fileListOpt;
    private Options optsMain = new Options();

    private Option nbentsOpt, npentsOpt, npropsOpt, noverlOpt, ncubemOpt, ndetailsOpt, nareapOpt, nocclOpt, nladderOpt, nrotfixOpt;
    private Options optsEntity = new Options();

    private Option fAreapManualOpt, fOcclManualOpt;
    private Options optsEntityMapping = new Options();

    private Option nbrushOpt, ndispOpt, bmodeOpt, thicknOpt;
    private Options optsWorld = new Options();

    private Option ntexfixOpt, ntooltexfixOpt, ftexOpt, bftexOpt;
    private Options optsTexture = new Options();

    private Option nvmfOpt, nlumpfilesOpt, nprotOpt, appidOpt, nvisgrpOpt, ncamsOpt, formatOpt, unpackOpt, nsmartUnpackOpt;
    private Options optsOther = new Options();

    private MultiOptions optsAll = new MultiOptions();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogUtils.configure();

        try {
            BspSourceCli cli = new BspSourceCli();
            cli.run(args);
        } catch (Throwable t) {
            // "Really bad!"
            L.log(Level.SEVERE, "Fatal BSPSource error", t);
        }
    }

    public BspSourceCli() {
        initOptions();
    }

    private void initOptions() {
        BspSourceConfig config = new BspSourceConfig();

        // basic options
        optsMain.addOption(helpOpt = new Option("h", "help", false, "Print this help."));
        optsMain.addOption(versionOpt = new Option("v", "Print version info."));
        optsMain.addOption(debugOpt = new Option("d", "Enable debug mode. Increases verbosity and adds additional data to the VMF file."));
        optsMain.addOption(recursiveOpt = new Option("r", "Decompile all files found in the given directory."));
        optsMain.addOption(outputOpt = Option.builder("o")
                .hasArg()
                .argName("file")
                .desc("Override output path for VMF file(s). Treated as directory if multiple BSP files are provided. \ndefault: <mappath>/<mapname>_d.vmf")
                .build());

        optsMain.addOption(fileListOpt = Option.builder("l")
                .hasArg()
                .argName("file")
                .desc("Use a text files with paths as input BSP file list.")
                .build());

        // entity options
        optsEntity.addOption(npentsOpt = new Option("no_point_ents", "Don't write any point entities."));
        optsEntity.addOption(nbentsOpt = new Option("no_brush_ents", "Don't write any brush entities."));
        optsEntity.addOption(npropsOpt = new Option("no_sprp", "Don't write prop_static entities."));
        optsEntity.addOption(noverlOpt = new Option("no_overlays", "Don't write info_overlay entities."));
        optsEntity.addOption(ncubemOpt = new Option("no_cubemaps", "Don't write env_cubemap entities."));
        optsEntity.addOption(ndetailsOpt = new Option("no_details", "Don't write func_detail entities."));
        optsEntity.addOption(nareapOpt = new Option("no_areaportals", "Don't write func_areaportal(_window) entities."));
        optsEntity.addOption(nocclOpt = new Option("no_occluders", "Don't write func_occluder entities."));
        optsEntity.addOption(nladderOpt = new Option("no_ladders", "Don't write func_ladder entities."));
        optsEntity.addOption(nrotfixOpt = new Option("no_rotfix", "Don't fix instance entity brush rotations for Hammer."));

        // entity mapping
        optsEntityMapping.addOption(fAreapManualOpt = new Option("force_manual_areaportal",
                "Force manual entity mapping for areaportal entities"));
        optsEntityMapping.addOption(fOcclManualOpt = new Option("force_manual_occluder",
                "Force manual entitiy mapping for occluder entities"));

        // world brush options
        optsWorld.addOption(nbrushOpt = new Option("no_brushes", "Don't write any world brushes."));
        optsWorld.addOption(ndispOpt = new Option("no_disps", "Don't write displacement surfaces."));
        optsWorld.addOption(bmodeOpt = Option.builder("brushmode")
                .hasArg()
                .argName("enum")
                .desc("Brush decompiling mode:\n" +
                        BrushMode.BRUSHPLANES.name() + "   - brushes and planes\n" +
                        BrushMode.ORIGFACE.name() + "      - original faces only\n" +
                        BrushMode.ORIGFACE_PLUS.name() + " - original + split faces\n" +
                        BrushMode.SPLITFACE.name() + "     - split faces only\n" +
                        "default: " + config.brushMode.name())
                .build());
        optsWorld.addOption(thicknOpt = Option.builder("thickness")
                .hasArg()
                .argName("float")
                .desc("Thickness of brushes created from flat faces in units.\n" +
                        "default: " + config.backfaceDepth)
                .build());

        // texture options
        optsTexture.addOption(ntexfixOpt = new Option("no_texfix", "Don't fix texture names."));
        optsTexture.addOption(ntooltexfixOpt = new Option("no_tooltexfix", "Don't fix tool textures."));
        optsTexture.addOption(ftexOpt = Option.builder("facetex")
                .hasArg()
                .argName("string")
                .desc("Replace all face textures with this one.")
                .build());
        optsTexture.addOption(bftexOpt = Option.builder("bfacetex")
                .hasArg()
                .argName("string")
                .desc("Replace all back-face textures with this one. Used in face-based decompiling modes only.")
                .build());

        // other options
        optsOther.addOption(nvmfOpt = new Option("no_vmf", "Don't write any VMF files, read BSP only."));
        optsOther.addOption(nlumpfilesOpt = new Option("no_lumpfiles", "Don't load lump files (.lmp) associated with the BSP file."));
        optsOther.addOption(nprotOpt = new Option("no_prot", "Skip decompiling protection checking. Can increase speed when mass-decompiling unprotected maps."));
        optsOther.addOption(listappidsOpt = new Option("appids", "List all available application IDs"));
        optsOther.addOption(nvisgrpOpt = new Option("no_visgroups", "Don't group entities from instances into visgroups."));
        optsOther.addOption(ncamsOpt = new Option("no_cams", "Don't create Hammer cameras above each player spawn."));
        optsOther.addOption(appidOpt = Option.builder("appid")
                .hasArg()
                .argName("string/int")
                .desc("Overrides game detection by using " +
                        "this Steam Application ID instead.\n" +
                        "Use -appids to list all known app-IDs.")
                .build());
        optsOther.addOption(formatOpt = Option.builder("format")
                .hasArg()
                .argName("enum")
                .desc("Sets the VMF format used for the decompiled maps:\n" +
                        SourceFormat.AUTO.name() + " - " + SourceFormat.AUTO + "\n" +
                        SourceFormat.OLD.name() + "  - " + SourceFormat.OLD + "\n" +
                        SourceFormat.NEW.name() + "  - " + SourceFormat.NEW + "\n" +
                        "default: " + config.sourceFormat.name())
                .build());
        optsOther.addOption(unpackOpt = new Option("unpack_embedded", "Unpack embedded files in the bsp."));
        optsOther.addOption(nsmartUnpackOpt = new Option("no_smart_unpack",
                "Disable 'smart' extracting of embedded files.\n Smart extracting automatically skips all files " +
                        "generated by vbsp, that are only relevant to running the map in the engine."));

        // all options
        optsAll.addOptions(optsMain)
                .addOptions(optsEntity)
                .addOptions(optsEntityMapping)
                .addOptions(optsWorld)
                .addOptions(optsTexture)
                .addOptions(optsOther);
    }

    private void run(String[] args) throws IOException, BspSourceCliParseException, ParseException {
        if (args.length == 0) {
            printHelp();
            return;
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(optsAll, args);

        if(commandLine.hasOption(helpOpt.getOpt())) {
            printHelp();
            return;
        } else if (commandLine.hasOption(versionOpt.getOpt())) {
            printVersion();
            return;
        } else if (commandLine.hasOption(listappidsOpt.getOpt())) {
            printAppIDs();
            return;
        }

        BspSourceConfig config = getConfig(commandLine);
        if (config.getFileSet().isEmpty()) {
            L.severe("No BSP file(s) specified");
        } else {
            BspSource bspsrc = new BspSource(config);
            bspsrc.run();
        }
    }

    /**
     * Prints application usage, then exits the app.
     */
    private void printHelp() {
        System.out.println("BSPSource " + BspSource.VERSION);
        System.out.println("usage: bspsrc [options] <path> [path...]");
        System.out.println();

        OptionHelpFormatter clHelp = new OptionHelpFormatter();
        clHelp.printHelp("Main options:", optsMain);
        clHelp.printHelp("Entity options:", optsEntity);
        clHelp.printHelp("Entity mapping options:", optsEntityMapping);
        clHelp.printHelp("World brush options:", optsWorld);
        clHelp.printHelp("Texture options:", optsTexture);
        clHelp.printHelp("Other options:", optsOther);
    }

    /**
     * Prints application version, then exits the app.
     */
    private void printVersion() {
        System.out.println("BSPSource " + BspSource.VERSION);
        System.out.println();
        System.out.println("Based on VMEX v0.98g by Rof <rof@mellish.org.uk>");
        System.out.println("Extended and modified by Nico Bergemann <barracuda415@yahoo.de>");
    }

    private void printAppIDs() {
        System.out.printf("%6s  %s\n", "ID", "Name");

        List<SourceApp> apps = SourceAppDB.getInstance().getAppList();

        for (SourceApp app : apps) {
            System.out.printf("%6d  %s\n", app.getAppID(), app.getName());
        }
    }

    /**
     * Parses an arguments command line and applies all settings to BSPSource.
     *
     * @param cl Command line arguments
     */
    public BspSourceConfig getConfig(CommandLine cl) throws IOException, BspSourceCliParseException {
        BspSourceConfig config = new BspSourceConfig();

        Set<BspFileEntry> files = new HashSet<>();

        // main options
        config.setDebug(cl.hasOption(debugOpt.getOpt()));

        File outputFile;
        if (cl.hasOption(outputOpt.getOpt())) {
            outputFile = new File(cl.getOptionValue(outputOpt.getOpt()));
        } else {
            outputFile = null;
        }

        boolean recursive = cl.hasOption(recursiveOpt.getOpt());

        if (cl.hasOption(fileListOpt.getOpt())) {
            Files.readAllLines(Paths.get(cl.getOptionValue(fileListOpt.getOpt()))).stream()
                    .map(File::new)
                    .map(filePath -> new BspFileEntry(filePath, outputFile))
                    .forEach(files::add);
        }

        // entity options
        config.writePointEntities = !cl.hasOption(npentsOpt.getOpt());
        config.writeBrushEntities = !cl.hasOption(nbentsOpt.getOpt());
        config.writeStaticProps = !cl.hasOption(npropsOpt.getOpt());
        config.writeOverlays = !cl.hasOption(noverlOpt.getOpt());
        config.writeDisp = !cl.hasOption(ndispOpt.getOpt());
        config.writeAreaportals = !cl.hasOption(nareapOpt.getOpt());
        config.writeOccluders = !cl.hasOption(nocclOpt.getOpt());
        config.writeCubemaps = !cl.hasOption(ncubemOpt.getOpt());
        config.writeDetails = !cl.hasOption(ndetailsOpt.getOpt());
        config.writeLadders = !cl.hasOption(nladderOpt.getOpt());

        // entity mapping options
        config.apForceManualMapping = cl.hasOption(fAreapManualOpt.getOpt());
        config.occForceManualMapping = cl.hasOption(fOcclManualOpt.getOpt());

        // world options
        config.writeWorldBrushes = !cl.hasOption(nbrushOpt.getOpt());

        if (cl.hasOption(bmodeOpt.getOpt())) {
            String modeStr = cl.getOptionValue(bmodeOpt.getOpt());

            config.brushMode = parseEnum(BrushMode.class, modeStr)
                    .orElseThrow(() -> new BspSourceCliParseException("Invalid brush mode: " + modeStr));
        }

        if (cl.hasOption(formatOpt.getOpt())) {
            String formatStr = cl.getOptionValue(formatOpt.getOpt());

            config.sourceFormat = parseEnum(SourceFormat.class, formatStr)
                    .orElseThrow(() -> new BspSourceCliParseException("Invalid source format: " + formatStr));
        }

        if (cl.hasOption(thicknOpt.getOpt())) {
            String thicknessStr = cl.getOptionValue(thicknOpt.getOpt());
            try {
                config.backfaceDepth = Float.parseFloat(thicknessStr);
            } catch (NumberFormatException e) {
                throw new BspSourceCliParseException("Invalid thickness: " + thicknessStr);
            }
        }

        // texture options
        config.fixCubemapTextures = !cl.hasOption(ntexfixOpt.getOpt());
        config.fixToolTextures = !cl.hasOption(ntooltexfixOpt.getOpt());

        if (cl.hasOption(ftexOpt.getOpt())) {
            config.faceTexture = cl.getOptionValue(ftexOpt.getOpt());
        }

        if (cl.hasOption(bftexOpt.getOpt())) {
            config.backfaceTexture = cl.getOptionValue(bftexOpt.getOpt());
        }

        // other options
        config.loadLumpFiles = !cl.hasOption(nlumpfilesOpt.getOpt());
        config.skipProt = cl.hasOption(nprotOpt.getOpt());
        config.fixEntityRot = !cl.hasOption(nrotfixOpt.getOpt());
        config.nullOutput = cl.hasOption(nvmfOpt.getOpt());
        config.writeVisgroups = !cl.hasOption(nvisgrpOpt.getOpt());
        config.writeCameras = !cl.hasOption(ncamsOpt.getOpt());
        config.unpackEmbedded = cl.hasOption(unpackOpt.getOpt());
        config.smartUnpack = !cl.hasOption(nsmartUnpackOpt.getOpt());

        if (cl.hasOption(appidOpt.getOpt())) {
            String appidStr = cl.getOptionValue(appidOpt.getOpt()).toUpperCase();

            try {
                int appid = Integer.parseInt(appidStr);
                config.defaultApp = SourceAppDB.getInstance().fromID(appid);
            } catch (NumberFormatException e) {
                throw new BspSourceCliParseException("Invalid App-ID: " + appidStr);
            }
        }

        // get non-recognized arguments, these are the BSP input files
        String[] argsLeft = cl.getArgs();

        for (String arg : argsLeft) {
            Path path = Paths.get(arg);

            if (Files.isDirectory(path)) {
                PathMatcher bspPathMatcher = path.getFileSystem().getPathMatcher("glob:**.bsp");
                try (Stream<Path> pathStream = Files.walk(path, recursive ? Integer.MAX_VALUE : 0)) {
                    pathStream
                            .filter(Files::isRegularFile)
                            .filter(bspPathMatcher::matches)
                            .map(filePath -> new BspFileEntry(filePath.toFile(), outputFile))
                            .forEach(files::add);
                }
            } else {
                files.add(new BspFileEntry(path.toFile(), outputFile));
            }
        }

        config.addFiles(files);

        return config;
    }

    private static <E extends Enum<E>> Optional<E> parseEnum(Class<E> eClass, String value) {
        try {
            return Optional.of(Enum.valueOf(eClass, value));
        } catch (IllegalArgumentException e) {
            try {
                return Optional.of(eClass.getEnumConstants()[Integer.parseInt(value)]);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e1) {}
        }

        return Optional.empty();
    }
}
