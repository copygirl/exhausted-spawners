package net.mcft.copy.limitedspawners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.mcft.copy.limitedspawners.config.ModConfigManager;
import net.mcft.copy.limitedspawners.event.SpawnerEventHandler;
import net.mcft.copy.limitedspawners.networking.LimitedSpawnersPacketHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * 	Small mod adding more functionality to Mob Spawners (Minecraft Forge 1.20)
 * 
 * 	@author Anders <Branders> Blomqvist
 */
@Mod(LimitedSpawners.MOD_ID)
public class LimitedSpawners {

	public static final String MOD_ID = "limitedspawners";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	/**
	 * 	Start of the mod.
	 * 
	 * 	- Register common setup for config init.
	 *  - Register network packets
	 *  - Register items and blocks
	 *  - Register event handler
	 */
	public LimitedSpawners() {

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Register the commonSetup method for modloading
		modEventBus.addListener(this::commonSetup);

		LimitedSpawnersPacketHandler.register();
		MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		// When init config here all modded entities are loaded which is needed for creating
		// the keys in the CONFIG_SPEC
		ModConfigManager.initConfig(MOD_ID, FMLPaths.CONFIGDIR.get());
	}
}