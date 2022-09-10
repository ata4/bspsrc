package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

import java.util.regex.Pattern;

public class Left4DeadDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Left 4 Dead")
			.setAppId(500)
			.setVersionMin(20)
			.setFilePattern(Pattern.compile("^l4d_"))
			.setEntities(
					"env_outtro_stats",
					"env_player_blocker",
					"env_rock_launcher",
					"env_tonemap_controller_ghost",
					"env_tonemap_controller_infected",
					"env_weaponfire",
					"filter_activator_infected_class",
					"func_button_timed",
					"func_elevator",
					"func_nav_avoidance_obstacle",
					"func_nav_blocker",
					"func_nav_stairs_toggle",
					"func_orator",
					"func_playerinfected_clip",
					"func_ragdoll_fader",
					"func_spawn_volume",
					"info_director",
					"info_elevator_floor",
					"info_goal_infected_chase",
					"info_map_parameters_versus",
					"info_survivor_position",
					"info_survivor_rescue",
					"info_zombie_spawn",
					"logic_versus_random",
					"point_deathfall_camera",
					"point_viewcontrol_survivor",
					"prop_car_alarm",
					"prop_car_glass",
					"prop_door_rotating_checkpoint",
					"prop_fuel_barrel",
					"prop_glowing_object",
					"prop_health_cabinet",
					"prop_minigun",
					"prop_mounted_machine_gun",
					"trigger_auto_crouch",
					"trigger_escape",
					"trigger_finale",
					"trigger_finale_dlc3",
					"trigger_hurt_ghost",
					"weapon_ammo_spawn",
					"weapon_autoshotgun_spawn",
					"weapon_first_aid_kit_spawn",
					"weapon_hunting_rifle_spawn",
					"weapon_molotov_spawn",
					"weapon_pain_pills_spawn",
					"weapon_pipe_bomb_spawn",
					"weapon_pistol_spawn",
					"weapon_pumpshotgun_spawn",
					"weapon_rifle_spawn",
					"weapon_smg_spawn"
			)
			.build();
}