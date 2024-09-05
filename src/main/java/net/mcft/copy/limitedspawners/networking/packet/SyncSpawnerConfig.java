package net.mcft.copy.limitedspawners.networking.packet;

import java.util.function.Supplier;

import net.mcft.copy.limitedspawners.config.ConfigValues;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * 	Config values needs to be synced from server to client
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class SyncSpawnerConfig {
	private int limitedSpawns;
	private int limitedSpawnsAmount;
	
	public SyncSpawnerConfig(int limitedSpawns, int limitedSpawnsAmount) {
		this.limitedSpawns = limitedSpawns;
		this.limitedSpawnsAmount = limitedSpawnsAmount;
	}
	
	public static void encode(SyncSpawnerConfig msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.limitedSpawns);
		buf.writeInt(msg.limitedSpawnsAmount);
	}
	
	public static SyncSpawnerConfig decode(FriendlyByteBuf buf) {
		int limitedSpawns = buf.readInt();
		int limitedSpawnsAmount = buf.readInt();
		return new SyncSpawnerConfig(limitedSpawns, limitedSpawnsAmount);
	}
	
	public static void handle(SyncSpawnerConfig msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ConfigValues.sync(msg.limitedSpawns, msg.limitedSpawnsAmount));
	}
}
