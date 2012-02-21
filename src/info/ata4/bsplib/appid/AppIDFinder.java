/*
** 2011 April 20
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.appid;

import info.ata4.bsplib.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * Application ID search and find utility class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AppIDFinder {
    
    private static final Logger L = Logger.getLogger(AppIDFinder.class.getName());
    private static AppIDFinder instance;

    private List<ClassNamesEntry> classNamesList = new ArrayList<ClassNamesEntry>();

    public static AppIDFinder getInstance() throws IOException {
        if (instance == null) {
            instance = new AppIDFinder();
        }

        return instance;
    }

    private AppIDFinder() throws IOException {
        loadClassNamesList(getClass().getResourceAsStream("resources/appid_classes.txt"));
    }

    private AppIDFinder(InputStream is) throws IOException {
        loadClassNamesList(is);
    }

    private AppIDFinder(File file) throws IOException {
        loadClassNamesList(FileUtils.openInputStream(file));
    }

    private void loadClassNamesList(InputStream is) throws IOException {
        LineIterator li = IOUtils.lineIterator(is, "US-ASCII");
        ClassNamesEntry entry = null;

        try {
            while (li.hasNext()) {
                String line = li.next().trim();

                // skip blank lines
                if (line.isEmpty()) {
                    continue;
                }
                
                if (line.startsWith("#")) {
                    // app-id header
                    entry = parseClassNamesEntry(line);
                    
                    if (entry != null) {
                        classNamesList.add(entry);
                    }
                    
                    continue;
                }
                
                if (entry != null) {
                    entry.classNames.add(line);
                }
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private ClassNamesEntry parseClassNamesEntry(String line) {
        line = line.substring(1); // remove #
        String[] args = StringUtils.fastSplit(line, ' ');

        if (args.length < 2) {
            L.log(Level.WARNING, "Invalid parameters: {0}", line);
            return null;
        }

        ClassNamesEntry entry = new ClassNamesEntry();

        try {
            entry.appID = AppID.valueOf(args[0]);
        } catch (IllegalArgumentException ex) {
            L.log(Level.WARNING, "Invalid AppID: {0}", ex);
            entry.appID = AppID.UNKNOWN;
            return null;
        }

        if (entry.appID == AppID.UNKNOWN) {
            L.log(Level.WARNING, "Unknown AppID: {0}", args[0]);
            return null;
        }

        try {
            entry.minBspVersion = Integer.valueOf(args[1]);

            if (args.length >= 3) {
                entry.maxBspVersion = Integer.valueOf(args[2]);
            }
        } catch (NumberFormatException ex) {
            L.log(Level.WARNING, "Invalid BSP version: {0}", ex);
            return null;
        }

        return entry;
    }

    /**
     * Tries to find the AppID though a heuristical search with the given
     * parameters.
     *
     * @param classNames Complete set of entity class names
     * @param bspVersion BSP file version
     * @return
     */
    public AppID find(Set<String> classNames, int bspVersion) {
        for (ClassNamesEntry entry : classNamesList) {
            // check BSP version
            if (!entry.checkVersion(bspVersion)) {
                L.log(Level.FINER, "Skipping {0} (version mismatch)", entry.appID);
                continue;
            }

            L.log(Level.FINER, "Testing {0}", entry.appID);

            // check entity class names
            if (entry.checkEntities(classNames)) {
                L.log(Level.FINER, "Match: {0}", entry.appID);
                return entry.appID;
            }
        }

        L.finer("No match");

        return AppID.UNKNOWN;
    }

    private class ClassNamesEntry {
        private Set<String> classNames = new TreeSet<String>();
        private AppID appID = AppID.UNKNOWN;
        private int minBspVersion = -1;
        private int maxBspVersion = -1;

        private boolean checkVersion(int bspVersion) {
            // check exact BSP version
            if (maxBspVersion == -1) {
                return bspVersion == minBspVersion;
            }

            // check BSP version range
            if (bspVersion > maxBspVersion ||
                    bspVersion < minBspVersion) {
                return false;
            }

            return true;
        }

        private boolean checkEntities(Set<String> classNamesChecked) {
            // empty lists count as instant match
            if (classNames.isEmpty()) {
                return true;
            }

            for (String className : classNamesChecked) {
                if (classNames.contains(className)) {
                    L.log(Level.FINER, "Match on {0}", className);
                    return true;
                }
            }

            return false;
        }
    }
}
