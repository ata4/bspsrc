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
import java.util.logging.Level;
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

    public static void create(String[] args) {
        try {
            BspSourceCli cli = new BspSourceCli();
            BspSource bspsrc = new BspSource(cli.getConfig(args));
            bspsrc.run();
        } catch (Throwable t) {
            // "Really bad!"
            L.log(Level.SEVERE, "Fatal BSPSource error", t);
        }
    }
    
    private Options optsMain = new MultiOptions();
    private Options optsEntity = new MultiOptions();
    private Options optsWorld = new MultiOptions();
    private Options optsTexture = new MultiOptions();
    private Options optsOther = new MultiOptions();
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
    private BspSourceConfig getConfig(String[] args) {
        BspSourceConfig config = new BspSourceConfig();
        
        // basic options
        Option opt_help, opt_version, opt_debug, opt_output, opt_recursive;
        optsMain.addOption(opt_help = new Option("h", "Print this help."));
        optsMain.addOption(opt_version = new Option("v", "Print version info."));
        optsMain.addOption(opt_debug = new Option("d", "Enable debug mode. Increases verbosity and adds additional data to the VMF file."));
        optsMain.addOption(opt_recursive = new Option("r", "Decompile all files found in the given directory."));
        optsMain.addOption(opt_output = OptionBuilder
            .hasArg()
            .withArgName("file")
            .withDescription("Override output path for VMF file(s). Treated as directory if multiple BSP files are provided. \ndefault: <mappath>/<mapname>_d.vmf")
            .withType(String.class)
            .create('o'));

        // entity options
        Option opt_nbents, opt_npents, opt_nprops, opt_noverl, opt_ncubem, opt_ndetails, opt_nareap, opt_noccl, opt_nrotfix;
        optsEntity.addOption(opt_npents = new Option("no_point_ents", "Don't write any point entities."));
        optsEntity.addOption(opt_nbents = new Option("no_brush_ents", "Don't write any brush entities."));
        optsEntity.addOption(opt_nprops = new Option("no_sprp", "Don't write prop_static entities."));
        optsEntity.addOption(opt_noverl = new Option("no_overlays", "Don't write info_overlay entities."));
        optsEntity.addOption(opt_ncubem = new Option("no_cubemaps", "Don't write env_cubemap entities."));
        optsEntity.addOption(opt_ndetails = new Option("no_details", "Don't write func_detail entities."));
        optsEntity.addOption(opt_nareap = new Option("no_areaportals", "Don't write func_areaportal(_window) entities."));
        optsEntity.addOption(opt_noccl = new Option("no_occluders", "Don't write func_occluder entities."));
        optsEntity.addOption(opt_nrotfix = new Option("no_rotfix", "Don't fix instance entity brush rotations for Hammer."));
        
        // world brush options
        Option opt_nbrush, opt_ndisp, opt_bmode, opt_thickn;
        optsWorld.addOption(opt_nbrush = new Option("no_brushes", "Don't write any world brushes."));
        optsWorld.addOption(opt_ndisp = new Option("no_disps", "Don't write displacement surfaces."));
        optsWorld.addOption(opt_bmode = OptionBuilder
            .hasArg()
            .withArgName("int")
            .withDescription("Brush decompiling mode: \n" +
            "0 - brushes and planes\n" +
            "1 - original faces only\n" +
            "2 - original + split faces\n" +
            "3 - split faces only\n" +
            "default: " + config.getBrushMode().ordinal())
            .create("brushmode"));
        optsWorld.addOption(opt_thickn = OptionBuilder
            .hasArg()
            .withArgName("float")
            .withDescription("Thickness of brushes created from flat faces in units.\n" +
            "default: " + config.getBackfaceDepth())
            .create("thickness"));
        
        // texture options
        Option opt_ntexfix, opt_ftex, opt_bftex;
        optsTexture.addOption(opt_ntexfix = new Option("no_texfix", "Don't fix texture names."));
        optsTexture.addOption(opt_ftex = OptionBuilder
            .hasArg()
            .withArgName("string")
            .withDescription("Replace all face textures with this one.")
            .create("facetex"));
        optsTexture.addOption(opt_bftex = OptionBuilder
            .hasArg()
            .withArgName("string")
            .withDescription("Replace all back-face textures with this one. Used in face-based decompiling modes only.")
            .create("bfacetex"));

        // other options
        Option opt_nvmf, opt_nlumpfiles, opt_nprot, opt_listappids, opt_appid, opt_nvisgrp, opt_ncams;
        optsOther.addOption(opt_nvmf = new Option("no_vmf", "Don't write any VMF files, read BSP only."));
        optsOther.addOption(opt_nlumpfiles = new Option("no_lumpfiles", "Don't load lump files (.lmp) associated with the BSP file."));
        optsOther.addOption(opt_nprot = new Option("no_prot", "Skip decompiling protection checking. Can increase speed when mass-decompiling unprotected maps."));
        optsOther.addOption(opt_listappids = new Option("appids", "List all available application IDs"));
        optsOther.addOption(opt_nvisgrp = new Option("no_visgroups", "Don't group entities from instances into visgroups."));
        optsOther.addOption(opt_ncams = new Option("no_cams", "Don't create Hammer cameras above each player spawn."));
        optsOther.addOption(opt_appid = OptionBuilder
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
            if(cl.hasOption(opt_help.getOpt())) {
                printHelp();
            }
            
            // version
            if (cl.hasOption(opt_version.getOpt())) {
                printVersion();
            }

            // list app-ids
            if (cl.hasOption(opt_listappids.getOpt())) {
                printAppIDs();
            }
            
            // main options
            config.setDebug(cl.hasOption(opt_debug.getOpt()));
            
            if (cl.hasOption(opt_output.getOpt())) {
                outputFile = new File(cl.getOptionValue(opt_output.getOpt()));
            }
            
            recursive = cl.hasOption(opt_recursive.getOpt());
            
            // entity options
            config.setWritePointEntities(!cl.hasOption(opt_npents.getOpt()));
            config.setWriteBrushEntities(!cl.hasOption(opt_nbents.getOpt()));
            config.setWriteStaticProps(!cl.hasOption(opt_nprops.getOpt()));
            config.setWriteOverlays(!cl.hasOption(opt_noverl.getOpt()));
            config.setWriteDisplacements(!cl.hasOption(opt_ndisp.getOpt()));
            config.setWriteAreaportals(!cl.hasOption(opt_nareap.getOpt()));
            config.setWriteOccluders(!cl.hasOption(opt_noccl.getOpt()));
            config.setWriteCubemaps(!cl.hasOption(opt_ncubem.getOpt()));
            config.setWriteDetails(!cl.hasOption(opt_ndetails.getOpt()));
            
            // world options
            config.setWriteWorldBrushes(!cl.hasOption(opt_nbrush.getOpt()));
            
            if (cl.hasOption(opt_bmode.getOpt())) {
                int mode = Integer.valueOf(cl.getOptionValue(opt_bmode.getOpt()));
                try {
                    config.setBrushMode(BrushMode.valueOf(mode));
                } catch(IllegalArgumentException ex) {
                    L.log(Level.SEVERE, "Invalid brush mode: {0}", mode);
                    System.exit(1);
                }
            }
            
            if (cl.hasOption(opt_thickn.getOpt())) {
                float thickness = Float.valueOf(cl.getOptionValue(opt_thickn.getOpt()));
                config.setBackfaceDepth(thickness);
            }
            
            // texture options
            config.setFixCubemapTexture(!cl.hasOption(opt_ntexfix.getOpt()));

            if (cl.hasOption(opt_ftex.getOpt())) {
                config.setFaceTexture(cl.getOptionValue(opt_ftex.getOpt()));
            }
            
            if (cl.hasOption(opt_bftex.getOpt())) {
                config.setBackfaceTexture(cl.getOptionValue(opt_bftex.getOpt()));
            }
            
            // other options
            config.setLoadLumpFiles(!cl.hasOption(opt_nlumpfiles.getOpt()));
            config.setSkipProtection(cl.hasOption(opt_nprot.getOpt()));
            config.setFixEntityRotation(!cl.hasOption(opt_nrotfix.getOpt()));
            config.setNullOutput(cl.hasOption(opt_nvmf.getOpt()));
            config.setWriteVisgroups(!cl.hasOption(opt_nvisgrp.getOpt()));
            config.setWriteCameras(!cl.hasOption(opt_ncams.getOpt()));
            
            if (cl.hasOption(opt_appid.getOpt())) {
                config.setDefaultAppID(cl.getOptionValue(opt_appid.getOpt()));
            }
        } catch (Exception ex) {
            L.severe(ex.getMessage());
            System.exit(0);
        }
        
        // get non-recognized arguments, these are the BSP input files
        String[] argsLeft = cl.getArgs();
        Set<BspFileEntry> files = config.getFiles();
        
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
