/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.lib;

import info.ata4.bspsrc.common.util.PathUtil;

import java.io.File;
import java.io.FileFilter;

/**
 * Simple file filter for BSP files (.bsp)
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFileFilter implements FileFilter {

    @Override
    public boolean accept(File pathname) {
        return PathUtil.extension(pathname.toPath())
                .orElse("")
                .equalsIgnoreCase("bsp");
    }
}
