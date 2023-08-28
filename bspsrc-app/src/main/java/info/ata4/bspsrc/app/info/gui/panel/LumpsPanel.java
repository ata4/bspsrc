package info.ata4.bspsrc.app.info.gui.panel;

import info.ata4.bspsrc.app.info.gui.data.LumpInfo;
import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.app.util.swing.model.ReadonlyListTableModel;
import info.ata4.bspsrc.app.util.swing.renderer.ByteSizeCellRenderer;
import info.ata4.bspsrc.app.util.swing.renderer.ProgressCellRenderer;
import info.ata4.bspsrc.lib.lump.LumpType;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static info.ata4.bspsrc.app.util.swing.GuiUtil.setColumnWidth;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class LumpsPanel extends JPanel {

	private final JTable tblLumps = new JTable();
	private final ReadonlyListTableModel<LumpInfo> tableModel = new ReadonlyListTableModel<>(List.of(
			new ReadonlyListTableModel.Column<>("ID", Integer.class, LumpInfo::id),
			new ReadonlyListTableModel.Column<>("Name", String.class, LumpInfo::name),
			new ReadonlyListTableModel.Column<>("Size", Integer.class, LumpInfo::size),
			new ReadonlyListTableModel.Column<>("Size usage", Integer.class, LumpInfo::sizePercentage),
			new ReadonlyListTableModel.Column<>("Version", Integer.class, LumpInfo::version)
	));
	private final JButton btnExtract = new JButton("Extract");
	private final JButton btnExtractAll = new JButton("Extract all");

	public LumpsPanel(
			Consumer<Set<Integer>> onExtractLumps
	) {
		setLayout(new BorderLayout());

		tblLumps.setModel(tableModel);
		tblLumps.setAutoCreateRowSorter(true);
		tblLumps.getColumnModel().getColumn(2).setCellRenderer(new ByteSizeCellRenderer());
		tblLumps.getColumnModel().getColumn(3).setCellRenderer(new ProgressCellRenderer());

		setColumnWidth(tblLumps, 0, Arrays.stream(LumpType.values())
				.map(LumpType::getIndex)
				.max(Integer::compareTo)
				.orElseThrow(), true, false);
		setColumnWidth(tblLumps, 1, Arrays.stream(LumpType.values())
				.map(Enum::name)
				.max(Comparator.comparingInt(String::length))
				.orElseThrow(), false, false);
		setColumnWidth(tblLumps, 2, 100_100, true, false);
		setColumnWidth(tblLumps, 3, 100, true, false);
		setColumnWidth(tblLumps, 4, 10, true, false);

		tblLumps.setPreferredScrollableViewportSize(new Dimension(tblLumps.getPreferredSize().width, -1));

		var scrlTable = new JScrollPane(tblLumps, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		add(scrlTable, BorderLayout.CENTER);

		btnExtract.addActionListener(e -> {
			var lumpIndices = Arrays.stream(tblLumps.getSelectedRows())
					.map(tblLumps::convertRowIndexToModel)
					.boxed()
					.collect(Collectors.toSet());

			if (!lumpIndices.isEmpty())
				onExtractLumps.accept(lumpIndices);
		});
		btnExtractAll.addActionListener(e -> {
			var lumpIndices = IntStream.range(0, tblLumps.getModel().getRowCount())
					.boxed()
					.collect(Collectors.toSet());

			onExtractLumps.accept(lumpIndices);
		});

		var pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnlButtons.add(btnExtract);
		pnlButtons.add(btnExtractAll);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	public void update(BspInfoModel model) {
		tableModel.setData(model.getLumps());

		boolean buttonsEnabled = !model.getLumps().isEmpty();
		btnExtract.setEnabled(buttonsEnabled);
		btnExtractAll.setEnabled(buttonsEnabled);
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new LumpsPanel(indicies -> {}));
	}
}
