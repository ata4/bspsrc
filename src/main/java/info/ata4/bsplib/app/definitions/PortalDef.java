package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

import java.util.regex.Pattern;

public class PortalDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Portal")
			.setAppId(SourceAppId.PORTAL)
			.setVersionMin(20)
			.setFilePattern(Pattern.compile("^(testchmb_a|escape)_"))
			.setEntities(
					"env_portal_credits",
					"func_noportal_volume",
					"func_portal_bumper",
					"func_portal_detector",
					"func_portal_orientation",
					"func_portal_volume",
					"npc_portal_turret_floor",
					"npc_rocket_turret",
					"npc_security_camera",
					"point_bonusmaps_accessor",
					"point_energy_ball_launcher",
					"prop_glados_core",
					"prop_portal",
					"prop_portal_stats_display",
					"trigger_portal_cleanser",
					"vgui_neurotoxin_countdown",
					"weapon_portalgun"
			)
			.build();
}