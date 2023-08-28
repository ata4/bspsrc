package info.ata4.bspsrc.app.src.gui.components.main;

import info.ata4.bspsrc.app.src.ObservableBspSourceConfig;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.decompiler.BspSourceConfig;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import static java.util.Objects.requireNonNull;

public class EntityMappingPanel extends JPanel {

	private final ObservableBspSourceConfig config;

	private final JCheckBox chkManualAreaportals = new JCheckBox("Use manual areaportal reallocation") {{
		setToolTipText("""
				Manually calculate areaportal reallocation.
				This method should be more robust at the expense of
				being less precise sometimes""");
		addActionListener(e -> EntityMappingPanel.this.config.updateConfig(c -> c.apForceManualMapping = isSelected()));
	}};
	private final JCheckBox chkManualOccluders = new JCheckBox("Use manual occluder reallocation") {{
		setToolTipText("""
				Manually calculate occluder reallocation.
				This method should be more robust at the expense of
				being less precise sometimes""");
		addActionListener(e -> EntityMappingPanel.this.config.updateConfig(c -> c.occForceManualMapping = isSelected()));
	}};

	public EntityMappingPanel(ObservableBspSourceConfig config) {
		this.config = requireNonNull(config);

		config.addListener(this::update);
		update();

		setLayout(new MigLayout());

		add(chkManualAreaportals, "wrap");
		add(chkManualOccluders, "wrap");
	}

	private void update() {
		chkManualAreaportals.setSelected(config.get(c -> c.apForceManualMapping));
		chkManualOccluders.setSelected(config.get(c -> c.occForceManualMapping));
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new EntityMappingPanel(new ObservableBspSourceConfig(new BspSourceConfig())));
	}
}
