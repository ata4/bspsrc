package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

import java.util.regex.Pattern;

public class CounterStrikeSourceDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Counter-Strike: Source")
			.setAppId(SourceAppId.COUNTER_STRIKE_SOURCE)
			.setVersionRange(19, 20)
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