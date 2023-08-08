package info.ata4.bspsrc.lib.app.definitions;

import info.ata4.bspsrc.lib.app.SourceApp;
import info.ata4.bspsrc.lib.app.SourceAppBuilder;
import info.ata4.bspsrc.lib.app.SourceAppId;

import java.util.regex.Pattern;

public class DarkMessiahOfMightMagicDef {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Dark Messiah of Might & Magic")
			.setAppId(SourceAppId.DARK_MESSIAH)
			.setVersion(20)
			.setFilePattern(Pattern.compile("^l\\d{2}_"))
			.setEntities(
					"env_entity_SpellCaster",
					"func_mm_avoidsimplify",
					"info_node_mm_link_controller",
					"item_food_fish_hanged",
					"item_food_magic_toad",
					"item_loot_scroll_charm",
					"item_loot_scroll_fire_trap",
					"item_loot_scroll_fireball",
					"item_loot_scroll_freeze",
					"item_loot_scroll_lightning",
					"item_loot_scroll_shrink",
					"item_potion_cure_poison",
					"item_potion_full_life",
					"item_potion_life",
					"item_potion_mana",
					"item_potion_stone",
					"mm_armor",
					"mm_book",
					"mm_butterflies",
					"mm_func_fishes",
					"mm_player_inputs",
					"npc_facehugger",
					"npc_ghoul",
					"npc_goblin",
					"npc_human_guard",
					"npc_orc_sword",
					"npc_spider_regular",
					"npc_undead",
					"npc_wizard",
					"prop_ammo_arrow",
					"prop_door_free_rotating",
					"spider_web_base",
					"spider_web_keyframe",
					"vehicle_mm_barge",
					"weapon_arx_silver_sword",
					"weapon_arxringmana",
					"weapon_arxringprotectfire",
					"weapon_mm_bow_explosive",
					"weapon_mm_shield_lava",
					"weapon_mm_staff_combat"
			)
			.build();
}