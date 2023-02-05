/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DLeaf;

/**
 * BSP tree iterator to find the leaf brush/face index minima/maxima.
 * 
 * Original class name: unmap.Treelimit
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspTreeStats {

    private BspData bsp;
    private int bmin;   //min brush
    private int bmax;   //max brush
    private int fmin;   //min face
    private int fmax;   //max face
    private int nmax;   //max node

    public BspTreeStats(BspData bsp) {
        this.bsp = bsp;
        reset();
    }

    public final void reset() {
        bmin = Integer.MAX_VALUE;
        bmax = -1;
        fmin = Integer.MAX_VALUE;
        fmax = -1;
        nmax = -1;
    }

    public void walk(int inode) {
        // if positive, inode is a node index
        // if negative, the value (-1 - child) is the index into the leaf array
        if (inode < 0) {
            int ileaf = -1 - inode;
            DLeaf l = bsp.leaves.get(ileaf);

            // scan leaf faces
            for (int i = 0; i < l.numleafface; i++) {
                int iface = bsp.leafFaces.get(l.fstleafface + i);
                fmax = Math.max(fmax, iface);
                fmin = Math.min(fmin, iface);
            }

            // scan leaf brushes
            for (int i = 0; i < l.numleafbrush; i++) {
                int ibrush = bsp.leafBrushes.get(l.fstleafbrush + i);
                bmax = Math.max(bmax, ibrush);
                bmin = Math.min(bmin, ibrush);
            }
        } else {
            nmax = Math.max(nmax, inode);

            // continue with child nodes
            walk(bsp.nodes.get(inode).children[0]);
            walk(bsp.nodes.get(inode).children[1]);
        }
    }

    public int getMinBrushLeaf() {
        return bmin;
    }

    public int getMaxBrushLeaf() {
        return bmax;
    }

    public int getMinFaceLeaf() {
        return fmin;
    }

    public int getMaxFaceLeaf() {
        return fmax;
    }

    public int getMaxNode() {
        return nmax;
    }
}
