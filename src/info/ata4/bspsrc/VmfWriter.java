/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc;

import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.entity.KeyValue;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.modules.texture.Texture;
import info.ata4.bspsrc.modules.texture.TextureAxis;
import info.ata4.log.LogUtils;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to write formatted VMF files.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class VmfWriter implements Closeable {

    private static final Logger L = LogUtils.getLogger();

    private final PrintWriter pw;
    private final Stack<String> section = new Stack<>();
    private final DecimalFormat decimalFormat = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ENGLISH));
    
    public VmfWriter(File file) throws FileNotFoundException, UnsupportedEncodingException {
        pw = new PrintWriter(file, "US-ASCII");
    }
    
    public VmfWriter(OutputStream os) {
        pw = new PrintWriter(os);
    }
    
    private void indent() {
        for (int i = 0; i < section.size(); i++) {
            pw.print("\t");
        }
    }

    public void start(String name) {
        indent();
        pw.print(name);
        pw.print("\r\n");
        indent();
        pw.print("{\r\n");

        section.push(name);
    }

    public void end(String name) {
        try {
            if (!section.peek().equals(name)) {
                throw new IllegalArgumentException("VMF section end name mismatch: " + name
                        + ", expected " + section.peek());
            }
        } catch (EmptyStackException ex) {
            throw new IllegalArgumentException("No open sections left");
        }

        section.pop();

        indent();
        pw.print("}\r\n");
    }

    public void put(String key, Object value) {
        indent();
        pw.printf("\"%s\" \"%s\"\r\n", key, value);
    }
    
    public void put(String key, int value) {
        put(key, String.valueOf(value));
    }
    
    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }
    
    public void put(String key, float value) {
        put(key, formatFloat(value));
    }
  
    public void put(String key, double value) {
        put(key, formatFloat(value));
    }
    
    public void put(String key, boolean value) {
        put(key, value ? "1" : "0");
    }
    
    public void put(String key, char value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, Vector3f v, int p) {
        put(key, formatVector3f(v, p));
    }

    public void put(String key, Vector3f v) {
        put(key, formatVector3f(v, 0));
    }

    public void put(String key, Vector3f v1, Vector3f v2, Vector3f v3) {
        StringBuilder sb = new StringBuilder();

        sb.append(formatVector3f(v1, 1));
        sb.append(' ');
        sb.append(formatVector3f(v2, 1));
        sb.append(' ');
        sb.append(formatVector3f(v3, 1));

        put(key, sb.toString());
    }

    public void put(String key, TextureAxis axis) {
        put(key, formatTextureAxis(axis));
    }

    public void put(Map<String, String> stringMap) {
        for (String key : stringMap.keySet()) {
            put(key, stringMap.get(key));
        }
    }

    public void put(Entity entity) {
        for (Map.Entry<String, String> kv : entity.getEntrySet()) {
            put(kv.getKey(), kv.getValue());
        }
    }

    public void put(Texture tex) {
        put("material", tex.getTexture());
        put("uaxis", tex.getUAxis());
        put("vaxis", tex.getVAxis());
        put("lightmapscale", tex.getLightmapScale());
    }

    public void put(KeyValue keyValue) {
        put(keyValue.getKey(), keyValue.getValue());
    }

    private String formatVector3f(Vector3f v, int p) {
        StringBuilder sb = new StringBuilder();

        if (p == 1) {
            sb.append('(');
        } else if (p == 2) {
            sb.append('[');
        }

        if (!v.isValid()) {
            L.log(Level.WARNING, "Invalid vector: {0}", v);
            sb.append("0 0 0");
        } else {
            sb.append(formatFloat(v.x)).append(' ');
            sb.append(formatFloat(v.y)).append(' ');
            sb.append(formatFloat(v.z));
        }

        if (p == 1) {
            sb.append(')');
        } else if (p == 2) {
            sb.append(']');
        }

        return sb.toString();
    }

    private String formatTextureAxis(TextureAxis tx) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        
        if (!tx.axis.isValid()) {
            L.log(Level.WARNING, "Invalid vector: {0}", tx.axis);
            sb.append("0 0 0 ");
        } else {
            sb.append(formatFloat(tx.axis.x)).append(' ');
            sb.append(formatFloat(tx.axis.y)).append(' ');
            sb.append(formatFloat(tx.axis.z)).append(' ');
        }
        
        sb.append(formatFloat(tx.shift));
        sb.append("] ");
        sb.append(formatFloat(tx.tw));

        return sb.toString();
    }
    
    private String formatFloat(double f) {
        return decimalFormat.format(f);
    }

    @Override
    public void close() {
        pw.close();

        // stack should be empty, otherwise someone forgot to call end() at least once
        if (!section.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            
            // get stack trace
            Collections.reverse(section);
            
            while (true) {
                sb.append(section.pop());
                
                if (section.isEmpty()) {
                    break;
                }
                
                sb.append(" -> ");
            }
            
            L.log(Level.WARNING, "Unclosed VMF chunk: {0}", sb.toString());
        }
    }
}
