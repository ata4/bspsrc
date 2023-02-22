/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.app.src.cli;

import info.ata4.log.LogUtils;
import picocli.CommandLine;

import java.util.logging.Logger;

/**
 * Helper class for CLI parsing and handling.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceCli {

    private static final Logger L = LogUtils.getLogger();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogUtils.configure();

        var cmdLine = new CommandLine(new BspSourceCliCommand());
        cmdLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO));

        if (args.length == 0)
            cmdLine.usage(cmdLine.getOut());
        else
            cmdLine.execute(args);
    }
}
