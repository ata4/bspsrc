module info.ata4.bspsrc.app {
	requires java.logging;
	requires java.desktop;
	requires java.sql; // TODO: We obviously don't need that

	requires info.ata4.bspsrc.lib;
	requires info.ata4.bspsrc.decompiler;
	requires org.apache.commons.lang3;
	requires commons.cli;
	requires ioutils.b1f26588b5;
}