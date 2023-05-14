/*
 ** 2014 May 15
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.src;

import info.ata4.bspsrc.app.src.cli.BspSourceCli;

/**
 * Simple launcher that starts the CLI if any command
 * line arguments are present or the GUI otherwise.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceLauncher {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			throw new UnsupportedOperationException("Not implemented currently");
		} else {
			BspSourceCli.main(args);
		}
	}
}
