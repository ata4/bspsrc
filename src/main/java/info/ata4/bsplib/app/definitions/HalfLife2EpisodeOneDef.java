package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

import java.util.regex.Pattern;

public class HalfLife2EpisodeOneDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Half-Life 2: Episode One")
			.setAppId(SourceAppId.HALF_LIFE_2_EP1)
			.setVersionMin(20)
			.setFilePattern(Pattern.compile("^ep1_"))
			.setPointsEntities(10)
			.setEntities(
					"filter_combineball_type",
					"info_darknessmode_lightsource",
					"npc_clawscanner",
					"npc_zombine",
					"point_combine_ball_launcher",
					"prop_coreball"
			)
			.build();
}