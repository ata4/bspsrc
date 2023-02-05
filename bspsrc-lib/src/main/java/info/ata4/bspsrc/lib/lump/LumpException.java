/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.lib.lump;

import java.io.IOException;

/**
 * Thrown to indicate reading errors in lump file structures
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpException extends IOException {

    public LumpException() {
        super();
    }

    public LumpException(String message) {
        super(message);
    }

    public LumpException(String message, Throwable cause) {
        super(message, cause);
    }

    public LumpException(Throwable cause) {
        super(cause);
    }
}
