package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

import java.util.regex.Pattern;

public class GarrysModDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Garry's Mod")
			.setAppId(SourceAppId.GARRYS_MOD)
			.setVersionMin(20)
			// There are very few hard-coded Garry's Mod entities, so priorize the prefix
			.setPointsFilePattern(5)
			.setFilePattern(Pattern.compile("^(gm(dm)?|mr|ifn|rp|sb|gms|zs|fw)_"))
			// info_gamemode is used in L4D2 as well, but the maps have a different BSP version
			.setEntities(
					"info_gamemode",
					"lua_run"
			)
			.build();
}