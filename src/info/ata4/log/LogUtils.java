/*
 ** 2011 September 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logging utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LogUtils {
    
    private LogUtils() {
    }
    
    public static void configure(Logger logger, Level level) {
        // remove default handler
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        // create new console handler
        ConsoleHandler conHandler = new ConsoleHandler();
        conHandler.setFormatter(new ConsoleFormatter());
        logger.addHandler(conHandler);
        
        // set level
        if (level != null) {
            logger.setLevel(level);
        }
    }
    
    public static void configure(Logger logger) {
        configure(logger, Level.INFO);
    }
    
    public static void configure() {
        configure(Logger.getLogger(""), Level.INFO);
    }
}
