/*
** 2012 June 3
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
import info.ata4.bspsrc.modules.texture.TextureSource;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * BSP resource dependencies scanner.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspDependencies extends ModuleRead {
    
    public BspDependencies(BspFileReader reader) {
        super(reader);
        
        reader.loadEntities();
    }

    public Set<String> getMaterials() {
        Set<String> materials = new TreeSet<>();
        TextureSource texsrc = new TextureSource(reader);

        // add all texnames
        for (String texname : texsrc.getFixedTextureNames()) {
            materials.add("materials/" + texname + ".vmt");
        }
        
        // add all entity materials
        for (Entity ent : bsp.entities) {
            try {
                for (String value : ent.getValues()) {

                    if (value.startsWith("materials/")
                            || value.endsWith(".vtf")
                            || value.endsWith(".vmt")) {
                        String texture = texsrc.canonizeTextureName(value);
                    
                        if (!texture.startsWith("materials/")) {
                            texture = "materials/" + texture;
                        }
                        
                        if (!texture.endsWith(".vtf") && !texture.endsWith(".vmt")) {
                            texture += ".vmt";
                        }
                        
                        materials.add(texture);
                    }
                }
            } catch (NullPointerException ex) {
            }
        }
        
        return materials;
    }
    
    public Set<String> getModels() {
        reader.loadStaticProps();
        
        TreeSet<String> models = new TreeSet<>();
        
        // add entity models
        for (Entity ent : bsp.entities) {
            try {
                for (String value : ent.getValues()) {
                    if (value.endsWith(".mdl")) {
                        models.add(value);
                    }
                }
            } catch (NullPointerException ex) {
            }
        }
        
        // add static prop models
        models.addAll(bsp.staticPropName);
        
        return models;
    }
    
    public Set<String> getSoundFiles() {
        Set<String> soundFiles = new TreeSet<>();
        
        for (Entity ent : bsp.entities) {
            for (Map.Entry<String, String> kv : ent.getEntrySet()) {
                String value = kv.getValue();

                // raw sound extensions
                if (value.startsWith("sound/")
                        || value.endsWith(".wav")
                        || value.endsWith(".mp3")) {

                    if (!value.startsWith("sound/")) {
                        value = "sound/" + value;
                    }

                    soundFiles.add(value);
                }
            }
        }
        
        return soundFiles;
    }
    
 
    public Set<String> getSoundScripts() {
        Set<String> soundScripts = new TreeSet<>();

        for (Entity ent : bsp.entities) {
            for (Map.Entry<String, String> kv : ent.getEntrySet()) {
                // soundscapes are not our job
                if (ent.getClassName().equals("env_soundscape")) {
                    continue;
                }
                
                String key = kv.getKey();
                String value = kv.getValue();

                // heuristic soundscript detection
                if (key.contains("sound") || key.contains("noise")) {
                    // ignore empty strings, scripted sentences and numeric values
                    if (value == null
                            || value.length() == 0
                            || value.startsWith("!")
                            || value.matches("^-?[0-9]+$")) {
                        continue;
                    }

                    soundScripts.add(value);
                }
            }

        }

        return soundScripts;
    }

    public Set<String> getSoundscapes() {
        Set<String> soundScapes = new TreeSet<>();

        for (Entity ent : bsp.entities) {
            if (ent.getClassName().equals("env_soundscape")) {
                soundScapes.add(ent.getValue("soundscape"));
            }
        }

        return soundScapes;
    }
    
    public Set<String> getParticles() {
        Set<String> particles = new TreeSet<>();
        
        for (Entity ent : bsp.entities) {
            try {
                if (ent.getClassName().equals("info_particle_system")) {
                    particles.add(ent.getValue("effect_name"));
                }
            } catch (NullPointerException ex) {
            }
        }
        
        return particles;
    }
}
