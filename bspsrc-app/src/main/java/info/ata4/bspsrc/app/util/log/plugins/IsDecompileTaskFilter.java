package info.ata4.bspsrc.app.util.log.plugins;

import info.ata4.bspsrc.decompiler.BspSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.message.Message;

@Plugin(name = "IsDecompileTaskFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE)
public class IsDecompileTaskFilter extends AbstractFilter {

	private final ContextDataInjector injector = ContextDataInjectorFactory.createInjector();

	public IsDecompileTaskFilter(Result onMatch, Result onMismatch) {
		super(onMatch, onMismatch);
	}

	@Override
	public Result filter(LogEvent event) {
		return event.getContextData().containsKey(BspSource.DECOMPILE_TASK_ID_IDENTIFIER) ? onMatch : onMismatch;
	}

	private Result filter() {
		return injector.rawContextData().containsKey(BspSource.DECOMPILE_TASK_ID_IDENTIFIER) ? onMatch : onMismatch;
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
		return filter();
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
		return filter();
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
		return filter();
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String msg, Object p0) {
		return filter();
	}

	/**
	 * Create a IsDecompileTaskFilter.
	 * @param onMatch The action to take on a match.
	 * @param onMismatch The action to take on a mismatch.
	 * @return The created IsDecompileTaskFilter.
	 */
	@PluginFactory
	public static IsDecompileTaskFilter createFilter(
			@PluginAttribute(value = "onMatch", defaultString = "NEUTRAL") Result onMatch,
			@PluginAttribute(value = "onMismatch", defaultString = "NEUTRAL") Result onMismatch
	) {
		return new IsDecompileTaskFilter(onMatch, onMismatch);
	}
}
