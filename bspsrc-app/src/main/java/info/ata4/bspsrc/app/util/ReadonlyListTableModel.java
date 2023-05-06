package info.ata4.bspsrc.app.util;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.function.Function;

public class ReadonlyListTableModel<T> extends AbstractTableModel {

	private final List<Column<T, ?>> columns;
	private List<T> data = List.of();

	public ReadonlyListTableModel(List<Column<T, ?>> columns) {
		this.columns = List.copyOf(columns);
	}

	public void setData(List<T> data) {
		this.data = List.copyOf(data);
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int column) {
		return columns.get(column).name();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columns.get(columnIndex).cls();
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return columns.get(columnIndex).getter().apply(data.get(rowIndex));
	}

	public record Column<D, T>(String name, Class<T> cls, Function<D, ? extends T> getter) {}
}
