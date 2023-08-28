/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
**/

package info.ata4.bspsrc.lib.exceptions;

/**
 * Thrown to indicate reading errors in BSP file structures.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspException extends Exception {

    public BspException() {
    }

    public BspException(String message) {
        super(message);
    }

    public BspException(String message, Throwable cause) {
        super(message, cause);
    }

    public BspException(Throwable cause) {
        super(cause);
    }

    public BspException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
