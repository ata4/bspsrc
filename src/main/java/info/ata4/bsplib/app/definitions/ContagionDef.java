package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

import java.util.regex.Pattern;

public class ContagionDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Contagion")
			.setAppId(238430)
			.setVersionMin(27)
			.setFilePattern(Pattern.compile("^(ch|ce|cx)_$"))
			.setEntities(
					"extraction_area",
					"info_extractiongoal",
					"info_extractiontarget",
					"info_goal_zombies",
					"info_gps",
					"info_player_survivor",
					"info_player_zombie",
					"info_random_ammo",
					"info_random_weapon",
					"info_survivor_position",
					"item_ammo_1911",
					"item_ammo_9mm",
					"item_ammo_barricade",
					"item_ammo_rifle",
					"item_ammo_shotgun",
					"prop_barricade",
					"trigger_win",
					"trigger_zombie_spawns",
					"weapon_1911",
					"weapon_ar15",
					"weapon_boltcutter",
					"weapon_extinguisher",
					"weapon_fireaxe",
					"weapon_inoculator",
					"weapon_key",
					"weapon_mossberg",
					"weapon_mp5k",
					"weapon_nailgun",
					"weapon_sig",
					"weapon_sledgehammer"
			)
			.build();
}