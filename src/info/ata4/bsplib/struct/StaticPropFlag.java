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
 * Enumeration for static prop flags.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum StaticPropFlag {

    STATIC_PROP_FLAG_FADES,
    STATIC_PROP_USE_LIGHTING_ORIGIN,
    STATIC_PROP_NO_DRAW,
    STATIC_PROP_IGNORE_NORMALS,
    STATIC_PROP_NO_SHADOW,
    STATIC_PROP_UNUSED,
    STATIC_PROP_NO_PER_VERTEX_LIGHTING,
    STATIC_PROP_NO_SELF_SHADOWING;
}
