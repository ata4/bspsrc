/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.struct;

/**
 * Enumeration for surface flags.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum SurfaceFlag {

    SURF_LIGHT,     // value will hold the light strength
    SURF_SKY2D,     // draw skybox instead of texture
    SURF_SKY,       // draw skybox with 3D elements instead of texture
    SURF_WARP,      // turbulent water warp
    SURF_TRANS,     // sort face as a translucent primitive
    SURF_NOPORTAL,  // the surface can not have a portal placed on it (Portal series only?)
    SURF_TRIGGER,   // xbox hack to work around elimination of trigger surfaces
    SURF_NODRAW,    // don't bother referencing the texture
    SURF_HINT,      // treat surface as primary BSP splitter
    SURF_SKIP,      // completely ignore surface, allowing non-closed brushes
    SURF_NOLIGHT,   // non-lit texture, don't calculate light
    SURF_BUMPLIGHT, // calculate three lightmaps for the surface for bumpmapping
    SURF_NOSHADOWS, // don't receive shadows
    SURF_NODECALS,  // don't receive decals
    SURF_NOCHOP,    // don't subdivide patches on this surface 
    SURF_HITBOX;    // surface is part of a hitbox

}
