/*
 ** 2012 June 2
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.info.gui.models;

import info.ata4.bspsrc.app.util.ListTableModel;
import info.ata4.bspsrc.lib.BspFileReader;
import info.ata4.bspsrc.lib.entity.Entity;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityTableModel extends ListTableModel {

    public EntityTableModel() {
        super(2);
        columnNames = Arrays.asList("Class", "Entities");
        columnClasses = new Class[] {String.class, Integer.class};
    }

    public EntityTableModel(BspFileReader bspReader) {
        this();

        bspReader.getData().entities.stream()
                .map(Entity::getClassName)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .forEach((cls, count) -> addRow(Arrays.asList(cls, count)));
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
