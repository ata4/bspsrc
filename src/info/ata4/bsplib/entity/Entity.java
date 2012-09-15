/*
 ** 2011 April 5
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.bsplib.entity;

import info.ata4.bsplib.vector.Vector3f;
import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract entity representation that works roughly like in Hammer. Has two
 * KeyValue lists, one for normal, unique key-values and one for I/O that may
 * contain duplicates.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Entity {

    private static final Logger L = Logger.getLogger(Entity.class.getName());
    
    private Map<String, String> keyValue = new LinkedHashMap<String, String>();
    private List<KeyValue> keyValueIO = new ArrayList<KeyValue>();
    private String className;

    /**
     * Creates a new empty entity with the given class name.
     *
     * @param className entity class name, must not be null or empty
     */
    public Entity(String className) {
        if (className == null) {
            throw new NullPointerException();
        }

        if (className.length() == 0) {
            throw new IllegalArgumentException("Empty class name");
        }

        this.className = className;
    }

    /**
     * Creates a new entity from a list of raw key-values.
     *
     * @param kvList raw key-value list
     */
    public Entity(List<KeyValue> kvList) {
        String sepChr = String.valueOf((char) 0x1b);
        
        for (KeyValue kv : kvList) {
            String key = kv.getKey();
            String value = kv.getValue();

            // special KV, don't add it
            if (key.equals("classname")) {
                if (className == null) {
                    className = value;
                } else {
                    L.log(Level.WARNING, "Found duplicate classname key, ignoring {0}", kv);
                }
                continue;
            }

            // search for escape separator chars
            int sep = StringUtils.countMatches(value, sepChr);

            if (sep == 0) {
                // try comma, too
                sep = StringUtils.countMatches(value, ",");
            }

            // 6 seps for VTMB and Messiah, 4 otherwise
            boolean io = sep == 4 || sep == 6;

            if (io) {
                keyValueIO.add(kv);
            } else {
                keyValue.put(key, value);
            }
        }

        // check and add missing class name
        if (className == null || className.isEmpty()) {
            L.log(Level.WARNING, "Missing or empty class name, using \"unknown_entity\"");
            className = "unknown_entity";
        }
    }

    public List<KeyValue> getIO() {
        return keyValueIO;
    }

    public Set<String> getKeys() {
        return keyValue.keySet();
    }

    public Collection<String> getValues() {
        return keyValue.values();
    }

    public Set<Entry<String, String>> getEntrySet() {
        return keyValue.entrySet();
    }

    public boolean hasKey(String key) {
        return keyValue.containsKey(key);
    }

    public String getValue(String key) {
        return keyValue.get(key);
    }

    public void setValue(String key, Object value) {
        keyValue.put(key, String.valueOf(value));
    }

    public void setValue(KeyValue kv) {
        setValue(kv.getKey(), kv.getValue());
    }

    public void removeValue(String key) {
        keyValue.remove(key);
    }

    public void clear() {
        keyValue.clear();
        keyValueIO.clear();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String value) {
        className = value;
    }

    public String getTargetName() {
        return getValue("targetname");
    }

    public void setTargetName(String value) {
        setValue("targetname", value);
    }

    public Vector3f getVector3f(String key) {
        String str = getValue(key);

        if (str == null) {
            return null;
        }

        // parse origin values
        try {
            // split string by whitespaces
            String[] costr = StringUtils.split(str, ' ');

            float x = costr.length > 0 ? Float.parseFloat(costr[0]) : 0;
            float y = costr.length > 1 ? Float.parseFloat(costr[1]) : 0;
            float z = costr.length > 2 ? Float.parseFloat(costr[2]) : 0;

            return new Vector3f(x, y, z);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void setVector3f(String key, Vector3f value) {
        setValue(key, value.x + " " + value.y + " " + value.z);
    }

    public Vector3f getOrigin() {
        return getVector3f("origin");
    }

    public void setOrigin(Vector3f origin) {
        setVector3f("origin", origin);
    }

    public Vector3f getAngles() {
        Vector3f a = getVector3f("angles");

        if (a == null) {
            return null;
        }

        // swap pitch yaw roll (Y Z X) axes
        return new Vector3f(a.z, -a.x, a.y);
    }

    public void setAngles(Vector3f a) {
        setVector3f("angles", new Vector3f(a.z, -a.x, a.y));
    }

    /**
     * Returns the model number for this entity.
     *
     * @return The model number of this entity. -1 if no valid model number was
     * assigned or -2 if this entity is a prop.
     */
    public int getModelNum() {
        String model = getValue("model");

        if (model == null) {
            // no model
            return -1;
        } else if (model.startsWith("*")) {
            try {
                // get model id
                return Integer.parseInt(model.substring(1));
            } catch (NumberFormatException ex) {
                return -2;
            }
        } else if (model.length() == 0) {
            // worldspawn
            return 0;
        } else {
            // studio model or invalid model format
            return -2;
        }
    }

    public void setModelNum(int modelnum) {
        setValue("model", "*" + modelnum);
    }

    /**
     * Prints all key-values to a PrintStream.
     *
     * @param ps PrintStream to write to
     */
    public void dump(PrintStream ps) {
        ps.println(getClassName() + ":");

        for (String key : keyValue.keySet()) {
            String value = keyValue.get(key);
            if (key.equals("classname")) {
                continue;
            }
            ps.println("  " + key + " = " + value);
        }

        for (KeyValue kv : keyValueIO) {
            ps.println("  " + kv.getKey() + ": " + kv.getValue());
        }

        ps.println();
    }

    /**
     * Prints all key-values to standard output
     */
    public void dump() {
        dump(System.out);
    }

    @Override
    public String toString() {
        return getClassName() + (getTargetName() == null ? "" : " (" + getTargetName() + ")");
    }
}
