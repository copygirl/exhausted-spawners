package net.mcft.copy.exhaustedspawners;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.fml.config.ModConfig.Type;

import net.mcft.copy.exhaustedspawners.event.SpawnerHarvestHandler;
import net.mcft.copy.exhaustedspawners.event.SpawnerRefillingHandler;
import net.mcft.copy.exhaustedspawners.loot.LootConditions;

public class ExhaustedSpawners implements ModInitializer {

	@Override
	public void onInitialize() {
		ForgeConfigRegistry.INSTANCE.register(
				Constants.MOD_ID, Type.COMMON, Config.COMMON_CONFIG);

		LootConditions.register();

		SpawnerHarvestHandler.register();
		SpawnerRefillingHandler.register();
	}
}
