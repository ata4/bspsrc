package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

import java.util.regex.Pattern;

public class CounterStrikeSourceDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Counter-Strike: Source")
			.setAppId(SourceAppId.COUNTER_STRIKE_SOURCE)
			.setVersionMin(19)
			.setVersionMax(20)
			.setFilePattern(Pattern.compile("^[de|cs]_"))
			.setEntities(
					"func_bomb_target",
					"func_buyzone",
					"func_footstep_control",
					"func_hostage_rescue",
					"hostage_entity",
					"info_player_counterterrorist",
					"info_player_logo",
					"info_player_terrorist"
			)
			.build();
}