/*
 ** 2012 June 3
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Log formatter for dialog messages.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DialogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatMessage(record));
        sb.append(": ");
        sb.append(record.getThrown().getMessage());
        return sb.toString();
    }

}
