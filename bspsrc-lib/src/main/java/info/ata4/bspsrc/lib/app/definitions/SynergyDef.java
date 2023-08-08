package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

import java.util.regex.Pattern;

public class SynergyDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Synergy")
			.setAppId(SourceAppId.SYNERGY)
			.setVersion(20)
			.setFilePattern(Pattern.compile("^syn_$"))
			.setEntities(
					"info_global_settings",
					"info_player_coop",
					"info_player_equip",
					"info_spawn_manager",
					"info_vehicle_spawn",
					"info_vehicle_spawn_destination",
					"info_vehicle_spawn_test",
					"prop_vehicle_mp",
					"synergy_noblock",
					"trigger_coop"
			)
			.build();
}