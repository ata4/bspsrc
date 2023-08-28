package info.ata4.bspsrc.app.util.log.plugins;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.io.Serializable;

import static java.util.Objects.requireNonNull;

@Plugin(name = "Document", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class DocumentAppender extends AbstractAppender {

	private final Document document;

	public DocumentAppender(
			String name,
			Filter filter,
			Layout<String> layout,
			boolean ignoreExceptions,
			Property[] properties,
			Document document
	) {
		super(name, filter, layout, ignoreExceptions, properties);
		this.document = requireNonNull(document);
	}

	@Override
	public void append(LogEvent event) {
		String str = (((StringLayout) getLayout())).toSerializable(event);
		if (str.isBlank())
			return;

		try {
			document.insertString(document.getLength(), str, null);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	@PluginFactory
	public static DocumentAppender createAppender(
			String name,
			Filter filter,
			Layout<String> layout,
			boolean ignoreExceptions,
			Document document
	) {
		if (name == null) {
			LOGGER.error("No name provided for DocumentAppender");
			return null;
		}
		if (layout == null)
			layout = PatternLayout.createDefaultLayout();

		if (document == null) {
			LOGGER.error("No document provided for DocumentAppender");
		}

		return new DocumentAppender(name, filter, layout, ignoreExceptions, null, document);
	}


	@PluginBuilderFactory
	public static <B extends DocumentAppender.Builder<B>> B newBuilder() {
		return new DocumentAppender.Builder<B>().asBuilder();
	}

	public static class Builder<B extends DocumentAppender.Builder<B>> extends AbstractAppender.Builder<B>
			implements org.apache.logging.log4j.core.util.Builder<DocumentAppender> {

		private Document document;

		@Override
		public DocumentAppender build() {
			final Layout<? extends Serializable> layout = getOrCreateLayout();
			if (!(layout instanceof StringLayout stringLayout)) {
				LOGGER.error("Layout must be a StringLayout");
				return null;
			}
			if (document == null) {
				LOGGER.error("No document specified");
				return null;
			}

			return new DocumentAppender(
					getName(),
					getFilter(),
					stringLayout,
					isIgnoreExceptions(),
					getPropertyArray(),
					document
			);
		}

		public B setDocument(Document document) {
			this.document = document;
			return asBuilder();
		}
	}
}
