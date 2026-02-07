/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.decompiler;

import info.ata4.bspsrc.decompiler.modules.texture.Texture;
import info.ata4.bspsrc.decompiler.modules.texture.TextureAxis;
import info.ata4.bspsrc.lib.entity.Entity;
import info.ata4.bspsrc.lib.entity.KeyValue;
import info.ata4.bspsrc.lib.vector.Vector3d;
import info.ata4.bspsrc.lib.vector.Vector3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to write formatted VMF files.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class VmfWriter implements Closeable {

    private static final Logger L = LogManager.getLogger();

    private final PrintWriter pw;
    private final Deque<String> section = new ArrayDeque<>();

    private final int doubleScale;
    private final int doubleScaleTextureAxes;
    private final int doubleScaleTextureScale;

    public VmfWriter(
            PrintWriter pw,
            int doubleScale,
            int doubleScaleTextureAxes,
            int doubleScaleTextureScale
    ) {
        this.pw = pw;
        this.doubleScale = doubleScale;
        this.doubleScaleTextureAxes = doubleScaleTextureAxes;
        this.doubleScaleTextureScale = doubleScaleTextureScale;
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

        section.addFirst(name);
    }

    public void end(String name) {
        var s = section.peekFirst();
        if (s == null)
            throw new NoSuchElementException("No open sections left");
        if (!s.equals(name))
            throw new NoSuchElementException("VMF section end name mismatch. Expected: '%s', got '%s'".formatted(name, s));

        section.removeFirst();

        indent();
        pw.print("}\r\n");
    }

    public void put(String key, String value) {
        indent();
        pw.printf("\"%s\" \"%s\"\r\n", key, value);
    }

    public void put(String key, int value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, int... values) {
        put(key, Arrays.stream(values).mapToObj(Integer::toString).collect(Collectors.joining(" ")));
    }

    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, float value) {
        put(key, formatFloat(value, doubleScale));
    }

    public void put(String key, double value) {
        put(key, formatFloat(value, doubleScale));
    }

    public void put(String key, boolean value) {
        put(key, value ? "1" : "0");
    }

    public void put(String key, char value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, Vector3f v, int p) {
        put(key, formatVector3d(v.toDouble(), p));
    }

    public void put(String key, Vector3d v, int p) {
        put(key, formatVector3d(v, p));
    }

    public void put(String key, Vector3f v) {
        put(key, v, 0);
    }

    public void put(String key, Vector3d v) {
        put(key, v, 0);
    }

    public void put(String key, Vector3d v1, Vector3d v2, Vector3d v3) {
        put(key, formatVector3d(v1, 1) + " "
                + formatVector3d(v2, 1) + " "
                + formatVector3d(v3, 1));
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

    private String formatVector3d(Vector3d v, int p) {
        StringBuilder sb = new StringBuilder();

        if (p == 1) {
            sb.append('(');
        } else if (p == 2) {
            sb.append('[');
        }

        if (!v.isValid()) {
            L.warn("Invalid vector: {}", v);
            sb.append("0 0 0");
        } else {
            sb.append(formatFloat(v.x(), doubleScale)).append(' ');
            sb.append(formatFloat(v.y(), doubleScale)).append(' ');
            sb.append(formatFloat(v.z(), doubleScale));
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
            L.warn("Invalid vector: {}", tx.axis);
            sb.append("0 0 0 ");
        } else {
            sb.append(formatFloat(tx.axis.x(), doubleScaleTextureAxes)).append(' ');
            sb.append(formatFloat(tx.axis.y(), doubleScaleTextureAxes)).append(' ');
            sb.append(formatFloat(tx.axis.z(), doubleScaleTextureAxes)).append(' ');
        }

        sb.append(tx.shift);
        sb.append("] ");
        sb.append(formatFloat(tx.tw, doubleScaleTextureScale));

        return sb.toString();
    }

    private static final Map<Integer, DecimalFormat> FORMATTERS = new HashMap<>();
    private static DecimalFormat createFormatter(int decimalPlaces) {
        return new DecimalFormat("0." + "#".repeat(decimalPlaces), new DecimalFormatSymbols(Locale.ENGLISH));
    }

    private String formatFloat(double f, int decimalPlaces) {
        if (decimalPlaces == 0)
            return Double.toString(f);

        return FORMATTERS
                .computeIfAbsent(decimalPlaces, VmfWriter::createFormatter)
                .format(f);
    }

    @Override
    public void close() {
        pw.close();

        // stack should be empty, otherwise someone forgot to call end() at least once
        if (!section.isEmpty()) {
            var stackState = String.join(" -> ", (Iterable<String>) section::descendingIterator);
            L.warn("Unclosed VMF chunk: {}", stackState);
        }
    }
}
