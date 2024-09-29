package info.ata4.bspsrc.app.src.gui.components.main;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import info.ata4.bspsrc.app.src.gui.components.task.DecompileTaskDialog;
import info.ata4.bspsrc.app.src.gui.models.BspSourceModel;
import info.ata4.bspsrc.app.src.gui.models.DecompileTaskModel;
import info.ata4.bspsrc.app.src.gui.models.FilesModel;
import info.ata4.bspsrc.app.util.BspPathUtil;
import info.ata4.bspsrc.app.util.log.Log4jUtil;
import info.ata4.bspsrc.app.util.log.plugins.DialogAppender;
import info.ata4.bspsrc.app.util.log.plugins.IsDecompileTaskFilter;
import info.ata4.bspsrc.app.util.swing.FileExtensionFilter;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.decompiler.BspFileEntry;
import info.ata4.bspsrc.decompiler.BspSource;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.core.Filter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static info.ata4.bspsrc.common.util.Collectors.mode;
import static java.util.Objects.requireNonNull;

public class BspSourceFrame extends JFrame {

	private final BspSourceModel model;
	private final FilesModel filesModel = new FilesModel();

	private final JFileChooser vmfFileChooser = new JFileChooser() {
		{
			setFileFilter(new FileExtensionFilter("Hammer map file", "vmf"));
		}

		@Override
		public void approveSelection() {
			File file = getSelectedFile();
			if (file != null && file.exists() && !askOverwrite(file))
				return;

			super.approveSelection();
		}

		private boolean askOverwrite(File file) {
			String title = "Overwriting " + file.getPath();
			String message = "File %s already exists.\nDo you like to replace it?".formatted(file.getName());

			int choice = JOptionPane.showConfirmDialog(
					this,
					message,
					title,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
			);

			return choice == JOptionPane.OK_OPTION;
		}
	};
	private final JFileChooser vmfDirFileChooser = new JFileChooser() {{
		setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}};

	public final FlatTabbedPane tbpMain = new FlatTabbedPane();

	public final FilesPanel filesPanel;
	public final WorldPanel worldPanel;
	public final EntitiesPanel entitiesPanel;
	public final TexturesPanel texturesPanel;
	public final OtherPanel otherPanel;

	private final JButton btnDefaults = new JButton("Defaults") {{
		setToolTipText("Resets all configurations to their defaults.");
		addActionListener(e -> BspSourceFrame.this.model.setDefaults());
	}};
	private final JCheckBox chkDarkTheme = new JCheckBox("Dark theme", GuiUtil.isDarkTheme()) {{
		addActionListener(e -> GuiUtil.setDarkTheme(chkDarkTheme.isSelected()));
	}};
	private final JButton btnDecompile = new JButton("Decompile") {{
		setFont(getFont().deriveFont(Font.BOLD));
		addActionListener(e -> decompile());
	}};

	private void decompile() {
		List<Path> bspPaths = filesModel.getBspPaths();
		if (bspPaths.isEmpty())
			return;

		List<BspFileEntry> entries;
		if (bspPaths.size() == 1) {
			Path bspPath = bspPaths.get(0);

			vmfFileChooser.setSelectedFile(BspPathUtil.defaultVmfPath(bspPath, null).toFile());

			Path vmfPath = showSaveDialog(vmfFileChooser);
			if (vmfPath == null)
				return;

			entries = List.of(new BspFileEntry(bspPath, vmfPath));
		} else {
			// idk if there is a perhaps "less surprising" way for the user, of picking the default dir
			Path commonParent = bspPaths.stream()
					.map(Path::getParent)
					.map(Path::toAbsolutePath)
					.map(Path::normalize)
					.collect(mode())
					.orElseThrow();

			vmfDirFileChooser.setCurrentDirectory(commonParent.toFile());

			Path dirPath = showSaveDialog(vmfDirFileChooser);
			if (dirPath == null)
				return;

			entries = bspPaths.stream()
					.map(bspPath -> new BspFileEntry(bspPath, BspPathUtil.defaultVmfPath(bspPath, dirPath)))
					.toList();
		}

		model.decompile(entries);
	}

	public BspSourceFrame(BspSourceModel model) {
		this.model = requireNonNull(model);
		model.addOnDecompileTask(this::onDecompileTask);

		initErrorDialog();

		filesModel.addListener(new FilesModel.Listener() {
			@Override public void added(int minIndex, int maxIndex) { updateDecompileButton(); }
			@Override public void removed(int minIndex, int maxIndex) { updateDecompileButton(); }
		});

		updateDecompileButton();

		filesPanel = new FilesPanel(filesModel);
		worldPanel = new WorldPanel(model.getConfig());
		entitiesPanel = new EntitiesPanel(model.getConfig());
		texturesPanel = new TexturesPanel(model.getConfig());
		otherPanel = new OtherPanel(model.getConfig());

		tbpMain.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tbpMain.setTabInsets(new Insets(8, 8, 8, 8)); // Make tabs a little more compact
		tbpMain.addTab("Files", filesPanel);
		tbpMain.addTab("World", worldPanel);
		tbpMain.addTab("Entities", entitiesPanel);
		tbpMain.addTab("Textures", texturesPanel);
		tbpMain.addTab("Other", otherPanel);

		var pnlBottom = new JPanel(new MigLayout(
				"insets 0",
				"[|]push[]",
				""
		));
		pnlBottom.add(btnDefaults);
		pnlBottom.add(chkDarkTheme);
		pnlBottom.add(btnDecompile);

		var panel = new JPanel(new MigLayout(
				"",
				"[grow]",
				"[grow][][]"
		));
		panel.add(tbpMain, "grow, wrap");
		panel.add(new JSeparator(), "grow, wrap");
		panel.add(pnlBottom, "grow");
		setContentPane(panel);

		setTitle("BSPSource " + BspSource.VERSION);

		URL iconUrl = requireNonNull(getClass().getResource("icon.png"));
		Image icon = Toolkit.getDefaultToolkit().createImage(iconUrl);
		setIconImage(icon);

		pack();
		setMinimumSize(getSize());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void initErrorDialog() {
		var dialogAppender = DialogAppender.createAppender(
				"DialogAppender" + hashCode(),
				new IsDecompileTaskFilter(Filter.Result.DENY, Filter.Result.NEUTRAL),
				null,
				false,
				this
		);
		var appenderCloseable = Log4jUtil.addAppenders(dialogAppender);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				appenderCloseable.close();
			}
		});
	}

	private void onDecompileTask(DecompileTaskModel model) {
		var decompileTaskDialog = new DecompileTaskDialog(this, model);
		decompileTaskDialog.setVisible(true);
	}

	private Path showSaveDialog(JFileChooser fileChooser) {
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
			return fileChooser.getSelectedFile().toPath();
		else
			return null;
	}

	private void updateDecompileButton() {
		this.btnDecompile.setEnabled(!filesModel.getBspPaths().isEmpty());
	}
}
