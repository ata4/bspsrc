/*
 ** 2014 June 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.modules.geom;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bspsrc.util.AABB;
import info.ata4.bspsrc.util.WindingFactory;

/**
 * Brush utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BrushUtils {
    
    private BrushUtils() {
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
        AABB bounds = new AABB();
        for (int i = 0; i < brush.numside; i++) {
            bounds.include(WindingFactory.fromSide(bsp, brush, i).getBounds());
        }
        return bounds;
    }
}
