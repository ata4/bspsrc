package info.ata4.bspsrc.app.util.log;

import info.ata4.bspsrc.decompiler.BspSource;
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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class to configure Log4j2 logging
 */
public class Log4jUtil {
	private static final Logger L = LogManager.getLogger();

	public static final PatternLayout DEFAULT_PATTERN = PatternLayout.newBuilder()
			.withPattern("%d{HH:mm:ss.SSS} %-5level %msg%n")
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

	public static CloseableScope addAppender(Appender appender) {
		var rootLogger = ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger());

		rootLogger.addAppender(appender);
		appender.start();

		return () -> {
			rootLogger.removeAppender(appender);
			appender.stop();
		};
	}

	public static CloseableScope configureDecompilationLogFileAppender(Map<UUID, Path> logFileOutputMap) {
		LoggerContext context = LoggerContext.getContext(false);
		Configuration config = context.getConfiguration();

		var appenders = logFileOutputMap.entrySet().stream()
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
						.setLayout(DEFAULT_PATTERN)
						.withAppend(false)
						.withFileName(entry.getValue().toString())
						.setConfiguration(config)
						.build())
				.collect(Collectors.toSet());

		var rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

		appenders.forEach(fileAppender -> {
			rootLogger.addAppender(fileAppender);
			fileAppender.start();
		});

		return () -> appenders.forEach(fileAppender -> {
			rootLogger.removeAppender(fileAppender);
			fileAppender.stop();
		});
	}

	public interface CloseableScope extends AutoCloseable {
		@Override
		void close();
	}
}
