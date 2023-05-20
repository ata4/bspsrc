package info.ata4.bspsrc.app.util.log.plugins;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.*;
import java.awt.*;

import static java.util.Objects.requireNonNull;

@Plugin(name = "Dialog", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class DialogAppender extends AbstractAppender {

	private final Component component;

	protected DialogAppender(
			String name,
			Filter filter,
			StringLayout layout,
			boolean ignoreExceptions,
			Property[] properties,
			Component component
	) {
		super(name, filter, layout, ignoreExceptions, properties);
		this.component = requireNonNull(component);
	}

	@Override
	public void append(LogEvent event) {
		if (!event.getLevel().isMoreSpecificThan(Level.ERROR))
			return;

		String message = ((StringLayout) getLayout()).toSerializable(event);
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
				component,
				message,
				"Error",
				JOptionPane.ERROR_MESSAGE
		));
	}

	@PluginFactory
	public static DialogAppender createAppender(
			String name,
			Filter filter,
			StringLayout layout,
			boolean ignoreExceptions,
			Component component
	) {
		if (name == null) {
			LOGGER.error("No name provided for DialogAppender");
			return null;
		}
		if (layout == null)
			layout = PatternLayout.createDefaultLayout();

		if (component == null) {
			LOGGER.error("No parent component provided for DialogAppender");
		}

		return new DialogAppender(name, filter, layout, ignoreExceptions, null, component);
	}
}
