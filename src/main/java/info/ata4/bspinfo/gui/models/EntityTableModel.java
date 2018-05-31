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

import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.entity.Entity;
import info.ata4.util.gui.ListTableModel;
import java.util.*;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityTableModel extends ListTableModel {

    public EntityTableModel() {
        super(2);
        columnNames = Arrays.asList(new String[]{"Class", "Entities"});
        columnClasses = new Class[] {String.class, Integer.class};
    }

    public EntityTableModel(BspFileReader bspReader) {
        this();

        Set<String> classes = bspReader.getEntityClassSet();
        List<Entity> entities = bspReader.getData().entities;
        List<String> entityStrings = new ArrayList<>();

        // create non-unique list of all entity classes
        for (Entity ent : entities) {
            entityStrings.add(ent.getClassName());
        }

        // create rows and count occurrences of all unique entity classes
        for (String cls : classes) {
            List<Object> row = new ArrayList<>();
            row.add(cls);
            row.add(Collections.frequency(entityStrings, cls));
            addRow(row);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
