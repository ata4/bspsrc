/*
 ** 2011 August 6
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package com.nuclearvelocity.barracuda.bsplib.entity;

import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Test {
    public static void main(String[] args) {
        File file = new File("D:/Temp/bsp/_rawent/vampire.ent");
        
        try {
            FileReader reader = new FileReader(file);
            
            EntityReader entReader = new EntityReader(reader);
            entReader.parse();
            
            List<Entity> ents = entReader.getEntities();
            
            for (Entity ent : ents) {
                List<KeyValue> io = ent.getIO();
                                
                for (KeyValue kv : io) {
                    System.out.println(kv.getKey() + "\t" + kv.getValue());
                }
            }
            
            reader.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
