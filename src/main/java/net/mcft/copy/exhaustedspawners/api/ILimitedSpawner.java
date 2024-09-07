package net.mcft.copy.exhaustedspawners.api;

import javax.annotation.Nullable;

import net.mcft.copy.exhaustedspawners.ExhaustedSpawners;
import net.minecraft.world.entity.EntityType;

/**
 * Implemented by BaseSpawner using a mixin.
 * Does nothing meaningful on the client.
 */
public interface ILimitedSpawner {

	// NBT keys used to save/load additional fields added by LimitedSpawnerMixin.
	static final String SPAWNED_NBT_KEY = ExhaustedSpawners.MOD_ID + ":spawned";
	static final String LIMIT_NBT_KEY   = ExhaustedSpawners.MOD_ID + ":limit";

	/** Gets how many mobs this spawner has spawned total. */
	int getSpawned();

	/** Returns the limit of mobs that can be spawned before this spawner empties. */
	int getLimit();

	/** Sets the limit of mobs that can be spawned from this spawner.
	 *  If set to -1, will reset the spawner to the default configured limit. */
	void setLimit(int value);

	/** Returns the amount of remaining mobs to be spawned from this spawner. */
	int getRemaining();

	/** Gets the type of entity spawned from this spawner.
	 *  Returns null if empty or type can't be determined. */
	@Nullable EntityType<?> getSpawnedEntityType();

	/** Sets the type of entity spawned from this spawner. */
	void setSpawnedEntityType(@Nullable EntityType<?> value);

	/** Empties this spawner, removing the currently spawned mob from it.
	 *  If the spawner wasn't already empty, plays a fizzle animation. */
	void clear();
}
