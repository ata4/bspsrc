module info.ata4.bspsrc.lib {
	requires java.logging;

	requires info.ata4.bspsrc.common;
	requires org.apache.commons.lang3;
	requires org.apache.commons.compress;
	requires org.tukaani.xz;
	requires ioutils.b1f26588b5;

	exports info.ata4.bspsrc.lib;
	exports info.ata4.bspsrc.lib.app;
	exports info.ata4.bspsrc.lib.app.definitions;
	exports info.ata4.bspsrc.lib.entity;
	exports info.ata4.bspsrc.lib.io;
	exports info.ata4.bspsrc.lib.io.lumpreader;
	exports info.ata4.bspsrc.lib.lump;
	exports info.ata4.bspsrc.lib.nmo;
	exports info.ata4.bspsrc.lib.struct;
	exports info.ata4.bspsrc.lib.vector;
}