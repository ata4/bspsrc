package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

public class ZenoClashDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Zeno Clash")
			.setAppId(22200)
			.setVersionMin(20)
			.setEntities(
					"NPC_Zeno_FatherMother_Finale",
					"NPC_Zeno_FatherMother_Naked",
					"env_selfShadowing",
					"func_endworld_pyre",
					"item_healthFruit",
					"item_zeno_skullbomb",
					"npc_crab",
					"npc_deadra",
					"npc_flyingAnimal",
					"npc_hen",
					"npc_mucalosaurus",
					"npc_pig",
					"npc_raptobird",
					"npc_squirrel_explosive",
					"npc_timid_rabbit",
					"npc_zeno_andro",
					"npc_zeno_assassin",
					"npc_zeno_assassin2",
					"npc_zeno_assassin_phase2",
					"npc_zeno_bird",
					"npc_zeno_bugbird",
					"npc_zeno_chneero",
					"npc_zeno_elephant1",
					"npc_zeno_enteloman",
					"npc_zeno_entelowoman",
					"npc_zeno_entelowomanBlack",
					"npc_zeno_fathermother",
					"npc_zeno_gabel",
					"npc_zeno_gastornis",
					"npc_zeno_helim",
					"npc_zeno_henae",
					"npc_zeno_macra",
					"npc_zeno_macra2",
					"npc_zeno_mantiswoman",
					"npc_zeno_mechanic",
					"npc_zeno_metamoq",
					"npc_zeno_metamoq_tutorial",
					"npc_zeno_neutral",
					"npc_zeno_pigman",
					"npc_zeno_plainsPerson",
					"npc_zeno_pothead",
					"npc_zeno_punk",
					"npc_zeno_rimat",
					"npc_zeno_shadow",
					"npc_zeno_talonco",
					"npc_zeno_therium",
					"npc_zeno_tsekung",
					"npc_zeno_xetse",
					"weapon_zeno_boneclub",
					"weapon_zeno_chainballgun",
					"weapon_zeno_dualgun",
					"weapon_zeno_fireballgun",
					"weapon_zeno_hammer",
					"weapon_zeno_musket"
			)
			.build();
}