BSPSource
=========

BSPSource is a map decompiler for [Source engine](http://developer.valvesoftware.com/wiki/Source) maps, written in Java.
It decompiles .bsp map files back to .vmf files that can be loaded in Hammer, Valve's official level editor.

BSPSource is based on a reengineered version of [VMEX 0.98g](http://www.bagthorpe.org/bob/cofrdrbob/vmex.html) by Rof, which is no longer developed and lacks
support for newer Source engine games.

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

Dependencies
------------

* [apache-commons-io-2.0.1](http://commons.apache.org/io/)
* [apache-commons-compress-1.1](http://commons.apache.org/compress/)
* [apache-commons-cli-1.2](http://commons.apache.org/cli/)
* [apache-commons-lang3-3.1](http://commons.apache.org/cli/)