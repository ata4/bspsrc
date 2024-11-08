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

    private static final Logger L = LogManager.getLogger();

    public int defaultAppId = SourceAppId.UNKNOWN;
    public BrushMode brushMode = BrushMode.BRUSHPLANES;
    public SourceFormat sourceFormat = SourceFormat.AUTO;
    public boolean debug = false;
    
    // entity options
    public boolean writeAreaportals = true;
    public boolean writeBrushEntities = true;
    public boolean writeCubemaps = true;
    public boolean writeDetails = true;
    public boolean writeOccluders = true;
    public boolean writeOverlays = true;
    public boolean writePointEntities = true;
    public boolean writeStaticProps = true;
    public boolean writeLadders = true;
    public boolean writeVisClusters = true;
    public boolean fixEntityRot = true;
    public boolean apForceManualMapping = false;
    public boolean detailMerge = false;
    public float detailMergeThresh = 1;
    public int maxCubemapSides = 8;
    public int maxOverlaySides = 64;
    
    // brush options
    public boolean writeWorldBrushes = true;
    public boolean writeDisp = true;
    public float backfaceDepth = 1;
    
    // texture options
    public String backfaceTexture = "";
    public String faceTexture = "";
    public boolean fixCubemapTextures = true;
    public boolean fixToolTextures = true;
    
    // miscellaneous options
    public boolean nullOutput = false;
    public boolean loadLumpFiles = true;
    public boolean skipProt = false;
    public boolean writeVisgroups = true;
    public boolean writeCameras = true;
    public boolean unpackEmbedded = false;
    public boolean smartUnpack = true;

    public BspSourceConfig() {}

    /**
     * Copy constructor
     */
    public BspSourceConfig(BspSourceConfig config) {
        this.defaultAppId = config.defaultAppId;
        this.brushMode = config.brushMode;
        this.sourceFormat = config.sourceFormat;
        this.debug = config.debug;
        
        this.writeAreaportals = config.writeAreaportals;
        this.writeBrushEntities = config.writeBrushEntities;
        this.writeCubemaps = config.writeCubemaps;
        this.writeDetails = config.writeDetails;
        this.writeOccluders = config.writeOccluders;
        this.writeOverlays = config.writeOverlays;
        this.writePointEntities = config.writePointEntities;
        this.writeStaticProps = config.writeStaticProps;
        this.writeLadders = config.writeLadders;
        this.writeVisClusters = config.writeVisClusters;
        this.fixEntityRot = config.fixEntityRot;
        this.apForceManualMapping = config.apForceManualMapping;
        this.detailMerge = config.detailMerge;
        this.detailMergeThresh = config.detailMergeThresh;
        this.maxCubemapSides = config.maxCubemapSides;
        this.maxOverlaySides = config.maxOverlaySides;
        
        this.writeWorldBrushes = config.writeWorldBrushes;
        this.writeDisp = config.writeDisp;
        this.backfaceDepth = config.backfaceDepth;
        
        this.backfaceTexture = config.backfaceTexture;
        this.faceTexture = config.faceTexture;
        this.fixCubemapTextures = config.fixCubemapTextures;
        this.fixToolTextures = config.fixToolTextures;
        
        this.nullOutput = config.nullOutput;
        this.loadLumpFiles = config.loadLumpFiles;
        this.skipProt = config.skipProt;
        this.writeVisgroups = config.writeVisgroups;
        this.writeCameras = config.writeCameras;
        this.unpackEmbedded = config.unpackEmbedded;
        this.smartUnpack = config.smartUnpack;
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
