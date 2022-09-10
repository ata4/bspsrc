package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

import java.util.regex.Pattern;

public class TacticalInterventionDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Tactical Intervention")
			.setAppId(SourceAppId.TACTICAL_INTERVENTION)
			.setVersionMin(20)
			.setVersionMax(21)
			.setFilePattern(Pattern.compile("^mis_$"))
			.setEntities(
					"func_capture_zone",
					"func_capzone_terrorist_1",
					"func_capzone_terrorist_2",
					"func_capzone_terrorist_3",
					"func_capzone_vis",
					"func_coverzone",
					"func_hostage_rescue_zone",
					"func_stageblocker",
					"info_ammo_box_spawn",
					"info_car_spawn",
					"info_ct_car_spawn",
					"info_heli_CT_spawn",
					"info_heli_TER_spawn",
					"info_heli_supply_drop",
					"info_heli_wp",
					"info_hostage_spawn_1",
					"info_hostage_spawn_2",
					"info_hostage_spawn_3",
					"info_hostage_waypoint_1",
					"info_hostage_waypoint_2",
					"info_hostage_waypoint_3",
					"info_player_ctstart_A1",
					"info_player_ctstart_A2",
					"info_player_ctstart_A3",
					"info_player_ctstart_B1",
					"info_player_ctstart_B2",
					"info_player_ctstart_B3",
					"info_player_ctstart_C1",
					"info_player_ctstart_C2",
					"info_player_ctstart_C3",
					"info_player_shooting_range_spawn",
					"info_player_terroriststart_A1",
					"info_player_terroriststart_A2",
					"info_player_terroriststart_A3",
					"info_terrorist_car_spawn",
					"info_vehicle_wp",
					"info_vip_car_goal",
					"info_vip_car_spawn",
					"info_vip_case_goal",
					"info_vip_interm_wp",
					"info_vip_wp",
					"point_intro_camera",
					"point_planning_stage_camera",
					"prop_fire_extinguisher",
					"prop_rappelpoint",
					"ti_trigger_once",
					"wheeled_controllable"
			)
			.build();
}