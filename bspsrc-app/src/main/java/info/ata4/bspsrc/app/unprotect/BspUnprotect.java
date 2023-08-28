/*
 ** 2013 July 3 3
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.bspsrc.app.unprotect;

import info.ata4.bspsrc.lib.BspFile;
import info.ata4.bspsrc.lib.exceptions.BspException;
import info.ata4.bspsrc.lib.lump.Lump;
import info.ata4.bspsrc.lib.lump.LumpFile;
import info.ata4.bspsrc.lib.lump.LumpType;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * BSPProtect map decrypter.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspUnprotect {

    public static final String VERSION = "1.0";
    public static final String BSPPROTECT_FILE = "entities.dat";
    public static final String BSPPROTECT_KEY = "EhZT36ErlQlZpLm7";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("BspUnprotect " + VERSION);
            System.out.println("Usage: bspunprotect.jar <BSP file> [key]");
            System.exit(0);
        }

        Path file = Paths.get(args[0]);
        byte[] key = args.length >= 2 ? args[1].getBytes() : BSPPROTECT_KEY.getBytes();

        try {
            BspUnprotect unprot = new BspUnprotect();
            unprot.setKey(key);
            unprot.decrypt(file);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    private BspFile bspFile;
    private byte[] key;

    public void setKey(byte[] key) {
        if (key.length % 8 != 0) {
            throw new IllegalArgumentException("Invalid key length, must be multiple of 8");
        }

        this.key = key;
    }

    public byte[] getKey() {
        return key;
    }

    public void decrypt(Path file) {
        System.out.println("Loading BSP file " + file.getFileName());

        try {
            bspFile = new BspFile();
            bspFile.load(file);
        } catch (BspException | IOException ex) {
            throw new RuntimeException("Couldn't load BSP file", ex);
        }

        System.out.println("Reading pakfile lump");

        byte[] encEnt = readEncryptedEntities();

        if (encEnt == null) {
            throw new RuntimeException("This map wasn't protected by BSPProtect");
        }

        System.out.println("Restoring entities");
        Lump entLump = bspFile.getLump(LumpType.LUMP_ENTITIES);

        int capacity = encEnt.length;

        if (entLump.getLength() > 0) {
            capacity += entLump.getLength();
        }

        ByteBuffer entBuf = ByteBuffer.allocateDirect(capacity);
        entBuf.order(bspFile.getByteOrder());

        // copy the worldspawn into the new entity lump
        if (entLump.getLength() > 0) {
            ByteBuffer entBufOld = entLump.getBuffer();
            entBufOld.limit(entBufOld.limit() - 1); // decrease limit to skip NUL
            entBuf.put(entBufOld);
        }

        // write decypted entity data into the new buffer
        try {
            InputStream is = new ByteArrayInputStream(encEnt);

            // init ICE cipher
            IceKey ice = new IceKey(key.length / 8 - 1);
            ice.set(key);

            final int blockSize = ice.blockSize();

            byte[] cipher = new byte[blockSize];
            byte[] plain = new byte[blockSize];

            for (int read = 0; read != -1; read = is.read(cipher)) {
                // decrypt block
                ice.decrypt(cipher, plain);

                // the last block is not encrypted if not equal to block size
                entBuf.put(read == blockSize ? plain : cipher, 0, read);
            }

            // NUL terminator
            entBuf.put((byte) 0);
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't decrypt entity data", ex);
        }

        entBuf.flip();

        System.out.println("Writing lump file");

        // write lump file
        try {
            Lump entLumpNew = new Lump(LumpType.LUMP_ENTITIES);
            entLumpNew.setBuffer(entBuf);

            LumpFile lump = new LumpFile(bspFile);
            lump.setLump(entLumpNew);
            lump.save(bspFile.getNextLumpFile());
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't write decrypted entity lump file", ex);
        }
    }

    private byte[] readEncryptedEntities() {
        try (ZipFile zip = bspFile.getPakFile().getZipFile()) {
            Iterator<ZipArchiveEntry> iterator = zip.getEntries(BSPPROTECT_FILE).iterator();
            if (iterator.hasNext()) {
                return zip.getInputStream(iterator.next()).readAllBytes();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't read pakfile", ex);
        }

        return null;
    }
}
