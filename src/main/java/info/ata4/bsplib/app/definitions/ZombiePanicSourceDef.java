package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

import java.util.regex.Pattern;

public class ZombiePanicSourceDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Zombie Panic! Source")
			.setAppId(SourceAppId.ZOMBIE_PANIC_SOURCE)
			.setVersionMin(20)
			.setFilePattern(Pattern.compile("^zp[aols]_"))
			.setEntities(
					"func_humanclip",
					"func_zombieclip",
					"game_win_human",
					"game_win_zombie",
					"info_player_common",
					"info_player_human",
					"info_player_observer",
					"info_player_zombie",
					"objective_zpa",
					"objective_zpo",
					"random_ammo",
					"random_any",
					"random_def",
					"random_firearm",
					"random_limit",
					"random_melee",
					"random_misc",
					"random_pistol",
					"random_rifle",
					"random_shotrev",
					"random_weapon",
					"trigger_capturepoint_zp",
					"trigger_joinhumanteam",
					"trigger_joinspectatorteam",
					"trigger_joinzombieteam"
			)
			.build();
}