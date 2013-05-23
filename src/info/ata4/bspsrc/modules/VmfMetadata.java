/*
 ** 2013 May 23
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.modules;

import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.entity.Entity;
import info.ata4.bspsrc.VmfWriter;
import info.ata4.bspsrc.modules.entity.Camera;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class VmfMetadata extends ModuleDecompile {
    
    // logger
    private static final Logger L = Logger.getLogger(VmfMetadata.class.getName());
    
    // sub-modules
    private final IDTracker idtracker;
    
    // visgroup list
    private List<String> visgroups = new ArrayList<String>();
    
    // camera list
    private List<Camera> cameras = new ArrayList<Camera>();

    private Entity worldspawn;
    private String comment;

    public VmfMetadata(BspFileReader reader, VmfWriter writer, IDTracker idtracker) {
        super(reader, writer);
        
        this.idtracker = idtracker;
        
        worldspawn = bsp.entities.get(0);
        
        // check for existing map comment
        if (worldspawn.getValue("comment") != null) {
            L.log(Level.INFO, "Map comment: {0}", worldspawn.getValue("comment"));
        }
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
    
    /**
     * Writes the worldspawn header
     */
    public void writeWorldHeader() {
        writer.start("world");
        writer.put("id", idtracker.getUID());
        writer.put(worldspawn);
        
        // write comment
        if (comment != null) {
            writer.put("comment", comment);
        }
        
        writer.put("classname", "worldspawn");
    }
    
    /**
     * Writes the worldspawn footer
     */
    public void writeWorldFooter() {
        writer.end("world");
    }
    
    public void writeVisgroups() {
        if (visgroups.isEmpty()) {
            return;
        }

        String[] visgroupArray = visgroups.toArray(new String[0]);

        writer.start("visgroups");

        for (String visgroup : visgroupArray) {
            writer.start("visgroup");
            writer.put("name", visgroup);
            writer.put("visgroupid", visgroups.indexOf(visgroup));
            writer.end("visgroup");
        }

        writer.end("visgroups");
    }
    
    public void writeMetaVisgroup(String visgroupName) {
        writer.start("editor");
        writer.put("visgroupid", getVisgroupID(visgroupName));
        writer.end("editor");
    }
    
    public void writeMetaVisgroups(List<String> visgroupNames) { 
        writer.start("editor");
        for (String visgroupName : visgroupNames) {
            writer.put("visgroupid", getVisgroupID(visgroupName));
        }
        writer.end("editor");
    }
    
    public int getVisgroupID(String visgroupName) {
        if (!visgroups.contains(visgroupName)) {
            visgroups.add(visgroupName);
        }
        
        return visgroups.indexOf(visgroupName);
    }
    
    public void writeCameras() {
        writer.start("cameras");

        if (cameras.isEmpty()) {
            writer.put("activecamera", -1);
        } else {
            writer.put("activecamera", 0);

            for (Camera camera : cameras) {
                writer.start("camera");
                writer.put("position", camera.pos, 2);
                writer.put("look", camera.look, 2);
                writer.end("camera");
            }
        }
        
        writer.end("cameras");
    }
    
    public List<Camera> getCameras() {
        return cameras;
    }
}
