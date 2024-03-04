module info.ata4.bspsrc.app {
	requires java.desktop;
	requires java.prefs;

	requires info.ata4.bspsrc.common;
	requires info.ata4.bspsrc.lib;
	requires info.ata4.bspsrc.decompiler;
	requires org.apache.logging.log4j;
	requires org.apache.logging.log4j.core;
	requires info.ata4.ioutils;
	requires org.apache.commons.compress;
	requires info.picocli;
	requires com.formdev.flatlaf;
	requires com.formdev.flatlaf.extras;
	requires com.github.weisj.jsvg;
	requires com.miglayout.core;
	requires com.miglayout.swing;

	opens info.ata4.bspsrc.app.src.cli;
	opens info.ata4.bspsrc.app.util.log.plugins to org.apache.logging.log4j.core;
	opens info.ata4.bspsrc.app.util.swing.ui;
}