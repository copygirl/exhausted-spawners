package net.mcft.copy.exhaustedspawners.loot;

import net.mcft.copy.exhaustedspawners.ExhaustedSpawners;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class LootConditions {

	public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES
		= DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ExhaustedSpawners.MOD_ID);

	public static final RegistryObject<LootItemConditionType> CONFIG_ENABLED
		= LOOT_CONDITION_TYPES.register("config_enabled",
			() -> new LootItemConditionType(new ConfigEnabled.Serializer()));

	public static void register(IEventBus bus) {
		LOOT_CONDITION_TYPES.register(bus);
	}
}
