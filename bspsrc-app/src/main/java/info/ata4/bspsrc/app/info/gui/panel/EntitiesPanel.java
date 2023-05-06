package info.ata4.bspsrc.app.info.gui.panel;

import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.GuiUtil;
import info.ata4.bspsrc.app.util.ReadonlyListTableModel;
import info.ata4.bspsrc.app.util.ReadonlyListTableModel.Column;
import info.ata4.bspsrc.lib.entity.Entity;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static info.ata4.bspsrc.app.info.gui.Util.createDisplayTxtField;
import static info.ata4.bspsrc.app.util.GuiUtil.setColumnWidth;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class EntitiesPanel extends JPanel {

	public final JTextField txtPoint = createDisplayTxtField(2);
	public final JTextField txtBrush = createDisplayTxtField(2);
	public final JTextField txtTotal = createDisplayTxtField(2);
	public final JTable tblEntities = new JTable();
	public final ReadonlyListTableModel<EntityInfo> tableModel = new ReadonlyListTableModel<>(List.of(
			new Column<>("Class", String.class, EntityInfo::className),
			new Column<>("Count", Long.class, EntityInfo::occurrences)
	));

	public EntitiesPanel() {
		setLayout(new BorderLayout());

		var pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnlHeader.add(new JLabel("Point"));
		pnlHeader.add(txtPoint);
		pnlHeader.add(new JLabel("Brush"));
		pnlHeader.add(txtBrush);
		pnlHeader.add(new JLabel("Total"));
		pnlHeader.add(txtTotal);
		add(pnlHeader, BorderLayout.NORTH);

		tblEntities.setModel(tableModel);
		tblEntities.setAutoCreateRowSorter(true);

		setColumnWidth(tblEntities, 0, "-".repeat(20), false);
		setColumnWidth(tblEntities, 1, 100, true);

		var scrlTable = new JScrollPane(tblEntities, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		add(scrlTable, BorderLayout.CENTER);
	}

	public void update(BspInfoModel model) {
		var bspData = model.getBspData().orElse(null);
		if (bspData != null) {
			long brushEntsCount = bspData.entities.stream()
					.filter(entity -> entity.getModelNum() > 0)
					.count();
			long pointEntsCount = bspData.entities.size() - brushEntsCount;

			txtPoint.setText("%,d".formatted(pointEntsCount));
			txtBrush.setText("%,d".formatted(brushEntsCount));
			txtTotal.setText("%,d".formatted(bspData.entities.size()));

			tableModel.setData(bspData.entities.stream()
					.collect(Collectors.groupingBy(Entity::getClassName, Collectors.counting()))
					.entrySet()
					.stream()
					.map(entry -> new EntityInfo(entry.getKey(), entry.getValue()))
					.toList());
		} else {
			txtPoint.setText("");
			txtBrush.setText("");
			txtTotal.setText("");

			tableModel.setData(List.of());
		}
	}

	public record EntityInfo(String className, long occurrences) {}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new EntitiesPanel());
	}
}
