package info.ata4.bspsrc.app.info.gui.panel;

import info.ata4.bspsrc.app.info.gui.data.EmbeddedInfo;
import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.app.util.swing.model.ReadonlyListTableModel;
import info.ata4.bspsrc.app.util.swing.renderer.ByteSizeCellRenderer;

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

public class EmbeddedPanel extends JPanel {

	public final JTable tblFiles = new JTable();
	public final ReadonlyListTableModel<EmbeddedInfo> tableModel = new ReadonlyListTableModel<>(List.of(
			new ReadonlyListTableModel.Column<>("Name", String.class, EmbeddedInfo::name),
			new ReadonlyListTableModel.Column<>("Size", Long.class, EmbeddedInfo::size)
	));
	public final JButton btnExtract = new JButton("Extract");
	public final JButton btnExtractAll = new JButton("Extract all");
	public final JButton btnExtractRaw = new JButton("Extract raw Zip file");

	public EmbeddedPanel(
			Consumer<Set<Integer>> onExtractFiles,
			Runnable onExtractRaw
	) {
		setLayout(new BorderLayout());

		tblFiles.setModel(tableModel);
		tblFiles.setAutoCreateRowSorter(true);
		tblFiles.getColumnModel().getColumn(1).setCellRenderer(new ByteSizeCellRenderer());

		setColumnWidth(tblFiles, 0, "-".repeat(20), false);
		setColumnWidth(tblFiles, 1, 100_000, true);

		tblFiles.setPreferredScrollableViewportSize(new Dimension(tblFiles.getPreferredSize().width, -1));

		var scrlTable = new JScrollPane(tblFiles, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		add(scrlTable, BorderLayout.CENTER);

		btnExtract.addActionListener(e -> {
			var fileIndices = Arrays.stream(tblFiles.getSelectedRows())
					.map(tblFiles::convertRowIndexToModel)
					.boxed()
					.collect(Collectors.toSet());

			if (!fileIndices.isEmpty())
				onExtractFiles.accept(fileIndices);
		});
		btnExtractAll.addActionListener(e -> {
			var fileIndices = IntStream.range(0, tblFiles.getModel().getRowCount())
					.boxed()
					.collect(Collectors.toSet());

			onExtractFiles.accept(fileIndices);
		});
		btnExtractRaw.addActionListener(e -> onExtractRaw.run());

		var pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pnlButtons.add(btnExtract);
		pnlButtons.add(btnExtractAll);
		pnlButtons.add(btnExtractRaw);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	public void update(BspInfoModel model) {
		 tableModel.setData(model.getEmbeddedInfos());

		boolean buttonsEnabled = !model.getEmbeddedInfos().isEmpty();
		btnExtract.setEnabled(buttonsEnabled);
		btnExtractAll.setEnabled(buttonsEnabled);
		btnExtractRaw.setEnabled(buttonsEnabled);
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new EmbeddedPanel(indices -> {}, () -> {}));
	}
}
