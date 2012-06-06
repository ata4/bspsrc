/*
 ** 2011 September 10
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import org.apache.commons.io.IOUtils;

/**
 * Logging utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LogUtils {
    
    private LogUtils() {
    }
    
    public static void configure(String config) {
        InputStream is = null;
        
        try {
            is = LogUtils.class.getResourceAsStream(config + ".properties");
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException ex) {
            // don't use the logging system here, maybe it went to hell!
           System.err.println("Can't read logger configuration: " + ex);
            
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (IOException ex2) {
                // okay, this is just silly...
                System.err.println("Can't restore logger configuration: " + ex2);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
