package info.ata4.bspsrc.app.info.gui.panel;

import info.ata4.bspsrc.app.info.gui.data.GameLumpInfo;
import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.app.util.swing.model.ReadonlyListTableModel;
import info.ata4.bspsrc.app.util.swing.model.ReadonlyListTableModel.Column;
import info.ata4.bspsrc.app.util.swing.renderer.ByteSizeCellRenderer;
import info.ata4.bspsrc.app.util.swing.renderer.ProgressCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static info.ata4.bspsrc.app.util.swing.GuiUtil.setColumnWidth;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class GameLumpsPanel extends JPanel {

	public final JTable tblLumps = new JTable();
	public final ReadonlyListTableModel<GameLumpInfo> tableModel = new ReadonlyListTableModel<>(List.of(
			new Column<>("Name", String.class, GameLumpInfo::name),
			new Column<>("Size", Integer.class, GameLumpInfo::size),
			new Column<>("Size usage", Integer.class, GameLumpInfo::sizePercentage),
			new Column<>("Version", Integer.class, GameLumpInfo::version)
	));
	public final JButton btnExtract = new JButton("Extract");
	public final JButton btnExtractAll = new JButton("Extract all");

	public GameLumpsPanel(
			Consumer<Set<Integer>> onExtractLumps
	) {
		setLayout(new BorderLayout());

		tblLumps.setModel(tableModel);
		tblLumps.setAutoCreateRowSorter(true);
		tblLumps.getColumnModel().getColumn(1).setCellRenderer(new ByteSizeCellRenderer());
		tblLumps.getColumnModel().getColumn(2).setCellRenderer(new ProgressCellRenderer());

		setColumnWidth(tblLumps, 0, "----", false, false);
		setColumnWidth(tblLumps, 1, 100_100, true, false);
		setColumnWidth(tblLumps, 2, 100, true, false);
		setColumnWidth(tblLumps, 3, 10, true, false);

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
		tableModel.setData(model.getGameLumps());

		boolean buttonsEnabled = !model.getGameLumps().isEmpty();
		btnExtract.setEnabled(buttonsEnabled);
		btnExtractAll.setEnabled(buttonsEnabled);
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new LumpsPanel(indices -> {}));
	}
}
