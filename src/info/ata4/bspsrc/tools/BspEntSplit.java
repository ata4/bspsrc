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
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.io.EntityInputStream;
import info.ata4.bsplib.io.EntityOutputStream;
import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpFile;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.util.log.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

/**
 * BSP entity splitting tool.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspEntSplit {

    private static final Logger L = Logger.getLogger(BspEntSplit.class.getName());

    public static void main(String[] args) {
        LogUtils.configure("normal");

        Options opts = new Options();
        opts.addOption(new Option("p", "Remove *all* entity data from the BSP, including the worldspawn. Note: This will crash clients who try to play the resulting map locally!"));

        if (args.length == 0) {
            System.out.println("BSP entity splitter v1.0");
            System.out.println("Splits a BSP file into an entity-free BSP file and an entity lump file.");
            System.out.println("Both the .bsp and .lmp file are required for servers to play the map,");
            System.out.println("but clients require only the entity-free .bsp file to connect to it.");
            System.out.println("Useful to hide sensitive entity data from clients, e.g. combination locks");
            System.out.println("or entities for proprietary server mods.");
            System.out.println();
            new HelpFormatter().printHelp("bspentsplit [options] <file> [file...]", opts);
            return;
        }

        boolean purge;

        // parse arguments
        try {
            CommandLine cl = new PosixParser().parse(opts, args);

            purge = cl.hasOption("p");

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
            
            // create a backup
            File bakFile = new File(arg + ".bak");
            if (!file.renameTo(bakFile)) {
                L.log(Level.SEVERE, "Can't create backup file, splitting cancelled");
                continue;
            }
            
            BspFile bspFile = new BspFile();

            try {
                bspFile.load(bakFile);
            } catch (Exception ex) {
                L.log(Level.SEVERE, "Can't load BSP file", ex);
                continue;
            }

            Lump entLump = bspFile.getLump(LumpType.LUMP_ENTITIES);

            // extract entities to a lump file
            try {
                File lmpFile = bspFile.getNextLumpFile();
                
                // temp hack for the ugly load/save method
                lmpFile = new File(lmpFile.getPath().replace(".bsp", ""));
                
                LumpFile lumpFile = new LumpFile(bspFile);
                lumpFile.setLump(entLump);
                lumpFile.save(lmpFile);
                L.log(Level.INFO, "Created lump file {0}", lmpFile);
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Can't extract entity lump", ex);
                continue;
            }

            // should the BSP file be really useless for the client?
            if (purge) {
                // "Cry some more!"
                entLump.setBuffer(ByteBuffer.allocate(1));
            } else {
                // read the worldspawn
                EntityInputStream eis = null;
                Entity worldspawn;

                try {
                    eis = new EntityInputStream(entLump.getInputStream());
                    worldspawn = eis.readEntity();
                } catch (IOException ex) {
                    L.log(Level.SEVERE, "Can't read worldspawn", ex);
                    continue;
                } finally {
                    IOUtils.closeQuietly(eis);
                }

                // write it back to the BSP without anything else
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                EntityOutputStream eos = null;

                try {
                    eos = new EntityOutputStream(baos);
                    eos.writeEntity(worldspawn);
                    eos.write(0);
                } catch (IOException ex) {
                    L.log(Level.SEVERE, "Can't write worldspawn", ex);
                    continue;
                } finally {
                    IOUtils.closeQuietly(eos);
                }

                entLump.setBuffer(ByteBuffer.wrap(baos.toByteArray()));
            }

            // now write the changed BSP
            try {
                bspFile.save(file);
            } catch (IOException ex) {
                L.log(Level.SEVERE, "Can't write BSP file", ex);
            }
        }
    }
}
