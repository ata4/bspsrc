/*
 ** 2011 November 3
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceProperties extends Properties {
    
    private static final Logger L = Logger.getLogger(BspSourceProperties.class.getName());
    
    private boolean locked = false;

    public BspSourceProperties() {
        super();
    }

    public BspSourceProperties(Properties defaults) {
        super(defaults);
    }
    
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    @Override
    public synchronized Object setProperty(String key, String value) {
        if (locked) {
            L.log(Level.WARNING, "Properties are locked! Attempted to set {0} = {1}", new Object[]{key, value});
            return null;
        }
        
        L.log(Level.CONFIG, "{0} = {1}", new Object[]{key, value});
        return super.setProperty(key, value);
    }
    
    public synchronized Object setPropertyBoolean(String key, boolean value) {
        return setProperty(key, value ? "true" : "false");
    }
    
    public boolean getPropertyBoolean(String key, boolean defaultValue) {
        String prop = getProperty(key);
        return prop == null ? defaultValue : prop.equals("true");
    }
    
    public synchronized Object setPropertyFloat(String key, float value) {
        return setProperty(key, String.valueOf(value));
    }

    public float getPropertyFloat(String key, float defaultValue) {
        String prop = getProperty(key);
        return prop == null ? defaultValue : Float.valueOf(prop);
    }
}
