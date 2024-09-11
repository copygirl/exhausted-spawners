package net.mcft.copy.exhaustedspawners.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.mcft.copy.exhaustedspawners.Constants;

public class LootConditions {
	private LootConditions() {  }

	public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES
		= DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Constants.MOD_ID);

	public static final RegistryObject<LootItemConditionType> CONFIG_ENABLED
		= LOOT_CONDITION_TYPES.register(
			ConfigEnabledCondition.ID,
			() -> ConfigEnabledCondition.TYPE);

	public static void register() {
		var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		LOOT_CONDITION_TYPES.register(modEventBus);
	}
}
