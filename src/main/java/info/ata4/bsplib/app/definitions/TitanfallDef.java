package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

import java.util.regex.Pattern;

public class TitanfallDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Titanfall")
			.setAppId(-400)
			.setVersionMin(29)
			.setFilePattern(Pattern.compile("^mp_$"))
			.setEntities(
					"env_dropzone",
					"func_window_hint",
					"info_frontline",
					"info_hardpoint",
					"info_lightprobe",
					"info_node_cover_crouch",
					"info_node_cover_left",
					"info_node_cover_right",
					"info_node_cover_stand",
					"info_node_safe_hint",
					"info_replacement_titan_spawn",
					"info_spawnpoint_droppod",
					"info_spawnpoint_droppod_start",
					"info_spawnpoint_dropship",
					"info_spawnpoint_dropship_start",
					"info_spawnpoint_flag",
					"info_spawnpoint_human",
					"info_spawnpoint_human_start",
					"info_spawnpoint_titan",
					"info_spawnpoint_titan_start",
					"info_target_clientside",
					"npc_turret_mega",
					"prop_control_panel",
					"prop_exfil_panel",
					"prop_refuel_pump",
					"script_marvin_job",
					"script_ref",
					"traverse",
					"trigger_capture_point",
					"trigger_indoor_area",
					"trigger_out_of_bounds"
			)
			.build();
}