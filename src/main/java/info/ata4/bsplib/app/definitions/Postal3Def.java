package info.ata4.bsplib.app.definitions;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppBuilder;
import info.ata4.bsplib.app.SourceAppId;

public class Postal3Def {

	public static final SourceApp APP = new SourceAppBuilder()
			.setName("Postal III")
			.setAppId(SourceAppId.POSTAL_3)
			.setVersionMin(20)
			.setEntities(
					"p3_ai_goal_actbusy",
					"p3_fsm_dummy",
					"p3_fsm_logic_entity",
					"p3_hugo_chavez",
					"p3_item_ammo_crate",
					"p3_keyframe_garland",
					"p3_move_garland",
					"p3_npc_cat",
					"p3_npc_citizen",
					"p3_npc_cop",
					"p3_npc_dog",
					"p3_npc_helicopter",
					"p3_npc_monkey",
					"p3_npc_motorhead",
					"p3_npc_rhino",
					"p3_npc_simdriver",
					"p3_prop_car",
					"p3_prop_compound",
					"p3_prop_damagedragdoll",
					"p3_prop_destructible",
					"p3_prop_extinguisher",
					"p3_trigger_covermode",
					"p3_vehicle_segway",
					"p3_weapon_beenest",
					"p3_weapon_catnip",
					"p3_weapon_cop_baton",
					"p3_weapon_deserteagle",
					"p3_weapon_fireaxe",
					"p3_weapon_gasoline",
					"p3_weapon_hammer",
					"p3_weapon_m136",
					"p3_weapon_m16",
					"p3_weapon_m60",
					"p3_weapon_machete",
					"p3_weapon_molotov",
					"p3_weapon_nailbat",
					"p3_weapon_shotgun",
					"p3_weapon_spray",
					"p3_weapon_taser",
					"prop_p3_fsmitem",
					"prop_p3_give_ammo",
					"prop_p3_stuffitem"
			)
			.build();
}