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

import static java.util.Objects.requireNonNull;

/**
 * Brush utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BrushBounds {

    private final WindingFactory windingFactory;
    private final Map<DBrush, AABB> aabbCache = new HashMap<>();

    public BrushBounds(WindingFactory windingFactory) {
        this.windingFactory = requireNonNull(windingFactory);
    }

    /**
     * Returns the bounding box of a brush by combining the bounding boxes of all
     * its brush sides.
     *
     * @param bsp bsp data
     * @param brush a brush
     * @return the bounding box of the brush
     */
    public AABB getBounds(BspData bsp, DBrush brush) {
        // add bounds of all brush sides
        return aabbCache.computeIfAbsent(
                brush,
                dBrush -> IntStream.range(0, brush.numside)
                        .mapToObj(i -> windingFactory.fromSide(bsp, brush, i))
                        .map(Winding::getBounds)
                        .reduce(AABB.ZERO, AABB::include)
        );
    }
}
