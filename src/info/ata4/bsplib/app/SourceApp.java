/*
 ** 2012 Februar 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.app;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Source engine application identifier.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SourceApp {
    
    private static final Logger L = Logger.getLogger(SourceApp.class.getName());
    public static final SourceApp UNKNOWN = new SourceApp("Unknown", SourceAppID.UNKNOWN);
    
    private final String name;
    private final int appID;
    private int versionMin = -1;
    private int versionMax = -1;
    private String filePattern;
    private Pattern filePatternCompiled;
    private Set<String> entities = new HashSet<String>();
    private float pointsEntities = 20;
    private float pointsFilePattern = 3;
    
    public SourceApp(String name, int appID) {
        this.name = name;
        this.appID = appID;
    }
    
    float getPointsEntities() {
        return pointsEntities;
    }

    void setPointsEntities(float pointsEntities) {
        this.pointsEntities = pointsEntities;
    }

    float getPointsFilePattern() {
        return pointsFilePattern;
    }

    void setPointsFilePattern(float pointsFilePattern) {
        this.pointsFilePattern = pointsFilePattern;
    }
    
    public String getName() {
        return name;
    }

    public int getAppID() {
        return appID;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        try {
            this.filePatternCompiled = Pattern.compile(filePattern);
            this.filePattern = filePattern;
        } catch (PatternSyntaxException ex) {
            L.log(Level.WARNING, "Invalid file name pattern", ex);
        }
    }
    
    public Set<String> getEntities() {
        return entities;
    }

    public int getVersionMin() {
        return versionMin;
    }

    public void setVersionMin(int versionMin) {
        this.versionMin = versionMin;
    }

    public int getVersionMax() {
        return versionMax;
    }

    public void setVersionMax(int versionMax) {
        this.versionMax = versionMax;
    }
    
    public boolean canCheckName() {
        return filePatternCompiled != null;
    }
    
    /**
     * Returns the absolute heuristic score for a BSP file name. If the name
     * matches the pattern of this app, a score of {@link #getPointsFilePattern()}
     * will be returned.
     * 
     * @param name BSP file name to check
     * @return name match score
     */
    public float checkName(String name) {
        if (!canCheckName()) {
            throw new UnsupportedOperationException();
        }
        
        if (filePatternCompiled.matcher(name.toLowerCase()).find()) {
            L.log(Level.FINER, "File pattern match: {0} on {1}", new Object[]{filePattern, name});
            return pointsFilePattern;
        } else {
            return 0;
        }
    }
    
    /**
     * Checks if the version can be checked.
     * 
     * @return true if the version can be checked
     */
    public boolean canCheckVersion() {
        return versionMin != -1 || versionMax != -1;
    }
    
    /**
     * Checks if a BSP version number is valid for this app.
     * 
     * @param bspVersion BSP version number to check
     * @return true if the version is valid for this app
     */
    public boolean checkVersion(int bspVersion) {
        if (!canCheckVersion()) {
            throw new UnsupportedOperationException();
        }
        
        // check exact BSP version
        if (versionMin != -1 && versionMax == -1) {
            return bspVersion == versionMin;
        }
        if (versionMax != -1 && versionMin == -1) {
            return bspVersion == versionMax;
        }

        // check BSP version range
        if (bspVersion > versionMax ||
                bspVersion < versionMin) {
            return false;
        }

        return true;
    }
    
    /**
     * Checks if the entities can be checked.
     * 
     * @return true if the entities can be checked
     */
    public boolean canCheckEntities() {
        return entities != null && !entities.isEmpty();
    }

    /**
     * Returns the absolute heuristic score for a set of entity class names.
     * The more entity classes are found for this app, the higher is the resulting
     * score. The maximum score can be set with {@link #setPointsEntities(float)}.
     * 
     * @param classNames
     * @return entity match score
     */
    public float checkEntities(Set<String> classNames) {
        if (!canCheckEntities()) {
            throw new UnsupportedOperationException();
        }
        
        int matches = 0;
        
        for (String className : classNames) {
            if (entities.contains(className)) {
                L.log(Level.FINER, "Entity match: {0}", className);
                matches++;
            }
        }

        // weight the points by entity matches relative to all entity entries
        return (matches / (float) entities.size()) * pointsEntities;
    }

    @Override
    public String toString() {
        return name;
    }
}
