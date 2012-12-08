jbsplib
=======

A basic Source engine BSP file library written in Java.

It is divided into a low-level file reader/writer (BspFile) and a high-level struct reader (BspReader).

Compatibility
-------------

Most Source games are supported thanks to a build-in game database.

Usage
-----

Print some information from the header in d1_trainstation_01.bsp:

```java
try {
	BspFile bspFile = new BspFile(new File("d1_trainstation_01.bsp"));
	
	System.out.println("Name: " + bspFile.getName());
	System.out.println("Version: " + bspFile.getVersion());
	System.out.println("Revision: " + bspFile.getRevision());
	System.out.println("Byte order: " + bspFile.getByteOrder());
} catch (IOException ex) {
	ex.printStackTrace();
}
```


Print the detected game and the class/target names for all entities in d1_canals_01.bsp:

```java
try {
	BspFile bspFile = new BspFile(new File("d1_canals_01.bsp"));

	// get the BSP reader
	BspFileReader bspReader = bspFile.getReader();

	// load just the entities
	bspReader.loadEntities();
	
	// print the detected game
	// BspFile.getSourceApp() will be fully available after the call
	// of BspReader.loadEntities(). Before that, it can only detect
	// few games based on the file header and structure.
	System.out.println("Game: " + bspFile.getSourceApp());
	System.out.println();

	// get the entity list from the loaded data
	List<Entity> entities = bspReader.getData().entities;

	// print the class/target name for each entity
	for (Entity entity : entities) {
			System.out.print(entity.getClassName());
			
			// print target name if available
			if (entity.getTargetName() != null) {
				System.out.print(":" + entity.getTargetName());
			}
			
			System.out.println();
	}
} catch (IOException ex) {
	ex.printStackTrace();
}
```

Removes all entities from tc_hydro.bsp with exception of the worldspawn entity via some low-level lump operations:

```java
try {
	File file = new File("tc_hydro.bsp");
	
	// disable memory mapping by setting second parameter to false
	// so the file can be saved at the same location
	BspFile bspFile = new BspFile(file, false);
	
	// get entity lump
	Lump entLump = bspFile.getLump(LumpType.LUMP_ENTITIES);
	
	// parse the entity lump text and get the worldspawn entity
	EntityInputStream eis = null;
	Entity worldspawn;

	try {
		eis = new EntityInputStream(entLump.getInputStream());
		// read one entity only (worldspawn is always the first one)
		worldspawn = eis.readEntity();
	} finally {
		IOUtils.closeQuietly(eis);
	}
	
	// rebuild lump data that contains only the worldspawn we just
	// read
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	EntityOutputStream eos = null;

	try {
		eos = new EntityOutputStream(baos);
		eos.writeEntity(worldspawn);
		// entity lump always ends with null
		eos.write(0);
	} finally {
		IOUtils.closeQuietly(eos);
	}
	
	// wrap the byte array and update the lump buffer
	entLump.setBuffer(ByteBuffer.wrap(baos.toByteArray()));
	
	// increment revision number (optional)
	bspFile.setRevision(bspFile.getRevision() + 1);
	
	// save the file (in practise, always create backups before!)
	bspFile.save(file);
} catch (IOException ex) {
	ex.printStackTrace();
}
```

Dependencies
------------

* [apache-commons-io-2.0.1](http://commons.apache.org/io/)
* [apache-commons-compress-1.1](http://commons.apache.org/compress/)
* [apache-commons-lang3-3.1](http://commons.apache.org/cli/)