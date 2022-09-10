package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

public class BloodyGoodTimeDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Bloody Good Time")
			.setAppId(2450)
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