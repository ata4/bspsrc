package info.ata4.bspsrc.app.src.gui.components.main;

import info.ata4.bspsrc.app.src.gui.models.FilesModel;
import info.ata4.bspsrc.app.util.swing.FileExtensionFilter;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class FilesPanel extends JPanel {

	private static final Logger L = LogManager.getLogger();

	private final FilesModel model;

	private final JFileChooser bspFileChooser = new JFileChooser() {{
		setMultiSelectionEnabled(true);
		setFileFilter(new FileExtensionFilter("Source engine map file", "bsp"));
	}};

	private final JList<Path> lstFiles = new JList<>();
	private final JScrollPane scrlTable = new JScrollPane(lstFiles, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

	private final JButton btnAdd = new JButton("Add") {{
		addActionListener(e -> FilesPanel.this.model.addEntries(chooseBsps()));
	}};
	private final JButton btnRemove = new JButton("Remove") {{
		addActionListener(e -> FilesPanel.this.model.removeEntries(lstFiles.getSelectedIndices()));
	}};
	private final JButton btnRemoveAll = new JButton("Remove all") {{
		addActionListener(e -> FilesPanel.this.model.removeAllEntries());
	}};

	public FilesPanel(FilesModel model) {
		this.model = requireNonNull(model);

		initLstFiles();
		initTransferHandler();

		setLayout(new MigLayout(
				"",
				"[grow][]",
				"[][][][grow][]"
		));
		add(scrlTable, "grow, spany 4");
		add(btnAdd, "growx, wrap");
		add(btnRemove, "growx, wrap");
		add(btnRemoveAll, "growx, wrap");
		add(new JLabel("Tip: drag and drop files/folders on the box above"), "newline");
	}

	private void initLstFiles() {
		lstFiles.setModel(new AbstractListModel<>() {
			{
				 model.addListener(new FilesModel.Listener() {
					@Override
					public void added(int minIndex, int maxIndex) {
						fireIntervalAdded(this, minIndex, maxIndex);
					}

					@Override
					public void removed(int minIndex, int maxIndex) {
						fireIntervalRemoved(this, minIndex, maxIndex);
					}
				});
			}

			@Override
			public int getSize() {
				return model.getBspPaths().size();
			}

			@Override
			public Path getElementAt(int index) {
				return model.getBspPaths().get(index);
			}
		});
	}

	private void initTransferHandler() {
		scrlTable.setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport(TransferSupport support) {
				boolean canImport = Arrays.stream(support.getDataFlavors())
						.anyMatch(DataFlavor::isFlavorJavaFileListType);

				if (canImport)
					support.setDropAction(DnDConstants.ACTION_MOVE);

				return canImport;
			}

			@Override
			public boolean importData(TransferSupport support) {
				if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
					return false;

				try {
					List<File> files = (List<File>) support.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);

					var bspPaths = new ArrayList<Path>();
					for (File file : files) {
						Path path = file.toPath();
						var bspMatcher = path.getFileSystem().getPathMatcher("glob:**.bsp");

						try (var subFilesStream = Files.walk(path)) {
							subFilesStream
									.skip(1) // always skip root. We add it later
									.filter(Files::isRegularFile)
									.filter(bspMatcher::matches)
									.forEachOrdered(bspPaths::add);
						}

						// Always add root as long it is a file and not a directory.
						// This way files that don't end in .bsp can be added as well if so desired.
						if (Files.isRegularFile(path))
							bspPaths.add(path);
					}

					// show message if no files were added
					if (bspPaths.isEmpty()) {
						JOptionPane.showMessageDialog(
								FilesPanel.this,
								"No .bsp files found",
								null,
								JOptionPane.WARNING_MESSAGE
						);
					}

					model.addEntries(bspPaths);
					return true;
				} catch (UnsupportedFlavorException | IOException e) {
					L.warn("Error in drag and drop", e);
					return false;
				}
			}
		});
		try {
			scrlTable.getDropTarget().addDropTargetListener(new DropTargetListener() {
				private Border originalBorder;

				@Override public void dragEnter(DropTargetDragEvent dtde) {
					originalBorder = scrlTable.getBorder();

					var borderColor = UIManager.getColor("Component.focusedBorderColor");
					var border = BorderFactory.createCompoundBorder(
							originalBorder,
							BorderFactory.createMatteBorder(2, 2, 2, 2, borderColor)
					);
					scrlTable.setBorder(border);
				}
				@Override public void dragOver(DropTargetDragEvent dtde) {}
				@Override public void dropActionChanged(DropTargetDragEvent dtde) {}
				@Override public void dragExit(DropTargetEvent dte) { scrlTable.setBorder(originalBorder); }
				@Override public void drop(DropTargetDropEvent dtde) { scrlTable.setBorder(originalBorder); }
			});
		} catch (TooManyListenersException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<Path> chooseBsps() {
		int result = bspFileChooser.showOpenDialog(this);
		if (result != JFileChooser.APPROVE_OPTION)
			return Set.of();

		return Arrays.stream(bspFileChooser.getSelectedFiles())
				.map(File::toPath)
				.collect(Collectors.toSet());
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new FilesPanel(new FilesModel()));
	}
}
