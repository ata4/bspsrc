package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

public class TheShipDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("The Ship")
			.setAppId(SourceAppId.THE_SHIP)
			.setVersionMin(20)
			.setEntities(
					"ai_shipmate",
					"ship_base_interaction",
					"ship_container",
					"ship_doctor",
					"ship_item_spawn_dest_physical",
					"ship_item_spawner",
					"ship_lift",
					"ship_psychiatrist",
					"ship_security_booth",
					"ship_security_camera",
					"ship_security_guard",
					"ship_sickbay_receptionist",
					"ship_trap",
					"ship_trigger_room",
					"ship_trigger_weapon_dissolve",
					"vfx_freezer",
					"vfx_sauna_steam",
					"vfx_ship_paddle",
					"vfx_ship_waterfall"
			)
			.build();
}