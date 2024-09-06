package net.mcft.copy.exhaustedspawners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.mcft.copy.exhaustedspawners.event.SpawnEggDropHandler;
import net.mcft.copy.exhaustedspawners.event.SpawnerHarvestHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ExhaustedSpawners.MOD_ID)
public class ExhaustedSpawners {

	public static final String MOD_ID = "exhaustedspawners";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public ExhaustedSpawners() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
		MinecraftForge.EVENT_BUS.register(new SpawnEggDropHandler());
		MinecraftForge.EVENT_BUS.register(new SpawnerHarvestHandler());
	}
}
