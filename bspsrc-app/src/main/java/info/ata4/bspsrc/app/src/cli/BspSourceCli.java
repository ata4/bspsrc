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

import info.ata4.bspsrc.app.util.log.Log4jUtil;
import picocli.CommandLine;

import static java.util.Objects.requireNonNull;

/**
 * Helper class for CLI parsing and handling.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceCli {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Log4jUtil.configure(requireNonNull(BspSourceCli.class.getResource("log4j2.xml")));

        var cmdLine = new CommandLine(new BspSourceCliCommand());
        cmdLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO));

        if (args.length == 0)
            cmdLine.usage(cmdLine.getOut());
        else
            cmdLine.execute(args);
    }
}
