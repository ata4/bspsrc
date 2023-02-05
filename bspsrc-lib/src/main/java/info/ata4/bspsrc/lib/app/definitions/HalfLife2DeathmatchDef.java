package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

import java.util.regex.Pattern;

public class HalfLife2DeathmatchDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Half-Life 2: Deathmatch")
			.setAppId(SourceAppId.HALF_LIFE_2_DEATHMATCH)
			.setVersionMin(19)
			.setVersionMax(20)
			.setFilePattern(Pattern.compile("^dm_"))
			.setPointsEntities(3)
			.setEntities(
					"info_player_combine",
					"info_player_deathmatch",
					"info_player_rebel"
			)
			.build();
}