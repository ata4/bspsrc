/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.lib.struct;

/**
 * Enumeration for static prop flags.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum StaticPropFlag {

    STATIC_PROP_FLAG_FADES,             // 0x1
    STATIC_PROP_USE_LIGHTING_ORIGIN,    // 0x2
    STATIC_PROP_NO_DRAW,                // 0x4
    STATIC_PROP_IGNORE_NORMALS,         // 0x8
    STATIC_PROP_NO_SHADOW,              // 0x10
    STATIC_PROP_SCREEN_SPACE_FADE,      // 0x20
    STATIC_PROP_NO_PER_VERTEX_LIGHTING, // 0x40
    STATIC_PROP_NO_SELF_SHADOWING,      // 0x80
    STATIC_PROP_NO_PER_TEXEL_LIGHTING;  // 0x100
}
