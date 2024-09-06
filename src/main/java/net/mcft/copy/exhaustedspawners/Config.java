package net.mcft.copy.exhaustedspawners;

import java.util.List;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

	public static ForgeConfigSpec COMMON_CONFIG;

	public static final String CATEGORY_SPAWNER = "spawner";
	public static ForgeConfigSpec.IntValue SPAWN_LIMIT;
	public static ForgeConfigSpec.BooleanValue SPAWNER_SILK_TOUCH;
	public static ForgeConfigSpec.DoubleValue SPAWNER_BREAK_SPEED;

	public static final String CATEGORY_REFILLING = "refilling";
	public static ForgeConfigSpec.IntValue AMOUNT_REFILLED;

	public static final String CATEGORY_SPAWN_EGGS = "spawn_eggs";
	public static ForgeConfigSpec.DoubleValue EGG_DROP_CHANCE;
	public static ForgeConfigSpec.DoubleValue EGG_DROP_LOOTING_BONUS;
	public static ForgeConfigSpec.DoubleValue EGG_DROP_CHANCE_SILK_TOUCH;
	public static ForgeConfigSpec.BooleanValue PLAYER_KILL_REQUIRED;
	public static ForgeConfigSpec.BooleanValue CLEAR_DROPS_ON_EGG;
	public static ForgeConfigSpec.BooleanValue CLEAR_DROPS_ON_SILK_TOUCH;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> EGG_DROP_BLACKLIST;

	static {
		var common = new ForgeConfigSpec.Builder();

		common.comment("Monster Spawner Settings").push(CATEGORY_SPAWNER);
		SPAWN_LIMIT = common.comment(
				"Amount of mobs spawned before spawner becomes inactive.",
				"Set to 0 to disable, making spawners work like normal.",
				"(Default: 64)")
			.defineInRange("spawn_limit", 64, 0, Integer.MAX_VALUE);
		SPAWNER_SILK_TOUCH = common.comment(
				"Whether a spawner can be retrieved using Silk Touch enchantment.",
				"This also prevents spawners from dropping experience when silk-touched.",
				"Spawner drops are controlled by a loot table, which can be overridden.",
				"(Default: true)")
			.define("silk_touch", true);
		SPAWNER_BREAK_SPEED = common.comment(
				"Multiplies the time it takes to break a spawner by this value.",
				"For example, a value of 0.1 would make it take 10 times as long to break.",
				"(Default: 1.0)")
			.defineInRange("break_speed", 1.0, 0.0, Double.POSITIVE_INFINITY);
		common.pop();

		common.comment("Refilling Settings").push(CATEGORY_REFILLING);
		AMOUNT_REFILLED = common.comment(
				"Amount of mobs a spawner is recharged with when a spawn egg is used on it.",
				"Requires the spawner to be empty or to already be spawning the same mob type.",
				"Keep drop chances lower than 1 / this value, or you can end up with a positive feedback loop.",
				"(Default: 16)")
			.defineInRange("amount_refilled", 16, 0, Integer.MAX_VALUE);
		common.pop();

		common.comment("Spawn Egg Settings").push(CATEGORY_SPAWN_EGGS);
		EGG_DROP_CHANCE = common.comment(
				"Chance for a mob to drop its spawn egg when killed.",
				"Set to 0.0 to disable.",
				"(Default: 0.002)")
			.defineInRange("drop_chance", 0.002, 0.0, 1.0);
		EGG_DROP_LOOTING_BONUS = common.comment(
				"Increases drop chance by this value for each effective Looting level.",
				"(Default: 0.001)")
			.defineInRange("looting_bonus", 0.001, 0.0, 1.0);
		EGG_DROP_CHANCE_SILK_TOUCH = common.comment(
				"Chance for a mob to drop its spawn egg when killed with Silk Touch.",
				"Replaces 'drop_chance' entirely and is incompatible with Looting.",
				"To have weapons be enchantable with Silk Touch, use Forgery's 'weapons_accept_silk' tweak.",
				"(Default: 0.05)")
			.defineInRange("drop_chance_silk_touch", 0.1, 0.0, 1.0);
		PLAYER_KILL_REQUIRED = common.comment(
				"Whether a player kill is required for mobs to drop their spawn egg.",
				"Note: Some automation mods can fill this requirement using a fake player.",
				"(Default: true)")
			.define("player_kill_required", true);
		CLEAR_DROPS_ON_EGG = common.comment(
				"Whether to clear any other mob drops (except equipment) when a spawn egg is dropped.",
				"(Default: false)")
			.define("clear_drops_on_egg", false);
		CLEAR_DROPS_ON_SILK_TOUCH = common.comment(
				"Whether to always clear mob drops (except equipment) when a Silk Touch item is used.",
				"(Default: true)")
			.define("clear_drops_on_silk_touch", true);
		EGG_DROP_BLACKLIST = common.comment(
				"Blacklist of mobs (ids and tags) that should not drop their spawn eggs when killed.",
				"Example: [\"minecraft:creeper\", \"minecraft:ghast\", \"#minecraft:illager\"]",
				"(Default: [])")
			.defineList("drop_blacklist", ImmutableList.of(), Config::isValidIdentifierOrTag);
		common.pop();

		COMMON_CONFIG = common.build();
	}

	private static boolean isValidIdentifierOrTag(Object obj) {
		if (!(obj instanceof String)) return false;
		var str = (String)obj;
		if (str.startsWith("#")) str = str.substring(1);
		return ResourceLocation.isValidResourceLocation(str);
	}
}
