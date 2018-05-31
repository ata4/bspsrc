/*
 ** 2011 August 26
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.io;

import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.entity.KeyValue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map.Entry;

/**
 * Enity stream writing class. Converts Entity objects text into keyvalue text.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityOutputStream extends PrintStream {

    public EntityOutputStream(OutputStream out) {
        super(out);
    }

    public void writeEntity(Entity ent) throws IOException {
        print("{\n");

        for (Entry<String, String> kv : ent.getEntrySet()) {
            printf("\"%s\" \"%s\"\n", kv.getKey(), kv.getValue());
        }

        printf("\"classname\" \"%s\"\n", ent.getClassName());

        List<KeyValue> ios = ent.getIO();

        for (KeyValue io : ios) {
            printf("\"%s\" \"%s\"\n", io.getKey(), io.getValue());
        }

        print("}\n");
    }
}
