package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

import java.util.regex.Pattern;

public class HalfLife2EpisodeTwoDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Half-Life 2: Episode Two")
			.setAppId(SourceAppId.HALF_LIFE_2_EP2)
			.setVersionMin(20)
			.setFilePattern(Pattern.compile("^ep2_"))
			.setPointsEntities(10)
			.setEntities(
					"func_tank_combine_cannon",
					"func_tanktrain",
					"grenade_helicopter",
					"npc_advisor",
					"npc_antlion_grub",
					"npc_enemyfinder_combinecannon",
					"npc_fastzombie_torso",
					"npc_grenade_frag",
					"npc_hunter",
					"npc_hunter_maker",
					"npc_magnusson",
					"weapon_striderbuster"
			)
			.build();
}