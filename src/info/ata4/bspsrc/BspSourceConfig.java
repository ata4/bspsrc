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
import java.io.Serializable;
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
    private static final Logger L = Logger.getLogger(BspSourceConfig.class.getName());
    
    public AppID defaultAppID = AppID.UNKNOWN;
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
    public boolean unpackEmbedded = true;
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
    public float backfaceDepth = 1;
    
    private boolean debug = false;
    private Set<BspFileEntry> files = new HashSet<BspFileEntry>();

    public BspSourceConfig() {
        updateDebugState();
    }
    
    private void updateDebugState() {
        this.updateDebugState(debug);
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
        updateDebugState(debug);
        this.debug = debug;
    }
}
