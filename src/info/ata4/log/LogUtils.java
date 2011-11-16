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
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LogUtils {
    public static void configureLogger(Level level) {
        // get root logger
        Logger log = Logger.getLogger("");

        // remove default handler
        for (Handler handler : log.getHandlers()) {
            log.removeHandler(handler);
        }

        // create new console handler
        ConsoleHandler conHandler = new ConsoleHandler();
        conHandler.setFormatter(new ConsoleFormatter());
        log.addHandler(conHandler);
        
        // set level
        if (level != null) {
            log.setLevel(level);
        }
    }
    
    public static void configureLogger() {
        configureLogger(null);
    } 
}
