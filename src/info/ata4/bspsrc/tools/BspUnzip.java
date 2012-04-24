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
import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.log.LogUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;

/**
 * BSP embedded file extractor tool.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspUnzip {
    
    private static final Logger L = Logger.getLogger(BspUnzip.class.getName());
    
    public static void main(String[] args) {
        LogUtils.configure();
        
        Options opts = new Options();
        opts.addOption(new Option("f", "Extract embedded files directly."));
        
        if (args.length == 0) {
            System.out.println("BSP embedded files extractor v1.0");
            System.out.println("A simple tool to extract the embedded zip file of a BSP file.");
            System.out.println();
            new HelpFormatter().printHelp("bspunzip [options] <file> [file...]", opts);
            return;
        }
        
        boolean direct;
        
        // parse arguments
        try {
            CommandLine cl = new PosixParser().parse(opts, args);
            
            direct = cl.hasOption("f");
            
            args = cl.getArgs();
        } catch (ParseException ex) {
            L.log(Level.SEVERE, "Parse error", ex);
            return;
        }
        
        if (args.length == 0) {
            L.severe("No BSP file(s) specified");
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
            
            File parentFile = bspFile.getFile().getAbsoluteFile().getParentFile();
            String bspName = bspFile.getName();
            File zipFile = new File(parentFile, bspName + ".zip");

            Lump pakLump = bspFile.getLump(LumpType.LUMP_PAKFILE);

            // extract pakfile
            if (direct) {
                ZipArchiveInputStream zis = new ZipArchiveInputStream(pakLump.getInputStream());
                ZipArchiveEntry ze;

                try {
                    String filePrefix = bspName + "_pakfile" + File.separator;
                    while ((ze = zis.getNextZipEntry()) != null) {
                        File entryFile = new File(parentFile, filePrefix + ze.getName());
                        L.log(Level.INFO, "Extracting {0}", ze.getName());

                        try {
                            InputStream cszis = new CloseShieldInputStream(zis);
                            FileUtils.copyInputStreamToFile(cszis, entryFile);
                        } catch (IOException ex) {
                            L.log(Level.WARNING, "Couldn''t extract file", ex);
                        }
                    }
                } catch (IOException ex) {
                    L.log(Level.WARNING, "Couldn''t read pakfile", ex);
                } finally {
                    IOUtils.closeQuietly(zis);
                }
            } else {
                try {
                    L.log(Level.INFO, "Extracting pakfile to {0}", zipFile);
                    InputStream is = pakLump.getInputStream();
                    FileUtils.copyInputStreamToFile(is, zipFile);
                } catch (Exception ex) {
                    L.log(Level.SEVERE, "Can't extract the pakfile", ex);
                }
            }
        }
    }
}
