package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

import java.util.regex.Pattern;

public class HalfLife2EpisodeOneDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Half-Life 2: Episode One")
			.setAppId(380)
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