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

/**
 * Abstract class for entity I/O values.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityIO {
    
    private String sourceEntity;
    private String targetEntity;
    private String input;
    private String output;
    private String param;
    private float delay;
    private int timesToFire;
    
    public String getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(String sourceEntity) {
        this.sourceEntity = sourceEntity;
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

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
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
