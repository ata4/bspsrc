/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bspsrc.modules.geom.BrushMode;
import info.ata4.bspsrc.util.AreaportalMapper;
import info.ata4.bspsrc.util.SourceFormat;
import info.ata4.log.LogUtils;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class for BSPSource and its modules.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class BspSourceConfig implements Serializable {

    // logger
    private static final Logger L = LogUtils.getLogger();

    public SourceApp defaultApp = SourceApp.UNKNOWN;
    public BrushMode brushMode = BrushMode.BRUSHPLANES;
    public SourceFormat sourceFormat = SourceFormat.AUTO;
    public String backfaceTexture = "";
    public String faceTexture = "";
    public boolean fixCubemapTextures = true;
    public boolean fixEntityRot = true;
    public boolean fixToolTextures = true;
    public boolean loadLumpFiles = true;
    public boolean nullOutput = false;
    public boolean skipProt = false;
    public boolean unpackEmbedded = false;
    public float backfaceDepth = 1;
    public int maxCubemapSides = 8;
    public int maxOverlaySides = 64;
    public boolean detailMerge = true;
    public float detailMergeThresh = 1;
    public boolean apForceMapping = false;
    public AreaportalMapper.ApMappingMode apMappingMode = AreaportalMapper.ApMappingMode.Ordered;
    public boolean writeAreaportals = true;
    public boolean writeBrushEntities = true;
    public boolean writeCameras = true;
    public boolean writeCubemaps = true;
    public boolean writeDetails = true;
    public boolean writeDisp = true;
    public boolean writeOccluders = true;
    public boolean writeOverlays = true;
    public boolean writePointEntities = true;
    public boolean writeStaticProps = true;
    public boolean writeVisgroups = true;
    public boolean writeWorldBrushes = true;
    public boolean writeLadders = true;

    private boolean debug = false;
    private Set<BspFileEntry> files = new HashSet<>();

    private void updateLogger(boolean debug) {
        LogUtils.configure(debug ? Level.ALL : Level.INFO);
        if (debug) {
            L.fine("Debug mode on, verbosity set to maximum");
        }
    }

    public void dumpToLog() {
        dumpToLog(L);
    }

    public void dumpToLog(Logger logger) {
        Field[] fields = getClass().getDeclaredFields();

        for (Field field : fields) {
            // ignore static fields
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            try {
                logger.log(Level.CONFIG, "{0} = {1}", new Object[]{field.getName(), field.get(this)});
            } catch (Exception ex) {
                continue;
            }
        }
    }

    public Set<BspFileEntry> getFileSet() {
        return files;
    }

    public boolean isWriteEntities() {
        return writeBrushEntities || writePointEntities;
    }

    public void setWriteEntities(boolean writeEntities) {
        writeBrushEntities = writePointEntities = writeEntities;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        updateLogger(debug);
        this.debug = debug;
    }
}
