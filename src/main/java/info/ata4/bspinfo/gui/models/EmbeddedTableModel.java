/*
 ** 2012 June 7
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspinfo.gui.models;

import info.ata4.bsplib.BspFile;
import info.ata4.log.LogUtils;
import info.ata4.util.gui.ListTableModel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EmbeddedTableModel extends ListTableModel {

    private static final Logger L = LogUtils.getLogger();

    public EmbeddedTableModel() {
        super(3);
        columnNames = Arrays.asList(new String[]{"Name", "Size"});
        columnClasses = new Class[] {String.class, Long.class};
    }

    public EmbeddedTableModel(BspFile bspFile) {
        this();

        try (ZipFile zip = bspFile.getPakFile().getZipFile()) {
            Enumeration<ZipArchiveEntry> enumeration = zip.getEntries();
            while (enumeration.hasMoreElements()) {
                ZipArchiveEntry ze = enumeration.nextElement();
                addRow(Arrays.asList(ze.getName(), ze.getSize()));
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Can't read pak");
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
