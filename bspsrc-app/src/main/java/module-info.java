module info.ata4.bspsrc.app {
	requires java.logging;
	requires java.desktop;
	requires java.sql; // TODO: We obviously don't need that

	requires info.ata4.bspsrc.common;
	requires info.ata4.bspsrc.lib;
	requires info.ata4.bspsrc.decompiler;
	requires ioutils.b1f26588b5;
	requires org.apache.commons.compress;
	requires info.picocli;
	requires com.formdev.flatlaf;
	requires com.jthemedetector;

	opens info.ata4.bspsrc.app.src.cli to info.picocli;
}