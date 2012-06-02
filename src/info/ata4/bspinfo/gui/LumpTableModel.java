/*
 ** 2012 June 2
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspinfo.gui;

import info.ata4.bsplib.BspFile;
import info.ata4.bsplib.lump.Lump;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Table data model for lumps.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpTableModel extends AbstractTableModel {
    
    private String[] columnNames = new String[]{"ID", "Name", "Size (bytes)", "Usage", "Version"};
    private Object[][] data = new Object[getRowCount()][getColumnCount()];
    
    public void update(BspFile bspFile) {
        List<Lump> lumps = bspFile.getLumps();
        
        float lumpSize = 0;
        
        for (Lump l : lumps) {
            lumpSize += l.getLength();
        }
        
        int row = 0;
        
        for (Lump l : lumps) {
            int col = 0;

            data[row][col++] = l.getIndex();
            data[row][col++] = l.getName();
            data[row][col++] = l.getLength();
            data[row][col++] = Math.round(l.getLength() / lumpSize * 100f);
            data[row][col++] = l.getVersion();

            row++;
        }
        
        fireTableDataChanged();
    }

    @Override
    public Class getColumnClass(int c) {
        Object v = getValueAt(0, c);
        return v == null ? Object.class : v.getClass();
    }

    public int getRowCount() {
        return BspFile.HEADER_LUMPS;
    }

    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        data[rowIndex][columnIndex] = value;
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
