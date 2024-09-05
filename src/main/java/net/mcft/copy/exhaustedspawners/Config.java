package net.mcft.copy.exhaustedspawners;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

	public static ForgeConfigSpec COMMON_CONFIG;

	public static final String CATEGORY_SPAWNER = "spawner";
	public static ForgeConfigSpec.IntValue SPAWNER_SPAWN_LIMIT;
	public static ForgeConfigSpec.DoubleValue SPAWNER_HARDNESS;
	public static ForgeConfigSpec.BooleanValue SPAWNER_SILK_TOUCH;

	public static final String CATEGORY_SPAWN_EGGS = "spawn_eggs";
	public static ForgeConfigSpec.DoubleValue SPAWN_EGG_DROP_CHANCE;
	public static ForgeConfigSpec.BooleanValue SPAWN_EGG_PLAYER_KILL_REQUIRED;
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> SPAWN_EGG_DROP_BLACKLIST;

	static {
		var common = new ForgeConfigSpec.Builder();

		common.comment("Monster Spawner Settings").push(CATEGORY_SPAWNER);
		SPAWNER_SPAWN_LIMIT = common
			.comment("Amount of mobs spawned before spawner becomes inactive. Set to 0 to disable limit. (Default: 64)")
			.defineInRange("spawn_limit", 64, 0, Integer.MAX_VALUE);
		SPAWNER_HARDNESS = common
			.comment("Controls how long a spawner takes to break. (Default: 5.0)")
			.defineInRange("hardness", 5.0, 0.0, Double.POSITIVE_INFINITY);
		SPAWNER_SILK_TOUCH = common
			.comment("Whether a spawner can be retrieved using Silk Touch enchantment. (Default: true)")
			.define("silk_touch", true);
		common.pop();

		common.comment("Spawn Egg Settings").push(CATEGORY_SPAWN_EGGS);
		SPAWN_EGG_DROP_CHANCE = common
			.comment("Chance for a mob to drop its spawn egg when killed. Set to 0.0 to disable. (Default: 0.04)")
			.defineInRange("drop_chance", 0.04, 0.0, 1.0);
		SPAWN_EGG_PLAYER_KILL_REQUIRED = common
			.comment("Whether a player kill is required for mobs to drop their spawn egg. (Default: true)")
			.define("player_kill_required", true);
		SPAWN_EGG_DROP_BLACKLIST = common
			.comment("Blacklist of mobs that should not drop their spawn eggs when killed. (Default: [])")
			.defineList("drop_blacklist", ImmutableList.of(), obj -> ResourceLocation.isValidResourceLocation((String)obj));
		common.pop();

		COMMON_CONFIG = common.build();
	}
}
