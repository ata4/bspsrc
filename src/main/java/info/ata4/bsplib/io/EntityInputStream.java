/*
 ** 2011 June 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.io;

import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.entity.KeyValue;
import info.ata4.log.LogUtils;
import org.apache.commons.io.input.CountingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Enity stream reading class. Converts keyvalue text into Entity objects.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityInputStream implements AutoCloseable {

    private static final Logger L = LogUtils.getLogger();

    private final CountingInputStream in;
    private boolean allowEsc = false;

    public EntityInputStream(InputStream in) {
        if (requireNonNull(in) instanceof CountingInputStream)
            this.in = (CountingInputStream) in;
        else
            this.in = new CountingInputStream(in);
    }

    public Entity readEntity() throws IOException {
        boolean section = false;
        boolean string = false;
        boolean esc = false;
        StringBuilder sb = new StringBuilder(512);

        List<KeyValue> keyValues = new ArrayList<>();
        String key = null;

        try {
            for (int b = 0; b != -1; b = in.read()) {
                switch (b) {
                    case '"':
                        if (!section) {
                            throw new ParseException("String in unopened section", in.getCount());
                        }

                        // ignore '"' if the previous character was '\'
                        if (esc) {
                            esc = false;
                            break;
                        }

                        // parse strings
                        if (string) {
                            if (key == null) {
                                key = sb.toString();
                            } else {
                                String value = sb.toString();

                                // ignore empty keys
                                if (key.isEmpty()) {
                                    L.log(Level.FINE, "Skipped value \"{0}\" with empty key at {1}",
                                            new Object[] {value, in.getCount()});
                                } else {
                                    keyValues.add(new KeyValue(key, value));
                                }

                                key = null;
                            }

                            // empty string buffer
                            sb.delete(0, sb.length());
                        }

                        string = !string;
                        continue;

                    case '{':
                        if (section && !string) {
                            throw new ParseException("Opened unclosed section", in.getCount());
                        }

                        if (!string) {
                            section = true;
                        }
                        break;

                    case '}':
                        if (!section && !string) {
                            throw new ParseException("Closed unopened section", in.getCount());
                        }

                        if (!string) {
                            return new Entity(keyValues);
                        }
                        break;

                    case '\\':
                        if (allowEsc) {
                            // skip this character and add the next '"' to the string
                            esc = true;
                        }
                        break;
                }

                // append to current string if inside section
                if (section && string) {
                    sb.append((char) b);
                }
            }
        } catch (ParseException ex) {
            L.log(Level.WARNING, "{0} at {1}", new Object[]{ex.getMessage(), ex.getErrorOffset()});

            // skip rest of this section by reading until EOF or '}'
            for (int b = 0; b != -1 && b != '}'; b = in.read());

            // return what we've got so far
            return new Entity(keyValues);
        }

        return null;
    }

    public boolean isAllowEscSeq() {
        return allowEsc;
    }

    public void setAllowEscSeq(boolean allowEsc) {
        this.allowEsc = allowEsc;
    }

    @Override
    public void close() throws IOException
    {
        in.close();
    }
}
