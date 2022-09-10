package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;

import java.util.regex.Pattern;

public class HalfLifeSourceDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Half-Life: Source")
			.setAppId(280)
			.setVersionMin(19)
			.setVersionMax(19)
			.setFilePattern(Pattern.compile("^c[0-5]a[0-5][a-z]$"))
			/*
			The entity list for Half-Life: Source is mostly identical to that of
			Half-Life. Some are available in all Source games, but are virtually
            unused outside Half-Life: Source due to their legacy status.

            Half-Life Deathmatch: Source maps are barely distinguishable from
            HL2:DM and HL1:S maps due to the lack of prefixes and special
            entities, so there's no separate entry for this game.
			 */
			.setPointsEntities(5)
			.setEntities(
					"ammo_357",
					"ammo_9mmAR",
					"ammo_9mmclip",
					"ammo_ARgrenades",
					"ammo_buckshot",
					"ammo_crossbow",
					"ammo_gaussclip",
					"ammo_glockclip",
					"ammo_mp5clip",
					"ammo_mp5grenades",
					"ammo_rpgclip",
					"env_beverage",
					"env_blood",
					"env_glow",
					"env_render",
					"env_sound",
					"func_friction",
					"func_guntarget",
					"func_healthcharger",
					"func_monsterclip",
					"func_mortar_field",
					"func_pendulum",
					"func_pushable",
					"func_recharge",
					"func_tanklaser",
					"func_water",
					"info_bigmomma",
					"item_longjump",
					"momentary_door",
					"monster_alien_controller",
					"monster_alien_grunt",
					"monster_alien_slave",
					"monster_apache",
					"monster_barnacle",
					"monster_barney",
					"monster_barney_dead",
					"monster_bigmomma",
					"monster_bullchicken",
					"monster_cockroach",
					"monster_flyer_flock",
					"monster_furniture",
					"monster_gargantua",
					"monster_gman",
					"monster_headcrab",
					"monster_hevsuit_dead",
					"monster_hgrunt_dead",
					"monster_houndeye",
					"monster_human_assassin",
					"monster_human_grunt",
					"monster_ichthyosaur",
					"monster_leech",
					"monster_miniturret",
					"monster_nihilanth",
					"monster_osprey",
					"monster_rat",
					"monster_scientist",
					"monster_scientist_dead",
					"monster_sentry",
					"monster_sitting_scientist",
					"monster_snark",
					"monster_tentacle",
					"monster_tripmine",
					"monster_turret",
					"monster_zombie",
					"monstermaker",
					"multi_manager",
					"scripted_sentence",
					"speaker",
					"target_cdaudio",
					"trigger_auto",
					"trigger_camera",
					"trigger_cdaudio",
					"trigger_endsection",
					"trigger_relay",
					"trigger_togglesave",
					"weapon_357",
					"weapon_crossbow",
					"weapon_crowbar",
					"weapon_egon",
					"weapon_gauss",
					"weapon_glock",
					"weapon_handgrenade",
					"weapon_hornetgun",
					"weapon_mp5",
					"weapon_rpg",
					"weapon_satchel",
					"weapon_shotgun",
					"weapon_snark",
					"weapon_tripmine",
					"weaponbox",
					"world_items",
					"xen_hair",
					"xen_plantlight",
					"xen_spore_large",
					"xen_spore_medium",
					"xen_spore_small",
					"xen_tree"
			)
			.build();
}