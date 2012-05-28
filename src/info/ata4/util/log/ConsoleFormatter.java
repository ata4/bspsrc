/*
 ** 2011 April 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Log formatter for console output.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ConsoleFormatter extends Formatter {
    
    private static final Map<Level, String> LEVEL_PREFIX;
    
    static {
        Map<Level, String> levelPrefix = new HashMap<Level, String>();
        levelPrefix.put(Level.CONFIG, "[config]");
        levelPrefix.put(Level.FINE, "[debug]");
        levelPrefix.put(Level.FINER, "[debug]");
        levelPrefix.put(Level.FINEST, "[trace]");
        levelPrefix.put(Level.INFO, "[info]");
        levelPrefix.put(Level.SEVERE, "[error]");
        levelPrefix.put(Level.WARNING, "[warning]");
        
        LEVEL_PREFIX = Collections.unmodifiableMap(levelPrefix);
    }
    
    private static boolean printStackTrace = false;

    public static boolean isPrintStackTrace() {
        return printStackTrace;
    }

    public static void setPrintStackTrace(boolean aPrintStackTrace) {
        printStackTrace = aPrintStackTrace;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(LEVEL_PREFIX.get(record.getLevel()));
        sb.append(' ');

        String[] classNameParts = record.getLoggerName().split("\\.");

        // add class name for non-info records
        if (record.getLevel() != Level.INFO && classNameParts.length != 0) {
            sb.append(classNameParts[classNameParts.length - 1]);
            sb.append(": ");
        }

        sb.append(formatMessage(record));

        // print stack trace if enabled
        if (record.getThrown() != null) {
            Throwable thrown = record.getThrown();
            
            if (printStackTrace) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                thrown.printStackTrace(pw);
                pw.close();
                sb.append(", caused by ");
                sb.append(sw.toString());
            } else {
                sb.append(": ");
                sb.append(thrown.getClass().getName()); 
                if (thrown.getMessage() != null) {
                    sb.append(": ");
                    sb.append(thrown.getMessage());
                }
            }
        }
        
        sb.append("\n");

        return sb.toString();
    }
}
