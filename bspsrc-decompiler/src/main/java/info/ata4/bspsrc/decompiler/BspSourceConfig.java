/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.decompiler;

import info.ata4.bspsrc.decompiler.modules.geom.BrushMode;
import info.ata4.bspsrc.decompiler.util.SourceFormat;
import info.ata4.bspsrc.lib.app.SourceAppId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Configuration class for BSPSource and its modules.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class BspSourceConfig {

    // logger
    private static final Logger L = LogManager.getLogger();

    public int defaultAppId = SourceAppId.UNKNOWN;
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
    public boolean smartUnpack = true;
    public float backfaceDepth = 1;
    public int maxCubemapSides = 8;
    public int maxOverlaySides = 64;
    public boolean detailMerge = false;
    public float detailMergeThresh = 1;
    public boolean apForceManualMapping = false;
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
    public boolean writeVisClusters = true;

    public boolean debug = false;

    public BspSourceConfig() {}

    /**
     * Copy constructor
     */
    public BspSourceConfig(BspSourceConfig config) {
        this.defaultAppId = config.defaultAppId;
        this.brushMode = config.brushMode;
        this.sourceFormat = config.sourceFormat;
        this.backfaceTexture = config.backfaceTexture;
        this.faceTexture = config.faceTexture;
        this.fixCubemapTextures = config.fixCubemapTextures;
        this.fixEntityRot = config.fixEntityRot;
        this.fixToolTextures = config.fixToolTextures;
        this.loadLumpFiles = config.loadLumpFiles;
        this.nullOutput = config.nullOutput;
        this.skipProt = config.skipProt;
        this.unpackEmbedded = config.unpackEmbedded;
        this.smartUnpack = config.smartUnpack;
        this.backfaceDepth = config.backfaceDepth;
        this.maxCubemapSides = config.maxCubemapSides;
        this.maxOverlaySides = config.maxOverlaySides;
        this.detailMerge = config.detailMerge;
        this.detailMergeThresh = config.detailMergeThresh;
        this.apForceManualMapping = config.apForceManualMapping;
        this.writeAreaportals = config.writeAreaportals;
        this.writeBrushEntities = config.writeBrushEntities;
        this.writeCameras = config.writeCameras;
        this.writeCubemaps = config.writeCubemaps;
        this.writeDetails = config.writeDetails;
        this.writeDisp = config.writeDisp;
        this.writeOccluders = config.writeOccluders;
        this.writeOverlays = config.writeOverlays;
        this.writePointEntities = config.writePointEntities;
        this.writeStaticProps = config.writeStaticProps;
        this.writeVisgroups = config.writeVisgroups;
        this.writeWorldBrushes = config.writeWorldBrushes;
        this.writeLadders = config.writeLadders;
        this.writeVisClusters = config.writeVisClusters;
        this.debug = config.debug;
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
                logger.info("%s = %s".formatted(field.getName(), field.get(this)));
            } catch (IllegalAccessException e) {
                logger.warn("", e);
            }
        }
    }

    public boolean isWriteEntities() {
        return writeBrushEntities || writePointEntities;
    }

    public void setWriteEntities(boolean writeEntities) {
        writeBrushEntities = writePointEntities = writeEntities;
    }
}
