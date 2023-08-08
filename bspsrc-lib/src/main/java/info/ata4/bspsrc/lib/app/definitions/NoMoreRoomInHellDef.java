package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

import java.util.regex.Pattern;

public class NoMoreRoomInHellDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("No More Room in Hell")
			.setAppId(SourceAppId.NO_MORE_ROOM_IN_HELL)
			.setVersion(20)
			.setFilePattern(Pattern.compile("^[nmo|nms]_"))
			.setEntities(
					"func_ff_blocker",
					"func_nmrih_extractionzone",
					"func_no_zombie_spawn",
					"func_safe_zone",
					"func_safe_zone_extension",
					"func_zombie_spawn",
					"info_player_nmrih",
					"music_manager",
					"nmrih_barricade",
					"nmrih_extract_point",
					"nmrih_extract_preview",
					"nmrih_game_state",
					"nmrih_health_station",
					"nmrih_health_station_location",
					"nmrih_interactive_screen",
					"nmrih_npc_squad",
					"nmrih_objective_boundary",
					"nmrih_safezone_supply",
					"npc_zombie_template_maker",
					"overlord_wave_controller",
					"overlord_zombie_helper",
					"prop_barricade_door",
					"prop_door_breakable",
					"walkie_manager",
					"wave_resupply_point"
			)
			.build();
}