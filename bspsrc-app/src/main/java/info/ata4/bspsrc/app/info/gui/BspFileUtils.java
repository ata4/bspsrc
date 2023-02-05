/*
 ** 2012 June 7
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.info.gui;

import info.ata4.bspsrc.lib.BspFile;
import info.ata4.bspsrc.lib.lump.AbstractLump;
import info.ata4.bspsrc.lib.lump.GameLump;
import info.ata4.bspsrc.lib.lump.Lump;
import info.ata4.bspsrc.lib.lump.LumpType;
import info.ata4.log.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFileUtils {

    private static final Logger L = LogUtils.getLogger();

    private BspFileUtils() {
    }

    public static void extractAbstractLump(AbstractLump lump, Path filePath) throws IOException {
        Files.createDirectories(filePath.getParent());

        L.log(Level.INFO, "Extracting {0}", lump);

        try (FileChannel fout = FileChannel.open(
                filePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {
            ByteBuffer buffer = lump.getBuffer();
            while (buffer.hasRemaining()) {
                fout.write(buffer);
            }
        } catch (IOException ex) {
            throw new BspFileException("Can't extract lump", ex);
        }
    }


    public static void extractLump(Lump lump, Path destDir) throws IOException {
        String fileName = String.format("%02d_%s.bin", lump.getIndex(), lump.getName());
        Path filePath = destDir.resolve(fileName);

        extractAbstractLump(lump, filePath);
    }

    public static void extractLump(BspFile bspFile, LumpType type, Path destDir) throws IOException {
        Lump lump = bspFile.getLump(type);
        if (lump == null)
            return;

        extractLump(lump, destDir);
    }

    public static void extractLumps(BspFile bspFile, Path destDir) throws IOException {
        for (Lump lump : bspFile.getLumps()) {
            extractLump(lump, destDir);
        }
    }


    public static void extractGameLump(GameLump lump, Path destDir) throws IOException {
        String fileName = String.format("%s_v%d.bin", lump.getName(), lump.getVersion());
        Path filePath = destDir.resolve(fileName);

        extractAbstractLump(lump, filePath);
    }

    public static void extractGameLump(BspFile bspFile, String type, Path destDir) throws IOException {
        GameLump lump = bspFile.getGameLump(type);
        if (lump == null)
            return;

        extractGameLump(lump, destDir);
    }

    public static void extractGameLumps(BspFile bspFile, Path destDir) throws IOException {
        for (GameLump lump : bspFile.getGameLumps()) {
            extractGameLump(lump, destDir);
        }
    }
}
