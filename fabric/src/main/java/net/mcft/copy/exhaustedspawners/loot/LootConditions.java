package net.mcft.copy.exhaustedspawners.loot;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import net.mcft.copy.exhaustedspawners.Constants;

public class LootConditions {
	private LootConditions() {  }

	public static final LootItemConditionType CONFIG_ENABLED
		= Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE,
			new ResourceLocation(Constants.MOD_ID, ConfigEnabledCondition.ID),
			ConfigEnabledCondition.TYPE);

	// This function just exists to make sure static fields are initialized.
	public static void register() {  }
}
