/*
 ** 2014 June 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.decompiler.modules.geom;

import info.ata4.bspsrc.decompiler.util.AABB;
import info.ata4.bspsrc.decompiler.util.Winding;
import info.ata4.bspsrc.decompiler.util.WindingFactory;
import info.ata4.bspsrc.lib.struct.BspData;
import info.ata4.bspsrc.lib.struct.DBrush;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Brush utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BrushUtils {

    private static final Map<DBrush, AABB> AABB_CACHE = new HashMap<>();

    private BrushUtils() {
    }

    public static void clearCache() {
        AABB_CACHE.clear();
    }

    /**
     * Returns the bounding box of a brush by combining the bounding boxes of all
     * its brush sides.
     *
     * @param bsp bsp data
     * @param brush a brush
     * @return the bounding box of the brush
     */
    public static AABB getBounds(BspData bsp, DBrush brush) {
        // add bounds of all brush sides
        return AABB_CACHE.computeIfAbsent(
                brush,
                dBrush -> IntStream.range(0, brush.numside)
                        .mapToObj(i -> WindingFactory.fromSide(bsp, brush, i))
                        .map(Winding::getBounds)
                        .reduce(AABB.ZERO, AABB::include)
        );
    }
}
