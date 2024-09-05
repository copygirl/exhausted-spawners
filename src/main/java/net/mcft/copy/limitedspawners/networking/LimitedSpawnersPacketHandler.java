package net.mcft.copy.limitedspawners.networking;

import net.mcft.copy.limitedspawners.LimitedSpawners;
import net.mcft.copy.limitedspawners.networking.packet.SyncSpawnerConfig;
import net.mcft.copy.limitedspawners.networking.packet.SyncSpawnerEggDrop;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 	Register network messages
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class LimitedSpawnersPacketHandler {
	
	private static final String PROTOCOL_VERSION = "1";
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(LimitedSpawners.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);
	
	public static void register() {
		int messageId = 0;
		
		INSTANCE.registerMessage(messageId++, 
				SyncSpawnerEggDrop.class, 
				SyncSpawnerEggDrop::encode, 
				SyncSpawnerEggDrop::decode, 
				SyncSpawnerEggDrop::handle);
		
		INSTANCE.registerMessage(messageId++, 
				SyncSpawnerConfig.class, 
				SyncSpawnerConfig::encode, 
				SyncSpawnerConfig::decode, 
				SyncSpawnerConfig::handle);
	}
}
