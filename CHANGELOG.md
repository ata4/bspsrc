## Changelog

### 1.3.11 - 23.05.2013
* Added support for newer Vindictus maps.
* Added support for Tactical Intervention map.
* Changed VMF ID mapping system: the hammerid value is now used for entities to restore their original VMF ID.

### 1.3.10 - 12.03.2013
* Fixed an exception when decompiling overlays in the ORIGFACE_PLUS brush mode.
* Disabled toolsinvisible tool texture detection that replaces correct textures as well.
* Added support for overlays for the latest Dota 2 build.
* Added some new features for BSPInfo.

### 1.3.9 - 15.02.2013
* Added full support for Vindictus maps
* Instead of creating one entity for each brush, func_details are now merged to one large entity for all detail brushes. This allows smaller VMF files and avoids some re-compiling problems for complex maps.

### 1.3.8 - 01.10.2012
* Improved support and detection of Vindictus maps
* Fixed wrong detection of Black Mesa maps

### 1.3.7 - 28.08.2012
* Added graphical BSPInfo tool to replace the various command line tools in previous versions
* Improved checking of invalid brushes and brush sides to prevent writing of corrupted VMF files that could crash Hammer
* Fixed a displacement MultiBlend indexing bug
* Fixed protector brushes tied to func_detail not being added to visgroups correctly

### 1.3.6 - 24.04.2012
* Added support for static props in newer CS:GO maps

### 1.3.5 - 23.03.2012
* Added support for displacement multiblend data (thanks to Sander van Noort!)
* VMEX decompile protection entities and brushes are now visgrouped
* New XML-based game detection system
* Fixed Hammer crash bug caused by invalid info_lighting coordinates for Dear Esther maps

### 1.3.4 - 22.02.2012
* Added support for Dear Esther maps
* Fixed some static prop reading errors
* Fixed incorrect detection of App-IDs in lower-case string form (CLI only)
* Fixed writing boolean values as true/false where Hammer expects 1/0

### 1.3.3 - 19.01.2012
* Added detection of CS:GO maps
* Added VMF format selection for old Hammer versions

### 1.3.2 - 02.12.2011
* Added basic support for BSP version 23 (Dota 2)
* Decompiled Vampire: Bloodlines maps with script I/O can now be loaded in the inofficial SDK Hammer without modifications
* Game-specific tool textures in Vampire: Bloodlines maps are no longer changed to tools/toolsnodraw
* Error messages are now logged in both text areas of the GUI so the affected map can be figured out when decompiling multiple maps with errors

### 1.3.1 - 21.11.2011
* Fixed "NoClassDefFoundError" when running on Java 6

### 1.3 - 18.11.2011
* Fixed a few map loading bugs
* Fixed brush sides incorrectly textured with tools/toolsplayerclip, causing "Bad detail brush side" compile errors in VBSP

### 1.3-beta2 - 02.10.11
* Added basic support for BSP version 22 (Dota 2)
* Added various small command line tools for advanced users
* LZMA size differences in compressed lumps are no longer errors
* Improved bsplib code structure

### 1.3-beta - 16.09.2011
* Reworked GUI and CLI
* Files from directories can now be added with drag-and-drop
* Added support for maps from the leaked HL2 beta
* Areaportals can now be decompiled with a flat brush if the original brush can't be found
* Occluders should now always be decompiled correctly. Exact brush dimensions may differ from the original VMF, though.
* Skipping displacements will now create flat brushes without displacement data instead of skipping the brushes entirely
* Overlays should now work in face decompiling modes
* Added additional VMF debug information
* Added a few new configuration options
* Debug information written into the VMF now have keys prefixed with "bspsrc_"
* Moved BSP analysis and pakfile extraction to a separate tool collection which will be released later
* Fixed many bugs for face decompiling modes, results are now close to VMEX again
* Fixed an instance rotation error causing some brushes being flipped around on the y-axis
* Fixed toolsinvisible and toolsnodraw not always being detected correctly
* Tons of refactoring, especially in the classes Winding, FaceSource and BrushSource
* Merged DisplacementSource with FaceSource
* Added many comments found in the original VMEX source code and Source SDK

### 1.2.3 - 24.08.2011
* Added support to select multiple files in GUI
* Improved entity reader, again
* Integrated LZMA library into bsplib
* Restructured bsplib to support file writing

### 1.2.2 - 07.08.2011
* Fixed problems caused by missing model indices that occurred in one map
* Entity rotation correction is now enabled on default again, but is ignored for BSP versions below 21
* Further improved entity reader, fixing some possible bugs with inputs and outputs
* Added detection of Bloody Good Time maps
* Implemented Apache Commons IO for some file/stream operations

### 1.2.1 - 25.05.2011
* Instance entities with fixup names are now automatically grouped to visgroups (prefix only)
* Fixed "Texture axis perpendicular to face" errors when using the rotation fix (hopefully)

### 1.2 - 09.05.2011
* Added support to process all maps inside a directory (CLI only)
* Added correct handling for info_overlay_accessor entities
* Fixed possible game detection bugs
* Changed most remaining messages to use the internal Java logging system

### 1.2-beta2 - 22.04.2011
* Improved game detection and added some Source games
* Added missing option to turn off lump file loading in CLI
* Fixed issue with Portal 2 maps for PC
* Fixed pakfile extraction not updating the suggested destination path when changing the BSP file
* Changed all messages in bsplib to use the internal Java logging system

### 1.2-beta - 18.04.2011
* Added initial support and detection for Portal 2 maps
* Added button to extract the pakfile without decompiling the map in the process
* Fixed bug that prevented info_overlay decompiling from being disabled by the user
* Fixed an uncaught exception caused by invalid vector strings
* The cameras in Hammer are now positioned above the spawn points (info_player_*)
* Re-enabled debug information for bsplib

### 1.1.1 - 12.04.2011
* Added experimental support for big-endian byte order and compressed BSP files (used for PS3/X360)
* Added lump alignment table to structure analysis
* Entity rotation correction is now disabled on default to avoid wrong rotations in older BSP versions
* Files with upper-case file extensions are now recognized correctly
* Fixed possible problems caused by NaN float values
* Changed package names

### 1.1 - 05.03.2011
* Multiplayer Dark Messiah maps are now supported again
* Optimized analysis mode

### 1.1-beta3 - 27.02.2011
* Fixed broken selection of face and back-face textures in CLI
* Fixed freeze when processing the embedded Zip file multiple times

### 1.1-beta2 - 26.02.2011
* Fixed support for singleplayer Dark Messiah maps (multiplayer support is currently broken, though)
* Fixed exception error in the compile parameter analysis and debug mode
* Lowered requirements to Java 5

### 1.1-beta - 24.02.2011
* Added new analysis mode: compile parameters
* Added manual analysis mode selection for GUI and CLI
* Added support for the toolsblock_los texture
* Re-added lost detection of entities with missing class name
* Face and back-face textures are no longer limited to presets and can be chosen as string in CLI mode
* Static props now have shared info_lighting entities
* Texture shifts are now normalized to the texture's width and height
* Fixed "Appearance" values for named lights
* Fixed fatal errors when encountering minor lump length/offset anomalies
* Fixed wrong (back-)face texture selection
* Fixed invalid texinfo indices when decompiling original faces for BSP v20 and above
* Converted most static integer fields to enumerations
* Optimized tool texture handling
* Entity key-values are now stored in maps again, except for the I/O

### 1.0.1 - 25.01.2011
* Fixed entity brush rotation using wrong rotation axes
* Added missing command line parameter for the entity brush rotation fix

### 1.0 - 21.01.2011
* Fixed bug that prevented external lump loading to be turned off
* Fixed exception when opening maps in the same directory as bspsrc.jar
* Fixed some bugs for Zeno Clash maps
* Fixed various other map loading bugs
* Fixed pakfile reading error caused by a bug in Java's Zip implementation
* Minor GUI improvements
* The decompile/analyze buttons will no longer stay disabled on fatal errors
* Added application icon, thanks SpAM_CAN!
* Added support to override automatic game detection with manual selection (command line only for now)
* Added more analysis information
* Moved BSPProtect decryption code to a separate tool
* Created external bsplib library that can be used by other applications

### 1.0-beta - 30.11.2010
* GUI re-design for better usability
* Added option to skip entity and/or brush decompiling entirely
* Added detection for nodraw texture hack by IID_BSP
* Added support for external lump files (.lmp)
* Added support for entity decryption of maps protected with BSPProtect (command line only, requires a key to be passed via parameter -k or -bspprotkey)
* Added support for newer overlay properties (fadedist, cpulevel, gpulevel)
* Added "Analyze" button, which prints information about the currently selected BSP file
* info_ladder entities will now be removed during decompile
* Fixed "null" texture bug for decompiled faces
* Fixed bug that prevented info_cubemap entities to have any brush sides assigned
* Fixed brush entities sometimes having the wrong visible rotation in Hammer (may cause "texture axis perpendicular to face" errors)
* Improved text format for information mode
* Non-debug decompiling is now less verbose
* Changed most command line parameters
* Splitted BspSource class into smaller modules
* Moved some parts of BspReader into the new LumpReader class
* Added Javadoc for many methods and classes

### 0.99.2 - 29.10.2010
* Added support for "Dark Messiah of Might and Magic" and "Vampire: The Masquerade * Bloodlines"
* Added file drag & drop support for the GUI
* Missing entity class names will now be replaced with "unknown_entity" to prevent Hammer crashes (inspired by the mapfix tool for Bloodlines Revival)
* Enhanced game detection
* Fixed false entity obfuscation detection for simple maps

### 0.99.1 - 08.10.2010
* Added detection for entity encryption by BSPProtect
* Added detection for entity obfuscation by IID
* Added information output only mode for command line
* Some GUI improvements, such as custom VMF file selection
* Improved CLI interface

### 0.99 - 15.09.2010
* Renamed to BSPSource and using new versioning scheme
* Improved func_areaportal brush decompiling
* Added support for func_areaportalwindow brushes
* Added experimental support for func_occluder brushes (brush sides may have wrong textures)
* Added support for info_lighting entities
* Added support for toolsblocklight texture
* Improved readability of console output
* Reduced default decimal precision from 8 to 6 to lower rounding errors
* When a map contains decompiling protections, the used methods will be displayed
* Major code refactoring and optimization

### 0.98g_mod7 - 01.09.2010
* Fixed overlays sometimes having too many assigned faces
* Fixed missing entity I/O entries

### 0.98g_mod6 - 24.08.2010
* Fixed overlays with no assigned faces for non-displacement brushes
* Fixed missing func_detail brushes in Alien Swarm maps
* Added very basic support for func_areaportal brushes (no func_areaportalwindow yet, though)
* Improved entity reading code and lots of small cleanups

### 0.98g_mod5 - 14.08.2010
* Fixed wrong entity input/output handling for Alien Swarm
* Improved game detection code

### 0.98g_mod4 - 21.07.2010
* Added support for Alien Swarm, Zeno Clash and The Ship
* New prop_statics are now fully supported (dxlevel, cpulevel, gpulevel, disableX360)
* More code cleanup

### 0.98g_mod3 - 20.06.2010
* The decompiling protection warning will now be displayed for all protection methods
* Code cleanup

### 0.98g_mod2 - 24.04.2010
* Enhanced support for BSP version 21, especially for prop_static
* func_simpleladder entities are now converted to func_ladder
* Fixed some texture name bugs
* Areaportal brushes now have at least the correct texture

### 0.98g_mod1 - 22.03.2010
* Basic support for BSP version 21 (L4D2)
* Fixed a bug when decompiling L4D1 maps
* Fixed some other small bugs
* Lightweight vmf output by removing values that are regenerated by Hammer itself
* Decompiling will no longer be refused when processing protected maps. Instead, a kind warning message is displayed FYI.
* Cleared up console output
* New GUI rewritten from scratch with more settings and a console output window
