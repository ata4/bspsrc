/*
 ** 2012 Juli 9
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.info.gui.models;

import info.ata4.bspsrc.app.util.ListTableModel;
import info.ata4.bspsrc.lib.BspFile;
import info.ata4.bspsrc.lib.lump.GameLump;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class GameLumpTableModel extends ListTableModel {

    public GameLumpTableModel() {
        super(4);
        columnNames = Arrays.asList(new String[]{ "Name", "Size", "Size usage", "Version"});
        columnClasses = new Class[] {String.class, Integer.class, Integer.class, Integer.class};
    }

    public GameLumpTableModel(BspFile bspFile) {
        this();

        List<GameLump> lumps = bspFile.getGameLumps();

        float lumpSize = 0;

        for (GameLump l : lumps) {
            lumpSize += l.getLength();
        }

        for (GameLump l : lumps) {
            List<Object> row = new ArrayList<>();
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
