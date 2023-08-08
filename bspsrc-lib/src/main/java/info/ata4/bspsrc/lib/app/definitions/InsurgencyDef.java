package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

public class InsurgencyDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Insurgency")
			.setAppId(SourceAppId.INSURGENCY)
			.setVersion(21)
			// There are more insurgency related entities, but I decided to only use distinct
			// ones because those should be enough to detect insurgency maps
			.setEntities(
					"ins_blockzone",
					"ins_rulesproxy",
					"ins_spawnpoint",
					"ins_spawnzone",
					"ins_viewpoint",
					"logic_battle",
					"logic_checkpoint",
					"logic_elimination",
					"logic_firefight",
					"logic_hunt",
					"logic_outpost",
					"logic_push",
					"logic_skirmish",
					"logic_training",
					"logic_vip",
					"obj_destructible",
					"obj_destructible_vehicle",
					"obj_weapon_cache",
					"point_controlpoint",
					"point_flag",
					"trigger_capture_zone"
			)
			.build();
}