package info.ata4.bspsrc.app.src.gui.components.main;

import com.formdev.flatlaf.FlatClientProperties;
import info.ata4.bspsrc.app.src.ObservableBspSourceConfig;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.decompiler.BspSourceConfig;
import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class TexturesPanel extends JPanel {

	private final ObservableBspSourceConfig config;

	private final List<String> textureSuggestions = List.of(
			ToolTexture.WHITE,
			ToolTexture.BLACK,
			ToolTexture.NODRAW,
			ToolTexture.ORANGE,
			ToolTexture.SKIP
	);

	private final JComboBox<String> cmbFaceTexture = new JComboBox<>(textureSuggestions.toArray(String[]::new)) {{
		setEditable(true);
		putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "<None>");
		addActionListener(e -> TexturesPanel.this.config.updateConfig(c -> c.faceTexture = (String) cmbFaceTexture.getSelectedItem()));
	}};
	private final JComboBox<String> cmbBackFaceTexture = new JComboBox<>(textureSuggestions.toArray(String[]::new)) {{
		setEditable(true);
		putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "<None>");
		addActionListener(e -> TexturesPanel.this.config.updateConfig(c -> c.backfaceTexture = (String) cmbBackFaceTexture.getSelectedItem()));
	}};
	private final JCheckBox chkFixCubemapTextures = new JCheckBox("Fix cubemap textures") {{
		setToolTipText("Fix textures for environment-mapped materials.");
		addActionListener(e -> TexturesPanel.this.config.updateConfig(c -> c.fixCubemapTextures = isSelected()));
	}};
	private final JCheckBox chkFixToolTextures = new JCheckBox("Fix tool textures") {{
		setToolTipText("Fix tool textures such as toolsnodraw or toolsblocklight.");
		addActionListener(e -> TexturesPanel.this.config.updateConfig(c -> c.fixToolTextures = isSelected()));
	}};

	public TexturesPanel(ObservableBspSourceConfig config) {
		this.config = requireNonNull(config);

		config.addListener(this::update);
		update();

		setLayout(new MigLayout(
				"",
				"",
				"[|]u[]"
		));

		var pnlOverride = new JPanel(new MigLayout());
		pnlOverride.setBorder(BorderFactory.createTitledBorder("Texture overwrite"));
		pnlOverride.add(new JLabel("Face texture"));
		pnlOverride.add(cmbFaceTexture, "wrap");
		pnlOverride.add(new JLabel("Back-face texture"));
		pnlOverride.add(cmbBackFaceTexture, "wrap");

		add(chkFixCubemapTextures, "wrap");
		add(chkFixToolTextures, "wrap");
		add(pnlOverride, "wrap");
	}

	private void update() {
		cmbFaceTexture.setSelectedItem(config.get(c -> c.faceTexture));
		cmbBackFaceTexture.setSelectedItem(config.get(c -> c.backfaceTexture));

		chkFixCubemapTextures.setSelected(config.get(c -> c.fixCubemapTextures));
		chkFixToolTextures.setSelected(config.get(c -> c.fixToolTextures));
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new TexturesPanel(new ObservableBspSourceConfig(new BspSourceConfig())));
	}
}
