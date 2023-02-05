package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

public class BloodyGoodTimeDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Bloody Good Time")
			.setAppId(SourceAppId.BLOODY_GOOD_TIME)
			.setVersionMin(20)
			.setEntities(
					"info_player_spawn",
					"npc_securityguard",
					"player_overlay_render_location",
					"pm_bonus_star_spawner",
					"pm_interact_icon_sprite",
					"pm_interaction",
					"pm_lift",
					"pm_no_collision_trigger",
					"pm_static_advert",
					"pm_trap",
					"pm_water_trigger",
					"pm_weapon_spawner"
			)
			.build();
}