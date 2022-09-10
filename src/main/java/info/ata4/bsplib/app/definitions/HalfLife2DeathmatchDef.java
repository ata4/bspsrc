package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

import java.util.regex.Pattern;

public class HalfLife2DeathmatchDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Half-Life 2: Deathmatch")
			.setAppId(320)
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