package info.ata4.bspsrc.app.src.gui.components.main;

import info.ata4.bspsrc.app.src.ObservableBspSourceConfig;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.decompiler.BspSourceConfig;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import static info.ata4.bspsrc.app.util.swing.GuiUtil.getComponentsRecursive;
import static java.util.Objects.requireNonNull;

public class EntitiesPanel extends JPanel {

	private final ObservableBspSourceConfig config;

	private final JCheckBox chkEnable = new JCheckBox("Enable") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.setWriteEntities(isSelected())));
	}};

	private final JCheckBox chkDetail = new JCheckBox("Restore func_detail") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.writeDetails = isSelected()));
	}};
	private final JCheckBox chkDetailMerging = new JCheckBox("Merge touching func_details") {{
		setToolTipText("""
				Merge func_details brushes which touch into one entity.""");
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.detailMerge = isSelected()));
	}};
	private final JCheckBox chkAreaportal = new JCheckBox("Restore func_areaportal/_window") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.writeAreaportals = isSelected()));
	}};
	private final JCheckBox chkAreaportalManualMapping = new JCheckBox("Force manual mapping") {{
		setToolTipText("""
				Manually calculate areaportal reallocation.
				This method should be more robust at the expense of
				being less precise sometimes""");
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.apForceManualMapping = isSelected()));
	}};
	private final JCheckBox chkOccluder = new JCheckBox("Restore func_occluder") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.writeOccluders = isSelected()));
	}};
	private final JCheckBox chkLadder = new JCheckBox("Restore func_ladder") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.writeLadders = isSelected()));
	}};
	private final JCheckBox chkVisCluster = new JCheckBox("Reconstruct func_viscluster") {{
		setToolTipText("""
				Try to reconstruct vis_cluster entities.
				Might not always be 100% accurate.""");
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.writeVisClusters = isSelected()));
	}};
	private final JCheckBox chkFixRotation = new JCheckBox("Fix rotation of instances") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.fixEntityRot = isSelected()));
	}};

	private final JCheckBox chkStaticProp = new JCheckBox("prop_static") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.writeStaticProps = isSelected()));
	}};
	private final JCheckBox chkCubemap = new JCheckBox("info_cubemap") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.writeCubemaps = isSelected()));
	}};
	private final JCheckBox chkOverlay = new JCheckBox("info_overlay") {{
		addActionListener(e -> EntitiesPanel.this.config.updateConfig(c -> c.writeOverlays = isSelected()));
	}};

	private final JPanel pnlPointEnts = new JPanel(new MigLayout()) {{
		setBorder(BorderFactory.createTitledBorder("Point entities"));
		add(chkStaticProp, "wrap");
		add(chkCubemap, "wrap");
		add(chkOverlay, "wrap");
	}};

	private final JPanel pnlBrushEnts = new JPanel(new MigLayout()) {{
		setBorder(BorderFactory.createTitledBorder("Brush entities"));
		add(chkDetail, "wrap");
		add(chkDetailMerging, "gapleft i, wrap");
		add(chkAreaportal, "wrap");
		add(chkAreaportalManualMapping, "gapleft i, wrap");
		add(chkOccluder, "wrap");
		add(chkLadder, "wrap");
		add(chkVisCluster, "wrap");
		add(chkFixRotation, "wrap");
	}};

	public EntitiesPanel(ObservableBspSourceConfig config) {
		this.config = requireNonNull(config);

		config.addListener(this::update);
		update();

		setLayout(new MigLayout());

		add(chkEnable, "wrap");
		add(pnlPointEnts, "growy");
		add(pnlBrushEnts, "growy");
	}

	private void update() {
		boolean enabled = config.get(c -> c.isWriteEntities());
		chkEnable.setSelected(enabled);

		getComponentsRecursive(pnlPointEnts).forEach(c -> c.setEnabled(enabled));
		getComponentsRecursive(pnlBrushEnts).forEach(c -> c.setEnabled(enabled));
		chkDetailMerging.setEnabled(enabled && config.get(c -> c.writeDetails));
		chkAreaportalManualMapping.setEnabled(enabled && config.get(c -> c.writeAreaportals));

		chkDetail.setSelected(config.get(c -> c.writeDetails));
		chkDetailMerging.setSelected(config.get(c -> c.detailMerge));
		chkAreaportal.setSelected(config.get(c -> c.writeAreaportals));
		chkAreaportalManualMapping.setSelected(config.get(c -> c.apForceManualMapping));
		chkOccluder.setSelected(config.get(c -> c.writeOccluders));
		chkLadder.setSelected(config.get(c -> c.writeLadders));
		chkVisCluster.setSelected(config.get(c -> c.writeVisClusters));
		chkFixRotation.setSelected(config.get(c -> c.fixEntityRot));

		chkStaticProp.setSelected(config.get(c -> c.writeStaticProps));
		chkCubemap.setSelected(config.get(c -> c.writeCubemaps));
		chkOverlay.setSelected(config.get(c -> c.writeOverlays));
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new EntitiesPanel(new ObservableBspSourceConfig(new BspSourceConfig())));
	}
}
