/*
 ** 2012 Juni 7
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.info.gui;

import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFileException extends IOException {

    /**
     * Creates a new instance of
     * <code>BspFileException</code> without detail message.
     */
    public BspFileException() {
    }

    /**
     * Constructs an instance of
     * <code>BspFileException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public BspFileException(String msg) {
        super(msg);
    }

    public BspFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
