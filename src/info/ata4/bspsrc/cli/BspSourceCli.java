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

import info.ata4.bsplib.appid.AppID;
import info.ata4.bspsrc.BrushMode;
import info.ata4.bspsrc.BspFileEntry;
import info.ata4.bspsrc.BspSource;
import info.ata4.bspsrc.BspSourceConfig;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

/**
 * Helper class for CLI parsing and handling.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceCli {

    private static final Logger L = Logger.getLogger(BspSourceCli.class.getName());

    private Options optsMain = new Options();
    private Options optsEntity = new Options();
    private Options optsWorld = new Options();
    private Options optsTexture = new Options();
    private Options optsOther = new Options();
    private MultiOptions optsAll = new MultiOptions();

    /**
     * Prints application usage, then exits the app.
     */
    private void printHelp() {
        System.out.println("BSPSource " + BspSource.VERSION);
        System.out.println("usage: bspsrc [options] <path> [path...]");
        System.out.println();
        
        CustomHelpFormatter clHelp = new CustomHelpFormatter();
        clHelp.printHelp("Main options:", optsMain);
        clHelp.printHelp("Entity options:", optsEntity);
        clHelp.printHelp("World brush options:", optsWorld);
        clHelp.printHelp("Texture options:", optsTexture);
        clHelp.printHelp("Other options:", optsOther);
        System.exit(0);
    }

    /**
     * Prints application version, then exits the app.
     */
    private void printVersion() {
        System.out.println("BSPSource " + BspSource.VERSION);
        System.out.println();
        System.out.println("Based on VMEX v0.98g by Rof <rof@mellish.org.uk>");
        System.out.println("Extended and modified by Nico Bergemann <barracuda415@yahoo.de>");
        System.exit(0);
    }
    
    private void printAppIDs() {
        System.out.printf("%6s  %-25s %s\n", "ID", "String ID", "Name");
        
        AppID[] ids = AppID.values();
        
        for (AppID id : ids) {
            System.out.printf("%6d  %-25s %s\n", id.getID(), id.name(), id);
        }
        
        System.exit(0);
    }

    /**
     * Parses an arguments string and applies all settings to BSPSource.
     *
     * @param args arguments string
     */
    @SuppressWarnings("static-access")
    public BspSourceConfig getConfig(String[] args) {
        BspSourceConfig config = new BspSourceConfig();
        
        // basic options
        Option helpOpt, versionOpt, debugOpt, outputOpt, recursiveOpt;
        optsMain.addOption(helpOpt = new Option("h", "Print this help."));
        optsMain.addOption(versionOpt = new Option("v", "Print version info."));
        optsMain.addOption(debugOpt = new Option("d", "Enable debug mode. Increases verbosity and adds additional data to the VMF file."));
        optsMain.addOption(recursiveOpt = new Option("r", "Decompile all files found in the given directory."));
        optsMain.addOption(outputOpt = OptionBuilder
            .hasArg()
            .withArgName("file")
            .withDescription("Override output path for VMF file(s). Treated as directory if multiple BSP files are provided. \ndefault: <mappath>/<mapname>_d.vmf")
            .withType(String.class)
            .create('o'));

        // entity options
        Option nbentsOpt, npentsOpt, npropsOpt, noverlOpt, ncubemOpt, ndetailsOpt, nareapOpt, nocclOpt, nrotfixOpt;
        optsEntity.addOption(npentsOpt = new Option("no_point_ents", "Don't write any point entities."));
        optsEntity.addOption(nbentsOpt = new Option("no_brush_ents", "Don't write any brush entities."));
        optsEntity.addOption(npropsOpt = new Option("no_sprp", "Don't write prop_static entities."));
        optsEntity.addOption(noverlOpt = new Option("no_overlays", "Don't write info_overlay entities."));
        optsEntity.addOption(ncubemOpt = new Option("no_cubemaps", "Don't write env_cubemap entities."));
        optsEntity.addOption(ndetailsOpt = new Option("no_details", "Don't write func_detail entities."));
        optsEntity.addOption(nareapOpt = new Option("no_areaportals", "Don't write func_areaportal(_window) entities."));
        optsEntity.addOption(nocclOpt = new Option("no_occluders", "Don't write func_occluder entities."));
        optsEntity.addOption(nrotfixOpt = new Option("no_rotfix", "Don't fix instance entity brush rotations for Hammer."));
        
        // world brush options
        Option nbrushOpt, ndispOpt, bmodeOpt, thicknOpt;
        optsWorld.addOption(nbrushOpt = new Option("no_brushes", "Don't write any world brushes."));
        optsWorld.addOption(ndispOpt = new Option("no_disps", "Don't write displacement surfaces."));
        optsWorld.addOption(bmodeOpt = OptionBuilder
            .hasArg()
            .withArgName("enum")
            .withDescription("Brush decompiling mode: \n" +
            BrushMode.BRUSHPLANES.name() + "   - brushes and planes\n" +
            BrushMode.ORIGFACE.name() + "      - original faces only\n" +
            BrushMode.ORIGFACE_PLUS.name() + " - original + split faces\n" +
            BrushMode.SPLITFACE.name() + "     - split faces only\n" +
            "default: " + config.brushMode.name())
            .create("brushmode"));
        optsWorld.addOption(thicknOpt = OptionBuilder
            .hasArg()
            .withArgName("float")
            .withDescription("Thickness of brushes created from flat faces in units.\n" +
            "default: " + config.backfaceDepth)
            .create("thickness"));
        
        // texture options
        Option ntexfixOpt, ftexOpt, bftexOpt;
        optsTexture.addOption(ntexfixOpt = new Option("no_texfix", "Don't fix texture names."));
        optsTexture.addOption(ftexOpt = OptionBuilder
            .hasArg()
            .withArgName("string")
            .withDescription("Replace all face textures with this one.")
            .create("facetex"));
        optsTexture.addOption(bftexOpt = OptionBuilder
            .hasArg()
            .withArgName("string")
            .withDescription("Replace all back-face textures with this one. Used in face-based decompiling modes only.")
            .create("bfacetex"));

        // other options
        Option nvmfOpt, nlumpfilesOpt, nprotOpt, listappidsOpt, appidOpt, nvisgrpOpt, ncamsOpt;
        optsOther.addOption(nvmfOpt = new Option("no_vmf", "Don't write any VMF files, read BSP only."));
        optsOther.addOption(nlumpfilesOpt = new Option("no_lumpfiles", "Don't load lump files (.lmp) associated with the BSP file."));
        optsOther.addOption(nprotOpt = new Option("no_prot", "Skip decompiling protection checking. Can increase speed when mass-decompiling unprotected maps."));
        optsOther.addOption(listappidsOpt = new Option("appids", "List all available application IDs"));
        optsOther.addOption(nvisgrpOpt = new Option("no_visgroups", "Don't group entities from instances into visgroups."));
        optsOther.addOption(ncamsOpt = new Option("no_cams", "Don't create Hammer cameras above each player spawn."));
        optsOther.addOption(appidOpt = OptionBuilder
            .hasArg()
            .withArgName("string/int")
            .withDescription("Overrides game detection by using "
            + "this Steam Application ID instead.\n"
            + "Use -appids to list all known app-IDs.")
            .create("appid"));

        // all options
        optsAll.addOptions(optsMain);
        optsAll.addOptions(optsEntity);
        optsAll.addOptions(optsWorld);
        optsAll.addOptions(optsTexture);
        optsAll.addOptions(optsOther);
        
        if (args.length == 0) {
            printHelp();
            System.exit(0);
        }
        
        CommandLineParser clParser = new PosixParser();
        CommandLine cl = null;
        File outputFile = null;
        boolean recursive = false;

        try {
            // parse the command line arguments
            cl = clParser.parse(optsAll, args);

            // help
            if(cl.hasOption(helpOpt.getOpt())) {
                printHelp();
            }
            
            // version
            if (cl.hasOption(versionOpt.getOpt())) {
                printVersion();
            }

            // list app-ids
            if (cl.hasOption(listappidsOpt.getOpt())) {
                printAppIDs();
            }
            
            // main options
            config.setDebug(cl.hasOption(debugOpt.getOpt()));
            
            if (cl.hasOption(outputOpt.getOpt())) {
                outputFile = new File(cl.getOptionValue(outputOpt.getOpt()));
            }
            
            recursive = cl.hasOption(recursiveOpt.getOpt());
            
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
            
            // world options
            config.writeWorldBrushes = !cl.hasOption(nbrushOpt.getOpt());
            
            if (cl.hasOption(bmodeOpt.getOpt())) {
                String modeStr = cl.getOptionValue(bmodeOpt.getOpt());
   
                try {
                    config.brushMode = BrushMode.valueOf(modeStr);
                } catch (IllegalArgumentException ex) {
                    // try again as ordinal enum value
                    try {
                        int mode = Integer.valueOf(modeStr.toUpperCase());
                        config.brushMode = BrushMode.fromOrdinal(mode);
                    } catch (IllegalArgumentException ex2) {
                        throw new RuntimeException("Invalid brush mode");
                    }
                }
            }
            
            if (cl.hasOption(thicknOpt.getOpt())) {
                float thickness = Float.valueOf(cl.getOptionValue(thicknOpt.getOpt()));
                config.backfaceDepth = thickness;
            }
            
            // texture options
            config.fixCubemapTextures = !cl.hasOption(ntexfixOpt.getOpt());

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
            
            if (cl.hasOption(appidOpt.getOpt())) {
                String appidStr = cl.getOptionValue(appidOpt.getOpt());
   
                try {
                    config.defaultAppID = AppID.valueOf(appidStr);
                } catch (IllegalArgumentException ex) {
                    // try again as integer ID
                    try {
                        int appid = Integer.valueOf(appidStr.toUpperCase());
                        config.defaultAppID = AppID.fromID(appid);
                    } catch (IllegalArgumentException ex2) {
                        throw new RuntimeException("Invalid default AppID");
                    }
                }
            }
        } catch (Exception ex) {
            L.severe(ex.getMessage());
            System.exit(0);
        }
        
        // get non-recognized arguments, these are the BSP input files
        String[] argsLeft = cl.getArgs();
        Set<BspFileEntry> files = config.getFileSet();
        
        if (argsLeft.length == 0) {
            L.severe("No BSP file(s) specified");
            System.exit(1);
        } else if (argsLeft.length == 1 && outputFile != null) {
            // set VMF file for one BSP
            BspFileEntry entry = new BspFileEntry(new File(argsLeft[0]));
            entry.setVmfFile(outputFile);
            files.add(entry);
        } else {
            for (String arg : argsLeft) {
                File file = new File(arg);
                
                if (file.isDirectory()) {
                    Collection<File> subFiles = FileUtils.listFiles(file, new String[]{"bsp"}, recursive);
                    
                    for (File subFile : subFiles) {
                        BspFileEntry entry = new BspFileEntry(subFile);

                        // override destination directory?
                        if (outputFile != null) {
                            entry.setVmfFile(new File(outputFile, entry.getVmfFile().getName()));
                        }    

                        files.add(entry);
                    }
                } else {
                    BspFileEntry entry = new BspFileEntry(file);

                    // override destination directory?
                    if (outputFile != null) {
                        entry.setVmfFile(new File(outputFile, entry.getVmfFile().getName()));
                    }    

                    files.add(entry);
                }
            }
        }
        
        return config;
    }
}
