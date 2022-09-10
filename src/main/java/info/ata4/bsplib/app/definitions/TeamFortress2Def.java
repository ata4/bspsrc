package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

import java.util.regex.Pattern;

public class TeamFortress2Def {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Team Fortress 2")
			.setAppId(SourceAppId.TEAM_FORTRESS_2)
			.setVersionMin(20)
			.setFilePattern(Pattern.compile("^(arena|cp|ctf|pl(r?)|t[cr]|koth|sd|mvm|bc|rd)_"))
			.setEntities(
					"bot_action_point",
					"bot_generator",
					"bot_hint_engineer_nest",
					"bot_hint_sentrygun",
					"bot_hint_teleporter_exit",
					"bot_roster",
					"dispenser_touch_trigger",
					"entity_spawn_manager",
					"entity_spawn_point",
					"fa_dodgeball_rocket_relay",
					"fa_dodgeball_spawner",
					"filter_activator_tfteam",
					"filter_tf_bot_has_tag",
					"filter_tf_damaged_by_weapon_in_slot",
					"func_capturezone",
					"func_flagdetectionzone",
					"func_nav_avoid",
					"func_nav_prefer",
					"func_nav_prerequisite",
					"func_nobuild",
					"func_nogrenades",
					"func_regenerate",
					"func_respawnflag",
					"func_respawnroom",
					"func_respawnroomvisualizer",
					"func_suggested_build",
					"func_tfbot_hint",
					"func_upgradestation",
					"game_forcerespawn",
					"game_intro_viewpoint",
					"game_round_win",
					"game_text_tf",
					"halloween_zapper",
					"info_observer_point",
					"info_player_teamspawn",
					"item_ammopack_full",
					"item_ammopack_medium",
					"item_ammopack_small",
					"item_healthkit_full",
					"item_healthkit_medium",
					"item_healthkit_small",
					"item_teamflag",
					"mapobj_cart_dispenser",
					"obj_dispenser",
					"obj_sentrygun",
					"obj_teleporter",
					"point_populator_interface",
					"team_control_point",
					"team_control_point_master",
					"team_control_point_round",
					"team_round_timer",
					"team_train_watcher",
					"tf_gamerules",
					"tf_logic_arena",
					"tf_logic_cp_timer",
					"tf_logic_holiday",
					"tf_logic_koth",
					"tf_logic_mann_vs_machine",
					"tf_logic_medieval",
					"tf_logic_multiple_escort",
					"tf_logic_training_mode",
					"tf_point_nav_interface",
					"tf_spell_pickup",
					"tf_zombie_spawner",
					"training_annotation",
					"training_prop_dynamic",
					"trigger_add_tf_player_condition",
					"trigger_capture_area",
					"trigger_ignite_arrows",
					"trigger_stun",
					"trigger_timer_door",
					"wheel_of_doom"
			)
			.build();
}