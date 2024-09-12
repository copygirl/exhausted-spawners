package net.mcft.copy.exhaustedspawners;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;

import net.mcft.copy.exhaustedspawners.api.OverfillBehavior;

// Need to suppress these so we can have List<String> config types
// instead of List<? extends String>, when String is final anyway!
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Config {

	public static ForgeConfigSpec COMMON_CONFIG;

	public static final String CATEGORY_SPAWNER = "spawner";
	public static IntValue SPAWN_LIMIT;
	public static DoubleValue SPAWNER_BREAK_SPEED;

	public static final String CATEGORY_REFILLING = "spawner_refilling";
	public static IntValue AMOUNT_REFILLED;
	public static EnumValue<OverfillBehavior> OVERFILL_BEHAVIOR;

	public static final String CATEGORY_SPAWNER_LOOT = "spawner_loot";
	public static BooleanValue SPAWNER_SILK_TOUCH;
	public static DoubleValue DROP_CHANCE_PER_EGG;
	public static DoubleValue DROP_CHANCE_PER_EGG_SILK_TOUCH;
	public static IntValue XP_BASE;
	public static IntValue XP_REMAINING_CAP;
	public static DoubleValue XP_PER_REMAINING;

	public static final String CATEGORY_SPAWN_EGG_LOOT = "spawn_egg_loot";
	public static DoubleValue DROP_CHANCE;
	public static DoubleValue DROP_LOOTING_BONUS;
	public static DoubleValue DROP_CHANCE_SILK_TOUCH;
	public static BooleanValue PLAYER_KILL_REQUIRED;
	public static BooleanValue CLEAR_DROPS_ON_EGG;
	public static BooleanValue CLEAR_DROPS_ON_SILK_TOUCH;
	public static ConfigValue<List<String>> DROP_LIST;
	public static EnumValue<DropListBehavior> DROP_LIST_BEHAVIOR;
	public enum DropListBehavior { ALLOW, DENY }

	public static Map<ResourceLocation     , Double> CACHED_DROP_LIST_IDS  = ImmutableMap.of();
	public static Map<TagKey<EntityType<?>>, Double> CACHED_DROP_LIST_TAGS = ImmutableMap.of();

	static {
		var common = new Builder();

		common.comment("Monster Spawner Settings").push(CATEGORY_SPAWNER);
		SPAWN_LIMIT = common.comment(
				"Amount of mobs spawned before spawner empties and becomes inactive.",
				"Set to 0 to disable, making spawners work like normal.",
				"Default: 64")
			.defineInRange("spawn_limit", 64, 0, Integer.MAX_VALUE);
		SPAWNER_BREAK_SPEED = common.comment(
				"Multiplies the time it takes to break a spawner by this value.",
				"For example, a value of 0.1 would make it take 10 times as long to break.",
				"Default: 1.0")
			.defineInRange("break_speed", 1.0, 0.0, Double.POSITIVE_INFINITY);
		common.pop();

		common.comment("Spawner Refilling Settings").push(CATEGORY_REFILLING);
		AMOUNT_REFILLED = common.comment(
				"Amount of mobs a spawner is recharged with when a spawn egg is used on it.",
				"Requires the spawner to be empty or to already be spawning the same mob type.",
				"Keep drop chances lower than '1 / amount_refilled', or you can end up with a positive feedback loop.",
				"Default: 4")
			.defineInRange("amount_refilled", 4, 0, Integer.MAX_VALUE);
		OVERFILL_BEHAVIOR = common.comment(
				"What happens when refilling a spawner beyond 'spawn_limit':",
				"  DENY   = You are prevented from doing so.",
				"  FIZZLE = Any additional spawns are lost.",
				"  ALLOW  = To infinity, and beyond!",
				"Default: FIZZLE")
			.defineEnum("overfill_behavior", OverfillBehavior.FIZZLE);
		common.pop();

		common.comment("Spawner Loot Settings").push(CATEGORY_SPAWNER_LOOT);
		SPAWNER_SILK_TOUCH = common.comment(
				"Whether a spawner can be retrieved using Silk Touch enchantment.",
				"This also prevents spawners from dropping experience when silk-touched.",
				"Spawner drops are controlled by a loot table, which can be overridden.",
				"Default: true")
			.define("silk_touch", true);
		DROP_CHANCE_PER_EGG = common.comment(
				"When breaking a spawner, each spawn egg worth of mobs remaining in it has this chance to drop.",
				"For each egg dropped, the number of remaining mobs is reduced for the purpose of calculating XP drops.",
				"Default: 0.0")
			.defineInRange("drop_chance_per_egg", 0.0, 0.0, 1.0);
		DROP_CHANCE_PER_EGG_SILK_TOUCH = common.comment(
				"When breaking a spawner with silk touch, each spawn egg",
				"worth of mobs remaining in it has this chance to drop.",
				"Default: 0.8")
			.defineInRange("drop_chance_per_egg_silk_touch", 0.8, 0.0, 1.0);
		XP_BASE = common.comment(
				"Amount of experience dropped when breaking a spawner, if not silk-touched.",
				"The following formula is used to determine the actual amount of xp to drop:",
				"  amount = xp_base + min(xp_remaining_cap, remaining) * xp_per_remaining",
				"  result = amount + rand(amount) + rand(amount)",
				"Default: 10")
			.defineInRange("xp_base", 10, 0, Integer.MAX_VALUE);
		XP_REMAINING_CAP = common.comment(
				"Number of remaining mobs is capped to this value for the purposes of calculating XP drops.",
				"Default: 20")
			.defineInRange("xp_remaining_cap", 20, 0, Integer.MAX_VALUE);
		XP_PER_REMAINING = common.comment(
				"Experience dropped for each remaining mob in the spawner when broken.",
				"Default: 0.5")
			.defineInRange("xp_per_remaining", 0.5, 0.0, Double.POSITIVE_INFINITY);
		common.pop();

		common.comment("Spawn Egg Loot Settings").push(CATEGORY_SPAWN_EGG_LOOT);
		DROP_CHANCE = common.comment(
				"Chance for a mob to drop its spawn egg when killed.",
				"Default: 0.0")
			.defineInRange("drop_chance", 0.0, 0.0, 1.0);
		DROP_LOOTING_BONUS = common.comment(
				"Increases drop chance by this value for each effective Looting level.",
				"Default: 0.0")
			.defineInRange("looting_bonus", 0.0, 0.0, 1.0);
		DROP_CHANCE_SILK_TOUCH = common.comment(
				"Chance for a mob to drop its spawn egg when killed with Silk Touch.",
				"Replaces 'drop_chance' entirely and is incompatible with Looting.",
				"To have weapons be enchantable with Silk Touch, use Forgery's 'weapons_accept_silk' tweak.",
				"Default: 0.15")
			.defineInRange("drop_chance_silk_touch", 0.15, 0.0, 1.0);
		PLAYER_KILL_REQUIRED = common.comment(
				"Whether a player kill is required for mobs to drop their spawn egg.",
				"Note: Some automation mods can fill this requirement using a fake player.",
				"Default: true")
			.define("player_kill_required", true);
		CLEAR_DROPS_ON_EGG = common.comment(
				"Whether to clear any other mob drops (except equipment) when a spawn egg is dropped.",
				"Default: false")
			.define("clear_drops_on_egg", false);
		CLEAR_DROPS_ON_SILK_TOUCH = common.comment(
				"Whether to always clear mob drops (except equipment) when a Silk Touch item is used.",
				"Default: true")
			.define("clear_drops_on_silk_touch", true);
		DROP_LIST = (ConfigValue)common.comment(
				"List of mobs (ids and tags) that are either do or don't drop their spawn eggs.",
				"By default, all mobs drop spawn eggs depending on the 'drop_list_behavior' setting,",
				"with entries in this list acting as exceptions. Entries starting with '#' are tags.",
				"When an entry contains an '=' character, the following number acts as a multiplier to",
				"the default drop chance, for example '0.5' halfing the chance and '2.0' doubling it.",
				"Default: [\"minecraft:slime=0.1\", \"minecraft:magma_cube=0.1\", \"#forge:slimes=0.1\", \"#c:slimes=0.1\"]")
			.defineList("drop_list", ImmutableList.of(
				"minecraft:slime=0.1",
				"minecraft:magma_cube=0.1",
				"#forge:slimes=0.1",
				"#c:slimes=0.1"
			), Config::isValidDropListEntry);
		DROP_LIST_BEHAVIOR = common.comment(
				"Specifies the behavior of how to treat entries in 'drop_list'.",
				"In addition, specifies the spawn egg drop behavior for mobs not in the list:",
				"  ALLOW = Do not drop spawn eggs by default.",
				"  DENY  = Do drop spawn eggs by default.",
				"Default: DENY")
			.defineEnum("drop_list_behavior", DropListBehavior.DENY);
		common.pop();

		COMMON_CONFIG = common.build();
	}

	public static void updateCachedValues() {
		var ids  = ImmutableMap.<ResourceLocation     , Double>builder();
		var tags = ImmutableMap.<TagKey<EntityType<?>>, Double>builder();

		for (var str : DROP_LIST.get()) {
			var chance = (DROP_LIST_BEHAVIOR.get() == DropListBehavior.ALLOW) ? 1.0 : 0.0;

			var equalsIndex = str.indexOf('=');
			if (equalsIndex >= 0) {
				var chanceStr = str.substring(equalsIndex + 1);
				str = str.substring(0, equalsIndex);
				try { chance = Double.parseDouble(chanceStr); }
				catch (NumberFormatException ex) {  }
			}

			if (str.startsWith("#")) {
				str = str.substring(1);
				var key = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(str));
				tags.put(key, chance);
			} else {
				var key = new ResourceLocation(str);
				ids.put(key, chance);
			}
		}

		CACHED_DROP_LIST_IDS  = ids.build();
		CACHED_DROP_LIST_TAGS = tags.build();
	}

	private static boolean isValidDropListEntry(Object obj) {
		if (!(obj instanceof String)) return false;
		var str = (String)obj;

		var equalsIndex = str.indexOf('=');
		if (equalsIndex >= 0) {
			var chance = Double.NaN;
			var chanceStr = str.substring(equalsIndex + 1);
			str = str.substring(0, equalsIndex);
			// Random rant: Who decided to make parseDouble trim
			// the input string but parseInt throw an exception?
			try { chance = Double.parseDouble(chanceStr); }
			catch (NumberFormatException ex) { return false; }
			return (chance >= 0) && Double.isFinite(chance);
		}

		// Strip the '#' from the string if it starts with one.
		if (str.startsWith("#")) str = str.substring(1);

		return ResourceLocation.isValidResourceLocation(str);
	}
}
