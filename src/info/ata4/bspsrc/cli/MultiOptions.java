/*
 ** 2011 September 5
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.cli;

import java.util.Collection;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MultiOptions extends Options {
    public MultiOptions addOptions(Options options) {
        if (options == null) {
            return this;
        }
        
        Collection<Option> opts = options.getOptions();
        
        for (Option opt : opts) {
            addOption(opt);
        }
        
        return this;
    }
}
