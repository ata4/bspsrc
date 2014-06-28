<<<<<<< HEAD
BSPSource
=========

BSPSource is a map decompiler for [Source engine](http://developer.valvesoftware.com/wiki/Source) maps, written in Java.
It decompiles .bsp map files back to .vmf files that can be loaded in Hammer, Valve's official level editor.

BSPSource is based on a reengineered version of [VMEX 0.98g](http://www.bagthorpe.org/bob/cofrdrbob/vmex.html) by Rof, which is no longer developed and lacks
support for newer Source engine games.

Downloads
---------

The latest version is available in the "release" tab. Older versons are available for download [here](http://ata4.info/downloads/apps/bspsource/).

Improvements and changes compared to VMEX 0.98g
-----------------------------------------------

* Support for more and newer Source engine games up to Dota 2.
* Support for new entity types:
	* [func_areaportal](http://developer.valvesoftware.com/wiki/func_areaportal)
	* [func_areaportalwindow](http://developer.valvesoftware.com/wiki/func_areaportalwindow)
	* [func_occluder](http://developer.valvesoftware.com/wiki/func_occluder)
	* [info_lighting](http://developer.valvesoftware.com/wiki/info_lighting)
* Support for the tools/blocklight texture.
* Support for compressed and big-endian encoded maps (XBox 360, PS3)
* Decompiles VMEX maps flagged with protection and at least detects other anti-decompiling methods.
* Improved support for [prop_static](http://developer.valvesoftware.com/wiki/prop_static) and [info_overlay](http://developer.valvesoftware.com/wiki/info_overlay)</a>.
* Improved console output.
* New graphical user interface with output window.
* New command line interface.
* New integrated pakfile extractor.
* Numerous bug fixes.
* Open source.

Limitations and known bugs
--------------------------

* Some internal entities that are entirely consumed by vbsp can't be restored. This includes following entities:
	* [func_instance](http://developer.valvesoftware.com/wiki/func_instance)
	* [func_instance_parms](http://developer.valvesoftware.com/wiki/func_instance_parms)
	* [func_instance_origin](http://developer.valvesoftware.com/wiki/func_instance_origin)
	* [func_viscluster](http://developer.valvesoftware.com/wiki/func_viscluster)
	* [info_no_dynamic_shadow](http://developer.valvesoftware.com/wiki/info_no_dynamic_shadow)
* Areaportal and occluder entities are somewhat difficult to decompile and sometimes have missing brushes or wrong textures.
=======
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

* [ioutils](https://github.com/ata4/ioutils)
* [apache-commons-io-2.4](http://commons.apache.org/io/)
* [apache-commons-compress-1.5](http://commons.apache.org/compress/)
* [apache-commons-lang3-3.1](http://commons.apache.org/cli/)
>>>>>>> e8bd3aa210c62f3a6aaabc40718c944288cd6742
