package net.mcft.copy.limitedspawners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.mcft.copy.limitedspawners.event.SpawnerEventHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(LimitedSpawners.MOD_ID)
public class LimitedSpawners {

	public static final String MOD_ID = "limitedspawners";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public LimitedSpawners() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
		MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
	}
}
