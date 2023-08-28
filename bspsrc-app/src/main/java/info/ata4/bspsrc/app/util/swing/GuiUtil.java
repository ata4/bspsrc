package info.ata4.bspsrc.app.util.swing;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;
import info.ata4.bspsrc.app.util.swing.ui.CustomFlatLabelUI;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GuiUtil {

	public static void debugDisplay(Supplier<Container> containerSupplier) {
		GuiUtil.setupFlatlaf();

		SwingUtilities.invokeLater(() -> {
			var frame = new JFrame();
			frame.setContentPane(containerSupplier.get());
			frame.pack();
			frame.setMinimumSize(frame.getSize());
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
		});
	}

	public static void setupFlatlaf() {
		System.setProperty("flatlaf.animation", "true");
		Consumer<Boolean> changeTheme = isDark -> SwingUtilities.invokeLater(() -> {
			if (isDark)
				FlatDarkLaf.setup();
			else
				FlatLightLaf.setup();

			// register custom LabelUI implementation
			UIManager.put("LabelUI", CustomFlatLabelUI.class.getName());

			FlatLaf.updateUI();
		});

		var detector = OsThemeDetector.getDetector();
		detector.registerListener(changeTheme);
		changeTheme.accept(detector.isDark());
	}

	public static void setColumnWidth(
			JTable table,
			int columnIndex,
			Object value,
			boolean alsoSetMax,
			boolean alsoSetMin
	) {
		TableColumn column = table.getColumnModel().getColumn(columnIndex);

		var headerRenderer = column.getHeaderRenderer();
		if (headerRenderer == null){
			JTableHeader tableHeader = table.getTableHeader();
			headerRenderer = tableHeader == null ? null : tableHeader.getDefaultRenderer();
		}

		int cellWidth = 0;
		if (headerRenderer != null) {
			var headerCell = headerRenderer.getTableCellRendererComponent(
					table,
					column.getHeaderValue(),
					false,
					false,
					-1,
					columnIndex
			);
			cellWidth = headerCell.getPreferredSize().width;
		}

		var rowSorter = table.getRowSorter();
		if (rowSorter != null) {
			var sortKeys = rowSorter.getSortKeys();

			for (SortOrder sortOrder : SortOrder.values())
			{
				rowSorter.setSortKeys(List.of(new RowSorter.SortKey(columnIndex, sortOrder)));

				var cell = headerRenderer.getTableCellRendererComponent(
						table,
						column.getHeaderValue(),
						false,
						false,
						-1,
						columnIndex
				);
				cellWidth = Math.max(cellWidth, cell.getPreferredSize().width);
			}

			rowSorter.setSortKeys(sortKeys);
		}

		var cell = table.getCellRenderer(-1, columnIndex)
				.getTableCellRendererComponent(
						table,
						value,
						false,
						false,
						-1,
						columnIndex
				);



		int width = Math.max(
				cell.getPreferredSize().width,
				cellWidth
		);

		column.setPreferredWidth(width);
		if (alsoSetMin)
			column.setMinWidth(width);
		if (alsoSetMax)
			column.setMaxWidth(width);
	}

	public static Stream<Component> getComponentsRecursive(Component root) {
		var components = Stream.of(root);

		if (root instanceof Container container) {
			var subComponents = Arrays.stream(container.getComponents())
					.flatMap(GuiUtil::getComponentsRecursive);

			components = Stream.concat(components, subComponents);
		}

		return components;
	}
}
