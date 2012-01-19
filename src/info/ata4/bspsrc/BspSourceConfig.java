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

import info.ata4.bsplib.appid.AppID;
import info.ata4.log.ConsoleFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration class for BSPSource and its modules.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class BspSourceConfig {

    // logger
    private static final Logger L = Logger.getLogger(BspSourceConfig.class.getName());
    
    private BspSourceProperties props = new BspSourceProperties();
    private Set<BspFileEntry> files = new HashSet<BspFileEntry>();

    public BspSourceConfig() {
        updateDebugState(isDebug());
    }
    
    private void updateDebugState(boolean debug) {
        Logger rootLogger = Logger.getLogger("info.ata4");

        // max out logger verbosity in debug mode
        rootLogger.setLevel(debug ? Level.ALL : Level.INFO);
        
        // enable/disable stack trace in log formatter
        ConsoleFormatter.setPrintStackTrace(debug);
        
        if (debug) {
            L.fine("Debug mode on, verbosity set to maximum");
        }
    }
    
    public void lock() {
        props.setLocked(true);
    }
    
    public void unlock() {
        props.setLocked(false);
    }
    
    public Set<BspFileEntry> getFiles() {
        return files;
    }
    
    public boolean isWriteEntities() {
        return isWriteBrushEntities() && isWritePointEntities();
    }

    public void setWriteEntities(boolean writeEntities) {
        setWriteBrushEntities(writeEntities);
        setWritePointEntities(writeEntities);
    }
    
    public boolean isBrushMode() {
        return getBrushMode() == BrushMode.BRUSHPLANES;
    }

    public void setBrushMode(BrushMode brushMode) {
        props.setProperty("brushMode", brushMode.name());
    }

    public BrushMode getBrushMode() {
        return BrushMode.valueOf(props.getProperty("brushMode", BrushMode.BRUSHPLANES.name()));
    }

    public boolean isFixCubemapTextures() {
        return props.getPropertyBoolean("fixCubemapTextures", true);
    }

    public void setFixCubemapTexture(boolean fixCubemapTextures) {
        props.setPropertyBoolean("fixCubemapTextures", fixCubemapTextures);
    }

    public boolean isWriteStaticProps() {
        return props.getPropertyBoolean("writeStaticProps", true);
    }

    public void setWriteStaticProps(boolean writeStaticProps) {
        props.setPropertyBoolean("writeStaticProps", writeStaticProps);
    }

    public boolean isWriteOverlays() {
        return props.getPropertyBoolean("writeOverlays", true);
    }

    public void setWriteOverlays(boolean writeOverlays) {
        props.setPropertyBoolean("writeOverlays", writeOverlays);
    }

    public boolean isWriteDisplacements() {
        return props.getPropertyBoolean("writeDisplacements", true);
    }

    public void setWriteDisplacements(boolean writeDisp) {
        props.setPropertyBoolean("writeDisplacements", writeDisp);
    }

    public boolean isWriteCubemaps() {
        return props.getPropertyBoolean("writeCubemaps", true);
    }

    public void setWriteCubemaps(boolean writeCubemaps) {
        props.setPropertyBoolean("writeCubemaps", writeCubemaps);
    }

    public String getBackfaceTexture() {
        return props.getProperty("backfaceTexture", "");
    }

    public void setBackfaceTexture(String backfaceTexture) {
        props.setProperty("backfaceTexture", backfaceTexture);
    }

    public String getFaceTexture() {
        return props.getProperty("faceTexture", "");
    }

    public void setFaceTexture(String faceTexture) {
        props.setProperty("faceTexture", faceTexture);
    }

    public boolean isWriteAreaportals() {
        return props.getPropertyBoolean("writeAreaportals", true);
    }

    public void setWriteAreaportals(boolean writeAreaportals) {
        props.setPropertyBoolean("writeAreaportals", writeAreaportals);
    }
    
    public boolean isWriteOccluders() {
        return props.getPropertyBoolean("writeOccluders", true);
    }

    public void setWriteOccluders(boolean writeOccluders) {
        props.setPropertyBoolean("writeOccluders", writeOccluders);
    }

    public boolean isWriteDetails() {
        return props.getPropertyBoolean("writeDetails", true);
    }

    public void setWriteDetails(boolean writeDetails) {
        props.setPropertyBoolean("writeDetails", writeDetails);
    }

    public float getBackfaceDepth() {
        return props.getPropertyFloat("backfaceDepth", 1);
    }

    public void setBackfaceDepth(float depth) {
        props.setPropertyFloat("backfaceDepth", depth);
    }
    
    public boolean isSkipProtection() {
        return props.getPropertyBoolean("skipProt", false);
    }

    public void setSkipProtection(boolean skipProt) {
        props.setPropertyBoolean("skipProt", skipProt);
    }

    public boolean isLoadLumpFiles() {
        return props.getPropertyBoolean("loadLumpFiles", true);
    }

    public void setLoadLumpFiles(boolean loadLumpFiles) {
        props.setPropertyBoolean("loadLumpFiles", loadLumpFiles);
    }

    public boolean isFixEntityRotation() {
        return props.getPropertyBoolean("fixEntityRotation", true);
    }

    public void setFixEntityRotation(boolean fixEntityRot) {
        props.setPropertyBoolean("fixEntityRotation", fixEntityRot);
    }
    
    public AppID getDefaultAppID() {
        return AppID.valueOf(props.getProperty("defaultAppID", AppID.UNKNOWN.name()));
    }

    public void setDefaultAppID(AppID defaultAppID) {
        props.setProperty("defaultAppID", defaultAppID.name());
    }
    
    public void setDefaultAppID(String appID) {
        AppID appid;

        try {
            appid = AppID.valueOf(appID.toUpperCase());
        } catch (IllegalArgumentException ex) {
            appid = AppID.valueOf(Integer.valueOf(appID));
        }
        
        setDefaultAppID(appid);
    }

    public boolean isDebug() {
        return props.getPropertyBoolean("debug", false);
    }

    public void setDebug(boolean debug) {
        updateDebugState(debug);
        props.setPropertyBoolean("debug", debug);
    }

    public boolean isWriteWorldBrushes() {
        return props.getPropertyBoolean("writeWorldBrushes", true);
    }

    public void setWriteWorldBrushes(boolean writeWorldBrushes) {
        props.setPropertyBoolean("writeWorldBrushes", writeWorldBrushes);
    }

    public boolean isFixToolTextures() {
        return props.getPropertyBoolean("fixToolTextures", true);
    }

    public void setFixToolTextures(boolean fixToolTextures) {
        props.setPropertyBoolean("fixToolTextures", fixToolTextures);
    }

    public boolean isNullOutput() {
        return props.getPropertyBoolean("nullOutput", false);
    }

    public void setNullOutput(boolean nullOutput) {
        props.setPropertyBoolean("nullOutput", nullOutput);
    }

    public boolean isWriteCameras() {
        return props.getPropertyBoolean("writeCameras", true);
    }

    public void setWriteCameras(boolean writeCameras) {
        props.setPropertyBoolean("writeCameras", writeCameras);
    }

    public boolean isWriteVisgroups() {
        return props.getPropertyBoolean("writeVisgroups", true);
    }

    public void setWriteVisgroups(boolean writeVisgroups) {
        props.setPropertyBoolean("writeVisgroups", writeVisgroups);
    }

    public boolean isWriteBrushEntities() {
        return props.getPropertyBoolean("writeBrushEntities", true);
    }

    public void setWriteBrushEntities(boolean writeBrushEntities) {
        props.setPropertyBoolean("writeBrushEntities", writeBrushEntities);
    }

    public boolean isWritePointEntities() {
        return props.getPropertyBoolean("writePointEntities", true);
    }

    public void setWritePointEntities(boolean writePointEntities) {
        props.setPropertyBoolean("writePointEntities", writePointEntities);
    }

    public boolean isExtractEmbedded() {
        return props.getPropertyBoolean("extractEmbeddedFiles", false);
    }

    public void setExtractEmbedded(boolean unpackEmbedded) {
        props.setPropertyBoolean("extractEmbeddedFiles", unpackEmbedded);
    }
    
    public void setSourceFormat(SourceFormat sourceFormat) {
        props.setProperty("sourceFormat", sourceFormat.name());
    }

    public SourceFormat getSourceFormat() {
        return SourceFormat.valueOf(props.getProperty("sourceFormat", SourceFormat.AUTO.name()));
    }
}
