package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

import java.util.regex.Pattern;

public class Dota2Def {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Dota 2")
			.setAppId(570)
			.setVersionMin(22)
			.setVersionMax(23)
			.setFilePattern(Pattern.compile("^(dota|tutorial)_"))
			.setEntities(
					"ambient_creatures",
					"ambient_creatures_zone",
					"dota_displacement_visibility",
					"dota_item_rune_spawner",
					"dota_minimap_boundary",
					"dota_prop_customtexture",
					"dota_world_particle_system",
					"ent_dota_dire_candybucket",
					"ent_dota_fountain",
					"ent_dota_game_events",
					"ent_dota_halloffame",
					"ent_dota_lightinfo",
					"ent_dota_radiant_candybucket",
					"ent_dota_shop",
					"ent_dota_tree",
					"ent_fow_blocker_node",
					"ent_sugar_rush",
					"info_courier_spawn_dire",
					"info_courier_spawn_radiant",
					"info_player_start_badguys",
					"info_player_start_goodguys",
					"info_roquelaire_perch",
					"npc_dota_barracks",
					"npc_dota_building",
					"npc_dota_fort",
					"npc_dota_neutral_spawner",
					"npc_dota_roshan_spawner",
					"npc_dota_scripted_spawner",
					"npc_dota_spawner",
					"npc_dota_spawner_bad_bot",
					"npc_dota_spawner_bad_mid",
					"npc_dota_spawner_bad_top",
					"npc_dota_spawner_good_bot",
					"npc_dota_spawner_good_mid",
					"npc_dota_spawner_good_top",
					"npc_dota_tower",
					"prop_player_cosmetic",
					"trigger_boss_attackable",
					"trigger_hero",
					"trigger_no_wards",
					"trigger_shop",
					"tutorial_npc_blocker",
					"world_bounds"
			)
			.build();
}