module info.ata4.bspsrc.lib {
    requires info.ata4.bspsrc.common;

    requires org.apache.logging.log4j;
    requires info.ata4.ioutils;
    requires org.apache.commons.compress;
    requires org.tukaani.xz;

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
    exports info.ata4.bspsrc.lib.exceptions;
}