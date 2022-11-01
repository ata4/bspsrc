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
import info.ata4.bsplib.app.SourceAppId;
import info.ata4.bsplib.nmo.NmoFile;
import info.ata4.bspsrc.BspSource;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.VmfWriter;
import info.ata4.bspsrc.modules.entity.EntitySource;
import info.ata4.bspsrc.modules.geom.*;
import info.ata4.bspsrc.modules.texture.TextureSource;
import info.ata4.bspsrc.util.WindingFactory;
import info.ata4.log.LogUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main decompiling module.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspDecompiler extends ModuleDecompile {

    // logger
    private static final Logger L = LogUtils.getLogger();

    // sub-modules
    private final BspSourceConfig config;
    private final BrushSideFaceMapper brushSideFaceMapper;
    private final TextureSource texsrc;
    private final BrushSource brushsrc;
    private final FaceSource facesrc;
    private final EntitySource entsrc;
    private final BspProtection bspprot;
    private final VmfMeta vmfmeta;

    public BspDecompiler(BspFileReader reader, VmfWriter writer, BspSourceConfig config) {
        super(reader, writer);

        WindingFactory.clearCache();
        BrushUtils.clearCache();

        this.config = config;

        texsrc = new TextureSource(reader);
        bspprot = new BspProtection(reader, texsrc);
        vmfmeta = new VmfMeta(reader, writer);
        brushSideFaceMapper = new BrushSideFaceMapper(reader);
        brushsrc = new BrushSource(reader, writer, config, texsrc, bspprot, vmfmeta, brushSideFaceMapper);
        facesrc = new FaceSource(reader, writer, config, texsrc, vmfmeta);
        entsrc = new EntitySource(reader, writer, config, brushsrc, facesrc,
                texsrc, bspprot, vmfmeta, brushSideFaceMapper);
    }

    /**
     * Starts the decompiling process
     */
    public void start() {
        // fix texture names
        texsrc.setFixTextureNames(config.fixCubemapTextures);
        texsrc.setFixToolTextures(config.fixToolTextures);

        // check for protection and warn if the map has been protected
        if (!config.skipProt) {
            checkProtection();
        }

        // we only need these for brushplanes mode
        if (config.brushMode == BrushMode.BRUSHPLANES) {
            brushSideFaceMapper.load();
        }

        // set comment
        vmfmeta.appendComment("Decompiled by BSPSource v" + BspSource.VERSION + " from " + bspFile.getName());

        // start worldspawn
        vmfmeta.writeWorldHeader();

        // write brushes and displacements
        if (config.writeWorldBrushes) {
            writeBrushes();
        }

        // end worldspawn
        vmfmeta.writeWorldFooter();

        // write entities
        if (config.isWriteEntities()) {
            writeEntities();
        }

        // write visgroups
        if (config.writeVisgroups) {
            vmfmeta.writeVisgroups();
        }

        // write cameras
        if (config.writeCameras) {
            vmfmeta.writeCameras();
        }
    }

    private void checkProtection() {
        if (!bspprot.check()) {
            return;
        }

        L.log(Level.WARNING, "{0} contains anti-decompiling flags or is obfuscated!", reader.getBspFile().getName());
        L.log(Level.WARNING, "Detected methods:");

        List<String> methods = bspprot.getProtectionMethods();

        for (String method : methods) {
            L.warning(method);
        }
    }

    private void writeBrushes() {
        switch (config.brushMode) {
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
        if (config.brushMode == BrushMode.BRUSHPLANES) {
            facesrc.writeDispFaces();
        }
    }

    private void writeEntities() {
        if (config.isWriteEntities()) {
            entsrc.writeEntities();
        }

        if (config.writeBrushEntities && config.writeDetails
                && config.brushMode == BrushMode.BRUSHPLANES) {
            entsrc.writeDetails();
        }

        if (config.writePointEntities) {
            if (config.writeOverlays) {
                entsrc.writeOverlays();
            }

            if (config.writeStaticProps) {
                entsrc.writeStaticProps();
            }

            if (config.writeCubemaps) {
                entsrc.writeCubemaps();
            }

            // Only write func_ladder if game uses object brush based ladders
            // see https://developer.valvesoftware.com/wiki/Working_Ladders
            if (config.writeLadders && !usesNonObjectBrushLadders(bspFile.getAppId())) {
                entsrc.writeLadders();
            }
        }
    }

    /**
     * @see EntitySource#setNmo(NmoFile)
     */
    public void setNmoData(NmoFile nmo) {
        entsrc.setNmo(nmo);
    }

    public static boolean usesNonObjectBrushLadders(int appId) {
        return appId == SourceAppId.COUNTER_STRIKE_GO;
    }
}
