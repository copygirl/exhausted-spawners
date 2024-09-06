package net.mcft.copy.exhaustedspawners.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.LevelEvent;

@Mixin(BaseSpawner.class)
public abstract class LimitedSpawnerMixin implements ILimitedSpawner {

	/**
	 * Number of mobs spawned by this spawner.
	 * May be decreased or reset to increase the limit.
	 * May be negative to increase the limit beyond the default.
	 */
	private int spawned = 0;

	@Inject(method = "load", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/nbt/CompoundTag;getShort(Ljava/lang/String;)S"))
	private void load(Level level, BlockPos pos, CompoundTag nbt, CallbackInfo info) {
		if (getLimit() == 0) return; // Check if limited spawns are enabled.
		spawned = nbt.getInt(ILimitedSpawner.SPAWNED_NBT_KEY);
	}

	@Inject(method = "save", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/nbt/CompoundTag;putShort(Ljava/lang/String;S)V"))
	private void save(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> info) {
		if (getLimit() == 0) return;
		nbt.putInt(ILimitedSpawner.SPAWNED_NBT_KEY, spawned);
	}

	@Inject(method = "serverTick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
	private void onEntitySpawned(ServerLevel level, BlockPos pos, CallbackInfo ci) {
		if (getLimit() == 0) return; // Functionality disabled.
		var remaining = getRemaining() - 1;
		if (remaining <= 0) clear();
		else adjustRemaining(-1);
	}

	@ModifyVariable(method = "serverTick", at = @At("STORE"))
	private CompoundTag stopSpawningWhenEmpty(CompoundTag entityToSpawn) {
		return (getRemaining() > 0) ? entityToSpawn : new CompoundTag();
	}


	// ILimitedSpawner implementation

	@Override
	public int getLimit() {
		return Config.SPAWNER_SPAWN_LIMIT.get();
	}

	@Override
	public boolean isEmpty() {
		if (nextSpawnData == null) return true;
		if (!nextSpawnData.entityToSpawn().contains("id", 8)) return true;
		if (getLimit() == 0) return false;
		return getRemaining() == 0;
	}

	@Override
	public int getRemaining() {
		var limit = getLimit();
		if (limit == 0) return Integer.MAX_VALUE;
		if (nextSpawnData == null) return 0;
		if (!nextSpawnData.entityToSpawn().contains("id", 8)) return 0;
		return Math.max(0, limit - spawned);
	}

	@Override
	public void adjustRemaining(int value) {
		var limit = getLimit();
		spawned = Math.min(limit, spawned - value);
	}

	@Override
	public void resetRemaining() {
		spawned = 0;
	}

	@Override
	public void clear() {
		var spawner = (BaseSpawner)(Object)this;
		var blockEntity = spawner.getSpawnerBlockEntity();
		if (blockEntity == null) return;

		var level = blockEntity.getLevel();
		if ((level == null) || level.isClientSide) return;

		if (!isEmpty()) level.levelEvent(
			LevelEvent.LAVA_FIZZ,
			blockEntity.getBlockPos(), 0);

		var pos = blockEntity.getBlockPos();
		invokeSetNextSpawnData(level, pos, new SpawnData());
		resetRemaining();
	}

	@Shadow
	private SpawnData nextSpawnData;

	@Invoker("setNextSpawnData")
	protected abstract void invokeSetNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData value);
}
