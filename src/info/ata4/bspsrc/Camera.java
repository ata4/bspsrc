/*
 ** 2011 September 7
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc;

import info.ata4.bsplib.vector.Vector3f;

/**
 * Structure for a Hammer viewport camera.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Camera {
    
    public final Vector3f pos;
    public final Vector3f look;
    
    public Camera(Vector3f pos, Vector3f look) {
        this.pos = pos;
        this.look = look;
    }
}
