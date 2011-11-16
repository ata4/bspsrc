/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.lump;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

/**
 * Simple file filter for lump files (.lmp).
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpFileFilter implements FileFilter {

    private Pattern lumpPattern;
    private int highestLumpIndex = -1;

    public LumpFileFilter(File bspFile) {
        String bspName = FilenameUtils.removeExtension(bspFile.getName());
        lumpPattern = Pattern.compile(bspName + "_l_([0-9]+)\\.lmp");
    }

    @Override
    public boolean accept(File file) {
        if (!file.isFile()) {
            return false;
        }

        Matcher m = lumpPattern.matcher(file.getName());

        if (m.matches()) {
            int lumpIndex = Integer.valueOf(m.group(1));

            if (lumpIndex > highestLumpIndex) {
                highestLumpIndex = lumpIndex;
            }
        }

        return m.matches();
    }

    /**
     * Returns the highest lump file index.
     *
     * @return lump file index
     */
    public int getHighestLumpIndex() {
        return highestLumpIndex;
    }
}
