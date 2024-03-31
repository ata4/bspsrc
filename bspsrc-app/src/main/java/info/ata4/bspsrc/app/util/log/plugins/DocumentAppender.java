package info.ata4.bspsrc.app.util.log.plugins;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import static java.util.Objects.requireNonNull;

public class DocumentAppender extends AbstractAppender {

	private final Document document;

	public DocumentAppender(
			String name,
			Filter filter,
			Layout layout,
			boolean ignoreExceptions,
			Property[] properties,
			Document document
	) {
		super(name, filter, layout, ignoreExceptions, properties);
		this.document = requireNonNull(document);
	}

	@Override
	public void append(LogEvent event) {
		String str = getLayout().toSerializable(event);
		if (str.isBlank())
			return;

		try {
			document.insertString(document.getLength(), str, null);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}
}
