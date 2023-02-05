/*
 ** 2012 June 2
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspinfo.gui.models;

import info.ata4.bsplib.BspFile;
import info.ata4.bsplib.lump.Lump;
import info.ata4.util.gui.ListTableModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Table data model for lumps.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpTableModel extends ListTableModel {

    public LumpTableModel() {
        super(5);
        columnNames = Arrays.asList(new String[]{"ID", "Name", "Size", "Size usage", "Version"});
        columnClasses = new Class[] {Integer.class, String.class, Integer.class, Integer.class, Integer.class};
    }

    public LumpTableModel(BspFile bspFile) {
        this();

        List<Lump> lumps = bspFile.getLumps();

        float lumpSize = 0;

        for (Lump l : lumps) {
            lumpSize += l.getLength();
        }

        for (Lump l : lumps) {
            List<Object> row = new ArrayList<>();
            row.add(l.getIndex());
            row.add(l.getName());
            row.add(l.getLength());
            row.add(Math.round(l.getLength() / lumpSize * 100f));
            row.add(l.getVersion());
            addRow(row);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
