package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

import java.util.regex.Pattern;

public class Portal2Def {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Portal 2")
			.setAppId(620)
			.setVersionMin(21)
			.setFilePattern(Pattern.compile("^(mp_coop|sp_a[1-5])_"))
			.setEntities(
					"env_lightrail_endpoint",
					"env_player_viewfinder",
					"env_portal_credits",
					"env_portal_laser",
					"env_portal_path_track",
					"env_sprite_clientside",
					"exploding_futbol_catcher",
					"filter_player_held",
					"func_noportal_volume",
					"func_placement_clip",
					"func_portal_bumper",
					"func_portal_detector",
					"func_portal_orientation",
					"func_portalled",
					"func_weight_button",
					"futbol_catcher",
					"info_coop_spawn",
					"info_landmark_entry",
					"info_landmark_exit",
					"info_paint_sprayer",
					"info_placement_helper",
					"info_player_ping_detector",
					"info_target_instructor_hint",
					"info_target_personality_sphere",
					"linked_portal_door",
					"logic_coop_manager",
					"npc_personality_core",
					"npc_portal_turret_floor",
					"npc_portal_turret_ground",
					"npc_rocket_turret",
					"npc_security_camera",
					"npc_wheatley_boss",
					"paint_sphere",
					"point_changelevel",
					"point_energy_ball_launcher",
					"point_futbol_shooter",
					"point_laser_target",
					"portalmp_gamerules",
					"prop_button",
					"prop_exploding_futbol",
					"prop_exploding_futbol_socket",
					"prop_exploding_futbol_spawner",
					"prop_floor_ball_button",
					"prop_floor_button",
					"prop_floor_cube_button",
					"prop_glados_core",
					"prop_glass_futbol",
					"prop_glass_futbol_socket",
					"prop_glass_futbol_spawner",
					"prop_indicator_panel",
					"prop_laser_catcher",
					"prop_laser_relay",
					"prop_linked_portal_door",
					"prop_mirror",
					"prop_monster_box",
					"prop_paint_bomb",
					"prop_personality_sphere",
					"prop_physics_paintable",
					"prop_portal",
					"prop_portal_stats_display",
					"prop_telescopic_arm",
					"prop_testchamber_door",
					"prop_tic_tac_toe_panel",
					"prop_tractor_beam",
					"prop_under_button",
					"prop_under_floor_button",
					"prop_wall_projector",
					"prop_weighted_cube",
					"trigger_catapult",
					"trigger_paint_cleanser",
					"trigger_ping_detector",
					"trigger_playerteam",
					"trigger_portal_cleanser",
					"vgui_level_placard_display",
					"vgui_movie_display",
					"vgui_mp_lobby_display",
					"vgui_neurotoxin_countdown",
					"vgui_screen",
					"weapon_portalgun"
			)
			.build();
}