package info.ata4.bspsrc.app.util.log.plugins;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import javax.swing.*;
import java.awt.*;

import static java.util.Objects.requireNonNull;

public class DialogAppender extends AbstractAppender {

	private final Component component;

	public DialogAppender(
			String name,
			Filter filter,
			Layout layout,
			boolean ignoreExceptions,
			Property[] properties,
			Component component
	) {
		super(name, filter, layout, ignoreExceptions, properties);
		this.component = requireNonNull(component);
	}

	@Override
	public void append(LogEvent event) {
		String message = getLayout().toSerializable(event);
		SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
				component,
				message,
				"Error",
				JOptionPane.ERROR_MESSAGE
		));
	}
}
