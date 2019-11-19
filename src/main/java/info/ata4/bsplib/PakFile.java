/*
 ** 2011 November 4
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib;

import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.bspsrc.modules.texture.TextureSource;
import info.ata4.log.LogUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to read BSP-embedded zip files (pakiles).
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PakFile {

    private static final Logger L = LogUtils.getLogger();

    private static Pattern vhvPattern = Pattern.compile("sp(_hdr)?_\\d+\\.vhv");
    private static Pattern cubemapVtfPattern = Pattern.compile("c(-?\\d+)_(-?\\d+)_(-?\\d+)(\\.hdr)?\\.vtf");

    private final Lump pakLump;

    public PakFile(BspFile bspFile) {
        pakLump = bspFile.getLump(LumpType.LUMP_PAKFILE);
    }

    public ZipArchiveInputStream getArchiveInputStream() {
        return new ZipArchiveInputStream(pakLump.getInputStream(), "Cp437", false);
    }

    public void unpack(Path dest) throws IOException {
        unpack(dest, false);
    }

    public void unpack(Path dest, boolean direct) throws IOException {
        if (direct) {
            L.log(Level.INFO, "Extracting pakfile to {0}", dest);

            try (InputStream is = pakLump.getInputStream()) {
                Files.copy(is, dest);
            }
        } else {
            unpack(dest, s -> true);
        }
    }

    public void unpack(Path dest, Predicate<Path> nameFilter) throws IOException {
        Files.createDirectories(dest);

        try (ZipArchiveInputStream zis = getArchiveInputStream()) {
            for (ZipArchiveEntry ze; (ze = zis.getNextZipEntry()) != null;) {
                Path zipPath;
                try {
                    zipPath = Paths.get(ze.getName());
                } catch (InvalidPathException e) {
                    L.log(Level.WARNING, "Couldn't resolve ZipArchiveEntry path", e);
                    continue;
                }

                if (!nameFilter.test(zipPath)) {
                    continue;
                }

                // create file path for zip entry and canonize it
                Path entryFile = dest.resolve(zipPath).normalize();

                // don't allow file path to exit the extraction directory
                if (!entryFile.startsWith(dest)) {
                    L.log(Level.WARNING, "Skipped {0} (path traversal attempt)", ze.getName());
                    continue;
                }

                // create missing parent directory
                if (Files.notExists(entryFile.getParent())) {
                    Files.createDirectories(entryFile.getParent());
                }

                // don't overwrite any files
                if (Files.exists(entryFile)) {
                    L.log(Level.WARNING, "Skipped {0} (exists)", ze.getName());
                    continue;
                }

                L.log(Level.INFO, "Extracting {0}", ze.getName());

                Files.copy(zis, entryFile);
            }
        }
    }

    public static Predicate<Path> nameFilter(Collection<String> names) {
        List<Path> paths = names.stream()
                .flatMap(s -> {
                    try {
                        return Stream.of(Paths.get(s));
                    } catch (InvalidPathException e) {
                        L.log(Level.WARNING, "Error converting string to path", e);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());

        return paths::contains;
    }

    public static Predicate<Path> isVBSPGeneratedFile(String bspFileName) {
        Predicate<String> isPatchedMaterial = TextureSource.isPatchedMaterial(bspFileName);

        return path -> {
            String s = path.getFileName().toString();

            return isPatchedMaterial.test(s)
                    || vhvPattern.matcher(s).matches()
                    || cubemapVtfPattern.matcher(s).matches()
                    || s.equalsIgnoreCase("cubemapdefault.vtf")
                    || s.equalsIgnoreCase("cubemapdefault.hdr.vtf");
        };
    }
}
