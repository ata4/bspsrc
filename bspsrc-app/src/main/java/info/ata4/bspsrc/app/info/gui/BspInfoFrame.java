package info.ata4.bspsrc.app.info.gui;

import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.info.gui.panel.*;
import info.ata4.bspsrc.app.info.log.DialogHandler;
import info.ata4.bspsrc.app.util.FileExtensionFilter;
import info.ata4.bspsrc.decompiler.BspSource;
import info.ata4.log.LogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static info.ata4.bspsrc.app.info.gui.Util.wrapWithAlign;
import static info.ata4.bspsrc.app.util.GridBagConstraintsBuilder.Anchor.FIRST_LINE_START;
import static info.ata4.bspsrc.app.util.GridBagConstraintsBuilder.Anchor.PAGE_START;
import static info.ata4.bspsrc.app.util.GridBagConstraintsBuilder.Fill.HORIZONTAL;
import static info.ata4.bspsrc.app.util.GridBagConstraintsBuilder.Fill.NONE;
import static java.util.Objects.requireNonNull;
import static javax.swing.BorderFactory.createCompoundBorder;

public class BspInfoFrame extends JFrame {

	private static final Logger L = LogUtils.getLogger();

	public static final String NAME = "BSPInfo";
	public static final String VERSION = BspSource.VERSION;

	private final BspInfoModel model;

	private final JFileChooser fileChooser = new JFileChooser();
	private final JFileChooser lumpDstChooser = new JFileChooser();
	private final JFileChooser embeddedFileDstChooser = new JFileChooser();
	private final JFileChooser embeddedRawDstChooser = new JFileChooser();

	private final JTabbedPane tabbedPane = new JTabbedPane();
	private final GeneralPanel generalPanel = new GeneralPanel();
	private final LumpsPanel lumpsPanel = new LumpsPanel(this::extractLumps);
	private final GameLumpsPanel gameLumpsPanel = new GameLumpsPanel(this::extractGameLumps);
	private final EntitiesPanel entitiesPanel = new EntitiesPanel();
	private final DependenciesPanel dependenciesPanel = new DependenciesPanel();
	private final EmbeddedPanel embeddedPanel = new EmbeddedPanel(this::extractFiles, this::extractFilesRaw);
	private final ProtectionPanel protectionPanel = new ProtectionPanel();


	public BspInfoFrame(BspInfoModel model) {
		this.model = model;
		model.addListener(this::onChanges);

		// add dialog log handler
		L.addHandler(new DialogHandler(this));

		fileChooser.setFileFilter(new FileExtensionFilter("Source engine map file", "bsp"));
		lumpDstChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		embeddedFileDstChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		embeddedRawDstChooser.setAcceptAllFileFilterUsed(false);
		embeddedRawDstChooser.setFileFilter(new FileExtensionFilter("Zip file", "zip"));

		var tabbedPanePaddingBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

		var wrappedGeneralPanel = wrapWithAlign(generalPanel, PAGE_START, HORIZONTAL);
		var wrappedProtectionPanel = wrapWithAlign(protectionPanel, FIRST_LINE_START, NONE);

		wrappedGeneralPanel.setBorder(tabbedPanePaddingBorder);
		lumpsPanel.setBorder(createCompoundBorder(tabbedPanePaddingBorder, lumpsPanel.getBorder()));
		gameLumpsPanel.setBorder(createCompoundBorder(tabbedPanePaddingBorder, gameLumpsPanel.getBorder()));
		entitiesPanel.setBorder(createCompoundBorder(tabbedPanePaddingBorder, entitiesPanel.getBorder()));
		dependenciesPanel.setBorder(createCompoundBorder(tabbedPanePaddingBorder, dependenciesPanel.getBorder()));
		embeddedPanel.setBorder(createCompoundBorder(tabbedPanePaddingBorder, embeddedPanel.getBorder()));
		wrappedProtectionPanel.setBorder(tabbedPanePaddingBorder);

		tabbedPane.addTab("General", wrappedGeneralPanel);
		tabbedPane.addTab("Lumps", lumpsPanel);
		tabbedPane.addTab("Game lumps", gameLumpsPanel);
		tabbedPane.addTab("Entities", entitiesPanel);
		tabbedPane.addTab("Dependencies", dependenciesPanel);
		tabbedPane.addTab("Embedded files", embeddedPanel);
		tabbedPane.addTab("Protection", wrappedProtectionPanel);

		setContentPane(tabbedPane);
		initMenuBar();
		initTransferHandler();

		onChanges();

		setTitle(NAME + " " + VERSION);

		URL iconUrl = requireNonNull(getClass().getResource("resources/icon.png"));
		Image icon = Toolkit.getDefaultToolkit().createImage(iconUrl);
		setIconImage(icon);

		pack();
		setMinimumSize(getSize());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void initMenuBar() {
		var menuItemOpenFile = new JMenuItem("Open");
		menuItemOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		menuItemOpenFile.addActionListener(e -> {
			int result = fileChooser.showOpenDialog(this);
			if (result != JFileChooser.APPROVE_OPTION)
				return;

			lumpDstChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());

			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			try {
				model.load(fileChooser.getSelectedFile().toPath());
			} catch (IOException ex) {
				L.log(Level.SEVERE, "Error occurred loading file", ex);
			} finally {
				setCursor(Cursor.getDefaultCursor());
			}
		});

		var menuFile = new JMenu("File");
		menuFile.add(menuItemOpenFile);

		var menuBar = new JMenuBar();
		menuBar.add(menuFile);

		setJMenuBar(menuBar);
	}

	private void initTransferHandler() {
		setTransferHandler(new TransferHandler() {
			@Override
			public boolean canImport(TransferSupport support) {
				return Arrays.stream(support.getDataFlavors())
						.anyMatch(DataFlavor::isFlavorJavaFileListType);
			}

			@Override
			public boolean importData(TransferSupport support) {
				try {
					List<File> files = (List<File>) support.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);

					model.load(files.get(files.size() - 1).toPath());
					return true;
				} catch (UnsupportedFlavorException | IOException e) {
					L.log(Level.WARNING, "Error in drag and drop", e);
					return false;
				}
			}
		});
	}

	private void onChanges() {
		generalPanel.update(model);
		lumpsPanel.update(model);
		gameLumpsPanel.update(model);
		entitiesPanel.update(model);
		dependenciesPanel.update(model);
		embeddedPanel.update(model);
		protectionPanel.update(model);
	}

	private void extractLumps(Set<Integer> lumpIndices) {
		Path lumpDst = chooseDstDialog(lumpDstChooser);
		if (lumpDst == null)
			return;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			model.extractLumps(lumpIndices, lumpDst);
		} catch (IOException e) {
			L.log(Level.SEVERE, "Error occurred extracting lump(s)", e);
			return;
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}

		JOptionPane.showMessageDialog(this, "Successfully extracted lump(s).");
	}

	private void extractGameLumps(Set<Integer> lumpIndices) {
		Path lumpDst = chooseDstDialog(lumpDstChooser);
		if (lumpDst == null)
			return;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			model.extractGameLumps(lumpIndices, lumpDst);
		} catch (IOException e) {
			L.log(Level.SEVERE, "Error occurred extracting game lump(s)", e);
			return;
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}

		JOptionPane.showMessageDialog(this, "Successfully extracted game lump(s).");
	}

	private void extractFiles(Set<Integer> fileIndices) {
		Path filesDst = chooseDstDialog(embeddedFileDstChooser);
		if (filesDst == null)
			return;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			model.extractEmbeddedFiles(fileIndices, filesDst);
		} catch (IOException e) {
			L.log(Level.SEVERE, "Error occurred extracting embedded file(s)", e);
			return;
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}

		JOptionPane.showMessageDialog(this, "Successfully extracted embedded file(s).");
	}

	private void extractFilesRaw() {
		Path filesDst = chooseDstDialog(embeddedRawDstChooser);
		if (filesDst == null)
			return;

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			model.extractEmbeddedFilesRaw(filesDst);
		} catch (IOException e) {
			L.log(Level.SEVERE, "Error occurred extracting embedded files", e);
			return;
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}

		JOptionPane.showMessageDialog(this, "Successfully extracted embedded files.");
	}

	private Path chooseDstDialog(JFileChooser fileChooser) {
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
			return fileChooser.getSelectedFile().toPath();
		else
			return null;
	}
}
