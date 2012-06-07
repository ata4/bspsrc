/*
 ** 2012 June 7
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspinfo.gui;

import info.ata4.bsplib.BspFile;
import info.ata4.util.gui.ListTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EmbeddedTableModel extends ListTableModel {
    
    private static final Logger L = Logger.getLogger(EmbeddedTableModel.class.getName());

    public EmbeddedTableModel() {
        super(3);
        columnNames = Arrays.asList(new String[]{"Name", "Size"});
        columnClasses = new Class[] {String.class, Long.class};
    }

    public EmbeddedTableModel(BspFile bspFile) {
        this();
        
        ZipArchiveInputStream zis = null;
        int files = 0;

        try {
            zis = bspFile.getPakFile().getArchiveInputStream();

            try {
                for (ZipArchiveEntry ze; (ze = zis.getNextZipEntry()) != null; files++) {
                    List<Object> row = new ArrayList<Object>();
                    row.add(ze.getName());
                    row.add(ze.getSize());
                    addRow(row);
                }
            } catch (IOException ex) {
                L.log(Level.WARNING, "Can't read pak");
            }
        } finally {
            IOUtils.closeQuietly(zis);
        }
    }
}
