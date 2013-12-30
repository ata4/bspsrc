/*
 ** 2012 June 2
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.modules;

import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.bsplib.struct.LevelFlag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspCompileParams extends ModuleRead {
    
    private static final Logger L = Logger.getLogger(BspCompileParams.class.getName());
    
    private List<String> vbspParams = new ArrayList<>();
    private List<String> vvisParams = new ArrayList<>();
    private List<String> vradParams = new ArrayList<>();
    
    private boolean vvisRun = false;
    private boolean vradRun = false;

    public BspCompileParams(BspFileReader reader) {
        super(reader);
        
        reader.loadFlags();

        boolean stale = false;
        boolean hasVhv = false;

        try (ZipArchiveInputStream zis = bspFile.getPakFile().getArchiveInputStream()) {
            ZipArchiveEntry ze;
            while ((ze = zis.getNextZipEntry()) != null) {
                // check for stale.txt, which marks possibly screwed up maps
                if (ze.getName().equals("stale.txt")) {
                    stale = true;
                }
                
                // check for .vhv files, which contain the vertex lighting data
                if (ze.getName().endsWith(".vhv")) {
                    hasVhv = true;
                }

                if (stale && hasVhv) {
                    // we're done here
                    break;
                }
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn''t read pakfile", ex);
        }

        // both parameters produce marked files, there's probably no way to
        // distinguish them
        if (stale) {
            vbspParams.add("-onlyents/-keepstalezip");
        }

        // check for an empty visibility lump
        vvisRun = bspFile.getLump(LumpType.LUMP_VISIBILITY).getLength() > 0;
        boolean sprpLight = false;

        // also check the map flags, if available
        if (bsp.mapFlags != null) {
            sprpLight = bsp.mapFlags.contains(LevelFlag.LVLFLAGS_BAKED_STATIC_PROP_LIGHTING_HDR)
                    || bsp.mapFlags.contains(LevelFlag.LVLFLAGS_BAKED_STATIC_PROP_LIGHTING_NONHDR);
        }

        // check both lighting lumps if they're non-empty
        boolean ldr = bspFile.getLump(LumpType.LUMP_LIGHTING).getLength() > 0;
        boolean hdr = bspFile.getLump(LumpType.LUMP_LIGHTING_HDR).getLength() > 0;
        vradRun = ldr || hdr;

        if (vradRun) {
            if (ldr && hdr) {
                vradParams.add("-both");
            } else if (ldr) {
                vradParams.add("-ldr");
            } else if (hdr) {
                vradParams.add("-hdr");
            } else {
                // no lighting?
            }

            if (sprpLight || hasVhv) {
                vradParams.add("-StaticPropLighting");
            }
        }
    }

    public List<String> getVbspParams() {
        return Collections.unmodifiableList(vbspParams);
    }

    public List<String> getVvisParams() {
        return Collections.unmodifiableList(vvisParams);
    }

    public List<String> getVradParams() {
        return Collections.unmodifiableList(vradParams);
    }

    public boolean isVvisRun() {
        return vvisRun;
    }

    public boolean isVradRun() {
        return vradRun;
    }
}
