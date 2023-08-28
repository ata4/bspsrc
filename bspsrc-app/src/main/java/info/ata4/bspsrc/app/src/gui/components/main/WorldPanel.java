package info.ata4.bspsrc.app.src.gui.components.main;

import info.ata4.bspsrc.app.src.ObservableBspSourceConfig;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.decompiler.BspSourceConfig;
import info.ata4.bspsrc.decompiler.modules.geom.BrushMode;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import static info.ata4.bspsrc.app.util.swing.GuiUtil.getComponentsRecursive;
import static java.util.Objects.requireNonNull;

public class WorldPanel extends JPanel {

	private final ObservableBspSourceConfig config;

	private final JCheckBox chkEnable = new JCheckBox("Enable") {{
		addActionListener(e -> WorldPanel.this.config.updateConfig(c -> c.writeWorldBrushes = isSelected()));
	}};

	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final JRadioButton rdBrushes = new JRadioButton("Brushes and planes") {{
		buttonGroup.add(this);
		setToolTipText("""
				Create brushes that closely resemble those
				brushes from which the map was originally created from.""");
		addActionListener(e -> WorldPanel.this.config.updateConfig(c -> { if (isSelected()) c.brushMode = BrushMode.BRUSHPLANES; }));
	}};
	private final JRadioButton rdOrig = new JRadioButton("Original faces only") {{
		buttonGroup.add(this);
		setToolTipText("""
				<html>Create flat brushes from the culled<br>
				brush sides of the original brushes.<br>
				<b>Note:</b> some sides may be missing.</html>""");
		addActionListener(e -> WorldPanel.this.config.updateConfig(c -> { if (isSelected()) c.brushMode = BrushMode.ORIGFACE; }));
	}};
	private final JRadioButton rdOrigAndSplit = new JRadioButton("Original/split faces") {{
		buttonGroup.add(this);
		setToolTipText("""
				Create flat brushes from the culled
				brush sides of the original brushes.
				When a side doesn't exist, the split face
				is created instead.""");
		addActionListener(e -> WorldPanel.this.config.updateConfig(c -> { if (isSelected()) c.brushMode = BrushMode.ORIGFACE_PLUS; }));
	}};
	private final JRadioButton rdSplit = new JRadioButton("Split faces only") {{
		buttonGroup.add(this);
		setToolTipText("""
				Create flat brushes from the split faces
				the engine is using for rendering.""");
		addActionListener(e -> WorldPanel.this.config.updateConfig(c -> { if (isSelected()) c.brushMode = BrushMode.SPLITFACE; }));
	}};

	private final JPanel pnlMode = new JPanel(new MigLayout()) {{
		setBorder(BorderFactory.createTitledBorder("Mode"));
		add(rdBrushes, "wrap");
		add(rdOrig, "wrap");
		add(rdOrigAndSplit, "wrap");
		add(rdSplit, "wrap");
	}};

	private final JCheckBox chkWriteDisplacements = new JCheckBox("Write displacements") {{
		addActionListener(e -> WorldPanel.this.config.updateConfig(c -> c.writeDisp = isSelected()));
	}};

	public WorldPanel(ObservableBspSourceConfig config) {
		this.config = requireNonNull(config);

		config.addListener(this::update);
		update();

		setLayout(new MigLayout(
				"",
				"[]u[]",
				""
		));

		add(chkEnable, "wrap");
		add(pnlMode);
		add(chkWriteDisplacements);
	}

	private void update() {
		boolean enabled = config.get(c -> c.writeWorldBrushes);
		chkEnable.setSelected(enabled);

		getComponentsRecursive(pnlMode).forEach(c -> c.setEnabled(enabled));
		chkWriteDisplacements.setEnabled(enabled);

		var radio = switch (config.get(c -> c.brushMode)) {
			case BRUSHPLANES -> rdBrushes;
			case ORIGFACE -> rdOrig;
			case ORIGFACE_PLUS -> rdOrigAndSplit;
			case SPLITFACE -> rdSplit;
		};
		radio.setSelected(true);

		chkWriteDisplacements.setSelected(config.get(c -> c.writeDisp));
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new WorldPanel(new ObservableBspSourceConfig(new BspSourceConfig())));
	}
}
