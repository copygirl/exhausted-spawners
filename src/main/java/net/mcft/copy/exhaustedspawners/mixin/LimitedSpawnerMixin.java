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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.LevelEvent;

@Mixin(BaseSpawner.class)
public abstract class LimitedSpawnerMixin implements ILimitedSpawner {

	/** Total number of mobs spawned by this spawner. */
	private int spawned = 0;
	/** Current limit of mobs that may be spawned. */
	private int limit = -1;

	@Inject(method = "load", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/nbt/CompoundTag;getShort(Ljava/lang/String;)S"))
	private void load(Level level, BlockPos pos, CompoundTag nbt, CallbackInfo info) {
		if (Config.SPAWN_LIMIT.get() == 0) return; // Functionality disabled.
		spawned = nbt.getInt(SPAWNED_NBT_KEY);
		limit   = nbt.contains(LIMIT_NBT_KEY) ? nbt.getInt(LIMIT_NBT_KEY) : -1;
	}

	@Inject(method = "save", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/nbt/CompoundTag;putShort(Ljava/lang/String;S)V"))
	private void save(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> info) {
		if (Config.SPAWN_LIMIT.get() == 0) return;
		nbt.putInt(SPAWNED_NBT_KEY, spawned);
		nbt.putInt(LIMIT_NBT_KEY, limit);
	}

	@Inject(method = "serverTick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
	private void onEntitySpawned(ServerLevel level, BlockPos pos, CallbackInfo ci) {
		var configLimit = Config.SPAWN_LIMIT.get();
		if (configLimit == 0) return; // Functionality disabled.

		// If limit is unset, set it to the configured limit.
		if (limit < 0) setLimit(spawned + configLimit);

		spawned++; // Increase number of mobs spawned so far.
		if (getRemaining() <= 0) clear(); // Empty, so fizzle!
	}

	@ModifyVariable(method = "serverTick", at = @At("STORE"))
	private CompoundTag stopSpawningWhenEmpty(CompoundTag entityToSpawn) {
		return (getRemaining() > 0) ? entityToSpawn : new CompoundTag();
	}


	// ILimitedSpawner implementation

	@Override
	public int getSpawned() { return spawned; }

	@Override
	public int getLimit() {
		var configLimit = Config.SPAWN_LIMIT.get();
		if (configLimit <= 0) return Integer.MAX_VALUE;
		return (limit >= 0) ? limit : spawned + configLimit;
	}

	@Override
	public void setLimit(int value) { limit = value; }

	@Override
	public int getRemaining() {
		if (getSpawnedEntityType() == null) return 0;
		var configLimit = Config.SPAWN_LIMIT.get();
		if (configLimit <= 0) return Integer.MAX_VALUE;
		return (limit >= 0) ? Math.max(0, limit - spawned)
		                    : configLimit;
	}

	@Override
	@Nullable
	public EntityType<?> getSpawnedEntityType() {
		if (nextSpawnData == null) return null;
		return EntityType.by(nextSpawnData.entityToSpawn()).orElse(null);
	}

	@Override
	public void setSpawnedEntityType(@Nullable EntityType<?> value) {
		var spawner     = (BaseSpawner)(Object)this;
		var blockEntity = spawner.getSpawnerBlockEntity();
		if (blockEntity == null) return;

		var level = blockEntity.getLevel();
		if ((level == null) || level.isClientSide) return;

		var pos = blockEntity.getBlockPos();
		if (value != null) spawner.setEntityId(value, level, level.random, pos);
		else invokeSetNextSpawnData(level, pos, new SpawnData());
	}

	@Override
	public void clear() {
		var spawner     = (BaseSpawner)(Object)this;
		var blockEntity = spawner.getSpawnerBlockEntity();
		if (blockEntity == null) return;

		var level = blockEntity.getLevel();
		if ((level == null) || level.isClientSide) return;

		var pos = blockEntity.getBlockPos();
		// If spawner is not already empty, cause a fizz "animation" on the spawner.
		if (getSpawnedEntityType() != null) level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
		// Clear out nextSpawnData, removing the entity in the spawner.
		invokeSetNextSpawnData(level, pos, new SpawnData());
	}

	@Shadow
	private SpawnData nextSpawnData;

	@Invoker("setNextSpawnData")
	protected abstract void invokeSetNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData value);
}
