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

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SourceAppDB {
    
    private static final Logger L = Logger.getLogger(SourceAppDB.class.getName());
    private static SourceAppDB instance;
    
    private List<SourceApp> appList = new ArrayList<SourceApp>();
    private Map<Integer, SourceApp> appMap = new HashMap<Integer, SourceApp>();
    private float score;
    
    public static SourceAppDB getInstance() {
        if (instance == null) {
            instance = new SourceAppDB();
        }

        return instance;
    }

    private SourceAppDB() {
        load(getClass().getResourceAsStream("appdb.xml"));
    }

    private SourceAppDB(InputStream is) {
        load(is);
    }
    
    private void load(InputStream is) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        
        try {
            SAXParser sp = spf.newSAXParser();
            SourceAppHandler handler = new SourceAppHandler();
            sp.parse(is, handler);
            appList = handler.getAppList();
            
            // generate ID map for validation and faster access
            for (SourceApp app : appList) {
                Integer appID = app.getAppID();
                
                // warn if we have more than one app for an ID
                if (appMap.containsKey(appID)) {
                    L.log(Level.WARNING, "Duplicate App ID {0} for \"{1}\" and \"{2}\"",
                            new Object[]{appID, appMap.get(appID), app});
                }
                
                appMap.put(appID, app);
            }
        } catch (Exception ex) {
            L.log(Level.SEVERE, "Can't load Source application database", ex);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
    
    public SourceApp fromID(int appID) {
        if (appMap == null || !appMap.containsKey(appID)) {
            return SourceApp.UNKNOWN;
        } else {
            return appMap.get(appID);
        }
    }
    
    /**
     * Tries to find the AppID though a heuristical search with the given
     * parameters.
     *
     * @param bspName BSP file name
     * @param bspVersion BSP file version
     * @param classNames Complete set of entity class names
     * @return
     */
    public SourceApp find(String bspName, int bspVersion, Set<String> classNames) {
        SourceApp candidate = SourceApp.UNKNOWN;
        score = 0;
        
        if (appList == null) {
            return candidate;
        }
        
        for (SourceApp app : appList) {
            // skip candidates with wrong version
            if (app.canCheckVersion() && !app.checkVersion(bspVersion)) {
                continue;
            }
            
            L.log(Level.FINER, "Testing {0}", app.getName());
            
            float scoreNew = 0;

            // check entity class names
            if (app.canCheckEntities()) {
                scoreNew += app.checkEntities(classNames);
            }
            
            // check BSP file name pattern
            if (app.canCheckName()) {
                scoreNew += app.checkName(bspName);
            }
            
            if (scoreNew != 0 && scoreNew > score) {
                L.log(Level.FINER, "New candidate {0} with a score of {1}", new Object[]{app.getName(), scoreNew});
                candidate = app;
                score = scoreNew;
            }
        }

        return candidate;
    }
    
    /**
     * Returns the total heuristic score for the last call of
     * {@link #find(java.lang.String, int, java.util.Set)}.
     * 
     * @return total score
     */
    public float getScore() {
        return score;
    }
    
    public List<SourceApp> getAppList() {
        return new ArrayList<SourceApp>(appList);
    }
    
//    public static void main(String[] args) throws Exception {
//        LogUtils.configure(Logger.getLogger(""), Level.OFF);
//        
//        for (String arg : args) {
//            File file = new File(arg);
//            BspFile bspFile = new BspFile();
//            bspFile.load(file);
//
//            BspFileReader bspReader = new BspFileReader(bspFile);
//            bspReader.loadEntities();
//
//            SourceAppDB appfinder = SourceAppDB.getInstance();
//            SourceApp app = appfinder.find(file.getName(), bspFile.getVersion(), bspReader.getEntityClassSet());
//            System.out.println(file.getName() + ": " + appfinder.getScore() + "\n" + app.getName());
//        }
//    }
}
