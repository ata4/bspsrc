package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

public class VindictusMabinogiHeroesDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Vindictus / Mabinogi: Heroes")
			.setAppId(SourceAppId.VINDICTUS)
			.setVersionMin(20)
			.setEntities(
					"func_brush_projectile_remove",
					"info_custom_eye_target",
					"info_player_dark_knight",
					"info_player_paladin",
					"npc_actor",
					"npc_bear",
					"npc_giant_spider",
					"npc_glasgavelen",
					"npc_gnoll",
					"npc_goblin",
					"npc_golem",
					"npc_kobold",
					"npc_lizardman",
					"npc_ogre",
					"npc_player_select",
					"npc_toad",
					"npc_troll",
					"npc_vamplord",
					"npc_yeti",
					"pix_fog_srgb_controller",
					"point_countdown_gauge",
					"prop_beanbag",
					"prop_ctf",
					"prop_gourd",
					"prop_vehicle_ballista",
					"prop_vehicle_ballista_rope",
					"prop_world",
					"score_ctf",
					"script_entity",
					"script_listener",
					"script_listener_survivor",
					"trigger_givesubweapon",
					"trigger_req_player_touch"
			)
			.build();
}