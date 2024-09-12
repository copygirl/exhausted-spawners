package net.mcft.copy.exhaustedspawners;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.mcft.copy.exhaustedspawners.event.SpawnerRefillingHandler;
import net.mcft.copy.exhaustedspawners.loot.LootConditions;
import net.mcft.copy.exhaustedspawners.event.SpawnerHarvestHandler;

@Mod(Constants.MOD_ID)
public class ExhaustedSpawners {

	public ExhaustedSpawners() {
		ModLoadingContext.get().registerConfig(
				ModConfig.Type.COMMON, Config.COMMON_CONFIG);

		// Update cached values in Config class when config is (re)loaded.
		var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener((ModConfigEvent.Loading   event) -> Config.updateCachedValues());
		modEventBus.addListener((ModConfigEvent.Reloading event) -> Config.updateCachedValues());

		LootConditions.register();

		SpawnerHarvestHandler.register();
		SpawnerRefillingHandler.register();
	}
}
