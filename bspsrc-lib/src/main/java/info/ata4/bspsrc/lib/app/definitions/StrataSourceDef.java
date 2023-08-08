package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

public class StrataSourceDef {
	public static final SourceApp APP = new SourceAppBuilder()
		.setName("Strata Source")
		.setAppId(SourceAppId.STRATA_SOURCE)
		.setVersion(25)
		.build();
}
