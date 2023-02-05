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

import org.apache.commons.lang3.StringUtils;

/**
 * Abstract class for entity I/O values.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityIO {

    public static final char SEP_CHR_OLD = ',';
    public static final char SEP_CHR_NEW = (char) 0x1b;
    public static final String SEP_STR_OLD = Character.toString(SEP_CHR_OLD);
    public static final String SEP_STR_NEW = Character.toString(SEP_CHR_NEW);

    public static boolean isEntityIO(KeyValue kv) {
        String value = kv.getValue();

        // newer format, always 4
        if (StringUtils.countMatches(value, SEP_STR_NEW) == 4) {
            return true;
        }

        // 6 seps for VTMB and Messiah, 4 otherwise
        int matches = StringUtils.countMatches(value, SEP_STR_OLD);
        if (matches == 4 || matches == 6) {
            return true;
        }

        return false;
    }

    private String targetEntity;
    private String input;
    private String param;
    private float delay;
    private int timesToFire;

    public EntityIO(String entityIO) {
        String[] elements = StringUtils.split(entityIO, SEP_CHR_NEW);

        if (elements.length < 4) {
            elements = StringUtils.split(entityIO, SEP_CHR_OLD);
        }

        if (elements.length < 4) {
            throw new IllegalArgumentException("Unsupported I/O format");
        }

        targetEntity = elements[0];
        input = elements[1];
        param = elements[2];
        delay = Float.parseFloat(elements[3]);
        timesToFire = Integer.parseInt(elements[3]);
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public float getDelay() {
        return delay;
    }

    public void setDelay(float delay) {
        this.delay = delay;
    }

    public int getTimesToFire() {
        return timesToFire;
    }

    public void setTimesToFire(int timesToFire) {
        this.timesToFire = timesToFire;
    }

}
