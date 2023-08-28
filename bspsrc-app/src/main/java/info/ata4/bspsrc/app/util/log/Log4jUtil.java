package info.ata4.bspsrc.app.util.log;

import info.ata4.bspsrc.app.util.log.plugins.DocumentAppender;
import info.ata4.bspsrc.decompiler.BspFileEntry;
import info.ata4.bspsrc.decompiler.BspSource;
import info.ata4.io.util.PathUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.ThreadContextMapFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.KeyValuePair;

import javax.swing.text.Document;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static info.ata4.bspsrc.common.util.JavaUtil.zip;

/**
 * Utility class to configure Log4j2 logging
 */
public class Log4jUtil {
	private static final Logger L = LogManager.getLogger();

	public static final PatternLayout FILE_PATTERN = PatternLayout.newBuilder()
			.withPattern("%d{HH:mm:ss.SSS} %-5level %msg%n")
			.build();
	public static final PatternLayout UI_PATTERN = PatternLayout.newBuilder()
			.withPattern("[%level{WARN=warning, DEBUG=debug, ERROR=error, TRACE=trace, INFO=info}] %msg%n")
			.build();

	public static void configure(URL configUrl) {
		try {
			Configurator.initialize(
					(String) null,
					null,
					configUrl.toURI()
			);
		} catch (URISyntaxException e) {
			// doesn't happen?
			throw new RuntimeException(e);
		}
	}

	public static void setRootLevel(Level level) {
		((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).setLevel(level);
	}

	public static CloseableScope addAppenders(Appender... appenders) {
		var rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

		for (Appender appender : appenders)
		{
			rootLogger.addAppender(appender);
			appender.start();
		}

		return () -> {
			for (Appender appender : appenders)
			{
				rootLogger.removeAppender(appender);
				appender.stop();
			}
		};
	}

	public static CloseableScope configureDecompilationLogFileAppender(
			List<UUID> entryUuids,
			List<BspFileEntry> entries
	) {
		LoggerContext context = LoggerContext.getContext(false);
		Configuration config = context.getConfiguration();

		var appenders = StreamSupport.stream(zip(entryUuids, entries).spliterator(), false)
				.map(entry -> Map.entry(entry.getKey(), PathUtils.setExtension(entry.getValue().getVmfFile(), "log")))
				.map(entry -> FileAppender.newBuilder()
						.setName("Decompile task file appender %s".formatted(entry.getKey()))
						.setFilter(ThreadContextMapFilter.createFilter(
								new KeyValuePair[]{
										new KeyValuePair(BspSource.DECOMPILE_TASK_ID_IDENTIFIER, entry.getKey().toString())
								},
								null,
								Filter.Result.ACCEPT,
								Filter.Result.DENY
						))
						.setLayout(FILE_PATTERN)
						.withAppend(false)
						.withFileName(entry.getValue().toString())
						.setConfiguration(config)
						.build())
				.collect(Collectors.toSet());

		return addAppenders(appenders.toArray(Appender[]::new));
	}

	public static CloseableScope configureDecompilationDocumentAppenders(
			List<UUID> entryUuids,
			List<Document> taskLogs
	) {
		LoggerContext context = LoggerContext.getContext(false);
		Configuration config = context.getConfiguration();

		var appenders = StreamSupport.stream(zip(entryUuids, taskLogs).spliterator(), false)
				.map(entry -> DocumentAppender.newBuilder()
						.setName("Decompile task document appender %s".formatted(entry.getKey()))
						.setDocument(entry.getValue())
						.setFilter(ThreadContextMapFilter.createFilter(
								new KeyValuePair[]{
										new KeyValuePair(BspSource.DECOMPILE_TASK_ID_IDENTIFIER, entry.getKey().toString())
								},
								null,
								Filter.Result.ACCEPT,
								Filter.Result.DENY
						))
						.setLayout(UI_PATTERN)
						.setConfiguration(config)
						.build())
				.collect(Collectors.toSet());

		return addAppenders(appenders.toArray(Appender[]::new));
	}

	public interface CloseableScope extends AutoCloseable {
		@Override
		void close();
	}
}
