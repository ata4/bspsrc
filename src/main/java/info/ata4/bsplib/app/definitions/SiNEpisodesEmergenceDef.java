package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

import java.util.regex.Pattern;

public class SiNEpisodesEmergenceDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("SiN Episodes: Emergence")
			.setAppId(SourceAppId.SIN_EPISODES_EMERGENCE)
			.setVersionMin(19)
			.setFilePattern(Pattern.compile("^arena_$"))
			.setEntities(
					"env_poison_gas",
					"item_healthvial",
					"npc_druglab_grunt_pistol",
					"npc_druglab_jetpack",
					"npc_druglab_worker",
					"npc_enemyfinder",
					"npc_grenade_frag",
					"prop_u4_barrel"
			)
			.build();
}