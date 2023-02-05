/*
 ** 2011 September 2
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.gui;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FileExtensionFilter extends FileFilter {

    private final FileNameExtensionFilter parent;

    public FileExtensionFilter(String description, String... extensions) {
        parent = new FileNameExtensionFilter(description, extensions);
    }

    @Override
    public boolean accept(File f) {
        return parent.accept(f);
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(parent.getDescription());

        String[] exts = parent.getExtensions();

        if (exts.length == 0) {
            return sb.toString();
        }

        sb.append(" (");

        for (String ext : exts) {
            sb.append("*.");
            sb.append(ext);
            sb.append(';');
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');

        return sb.toString();
    }
}
