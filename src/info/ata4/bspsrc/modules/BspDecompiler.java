/*
** 2011 April 5
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
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.Camera;
import info.ata4.bspsrc.VmfWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main decompiling module.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspDecompiler extends BspSourceVmfModule {

    // logger
    private static final Logger L = Logger.getLogger(BspDecompiler.class.getName());

    // sub-modules
    private final TextureSource texsrc;
    private final BrushSource brushsrc;
    private final FaceSource facesrc;
    private final EntitySource entsrc;
    private final BspProtection bspprot;
    
    // serial brush and brush side IDs
    private int brushID = 1;
    private int sideID = 1;
    
    // visgroup list
    private List<String> visgroups = new ArrayList<String>();
    
    // camera list
    private List<Camera> cameras = new ArrayList<Camera>();

    private Entity worldspawn;
    private String comment;

    public BspDecompiler(BspSourceConfig config, BspFileReader reader, VmfWriter writer) {
        super(config, reader, writer);

        texsrc = new TextureSource(this);
        brushsrc = new BrushSource(this);
        facesrc = new FaceSource(this);
        entsrc = new EntitySource(this);
        bspprot = new BspProtection(this);
        
        worldspawn = bsp.entities.get(0);
    }

    public TextureSource getTextureSource() {
        return texsrc;
    }

    public BrushSource getBrushSource() {
        return brushsrc;
    }

    public FaceSource getFaceSource() {
        return facesrc;
    }

    public EntitySource getEntitySource() {
        return entsrc;
    }

    public BspProtection getBspProtection() {
        return bspprot;
    }

    /**
     * Starts the decompiling process
     */
    public void start() {
        // fix texture names
        if (config.isFixCubemapTextures()) {
            texsrc.fixCubemapTextures();
        }

        // check for existing map comment
        if (worldspawn.getValue("comment") != null) {
            L.log(Level.INFO, "Map comment: {0}", worldspawn.getValue("comment"));
        }

        // check for protection and warn if the map has been protected
        if (!config.isSkipProtection()) {
            checkProtection();
        }

        // write worldspawn
        writeHeader();

        // write brushes and displacements
        if (config.isWriteWorldBrushes()) {
            writeBrushes();
        }

        // end worldspawn section
        writeFooter();

        // write entities
        if (config.isWriteEntities()) {
            writeEntities();
        }
        
        // write visgroups
        if (config.isWriteVisgroups()) {
            writeVisgroups();
        }
        
        // write cameras
        if (config.isWriteCameras()) {
            writeCameras();
        }
    }

    private void checkProtection() {
        if (!bspprot.check()) {
            return;
        }

        L.log(Level.WARNING, "{0} contains anti-decompiling flags or is obfuscated!", reader.getBspFile().getName());
        L.warning("Detected methods:");
        
        List<String> methods = bspprot.getProtectionMethods();
        
        for (String method : methods) {
            L.warning(method);
        }
    }

    private void writeBrushes() {
        switch (config.getBrushMode()) {
            case BRUSHPLANES:
                brushsrc.writeBrushes();
                break;
            case ORIGFACE:
                facesrc.writeOrigFaces();
                break;
            case ORIGFACE_PLUS:
                facesrc.writeOrigFacesPlus();
                break;
            case SPLITFACE:
                facesrc.writeFaces();
                break;
            default:
                break;
        }
        
        // add faces with displacements
        // face modes don't need to do this separately
        if (config.isBrushMode()) {
            facesrc.writeDispFaces();
        }
    }

    private void writeEntities() {
        if (config.isWriteBrushEntities() || config.isWritePointEntities()) {
            entsrc.writeEntities();
        }

        if (config.isWriteBrushEntities() && config.isWriteDetails() && config.isBrushMode()) {
            entsrc.writeDetails();
        }

        if (config.isWritePointEntities()) {
            if (config.isWriteOverlays()) {
                entsrc.writeOverlays();
            }

            if (config.isWriteStaticProps()) {
                entsrc.writeStaticProps();
            }

            if (config.isWriteCubemaps()) {
                entsrc.writeCubemaps();
            }
        }
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
    
    public int getBrushID() {
        return brushID;
    }
    
    public int getSideID() {
        return sideID;
    }
    
    public int nextBrushID() {
        return brushID++;
    }
    
    public int nextSideID() {
        return sideID++;
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
    public void writeHeader() {
        writer.start("world");
        writer.put("id", nextBrushID());
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
    public void writeFooter() {
        writer.end("world");
    }
}
