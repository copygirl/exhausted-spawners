package net.mcft.copy.limitedspawners.api;

/**
 * Implemented by BaseSpawner using a mixin.
 * Does nothing meaningful on the client.
 */
public interface ILimitedSpawner {

	/** The NBT key used to save/load the number of spawned mobs. */
	static final String SPAWNED_NBT_KEY = "limitedspawners:spawned";

	/**
	 * Returns the configurated limit of mobs that can be spawned before
	 * this spawner empties out, or 0 if this functionality is disabled.
	 */
	int getLimit();

	/**
	 * Returns if this spawner is empty and can't spawn any more mobs.
	 */
	boolean isEmpty();

	/**
	 * Returns the amount of remaining mobs to be spawned from this spawner,
	 * or {@link Integer#MAX_VALUE} if the functionality is disabled.
	 */
	int getRemaining();

	/**
	 * Adjusts the amount of remaining mobs to be
	 * spawned from this spawner by a relative value.
	 */
	void adjustRemaining(int value);

	/**
	 * Resets the amount of remaining mobs to be
	 * spawned from this spawner to the default limit.
	 */
	void resetRemaining();

	/**
	 * Empties this spawner, removing the currently spawned mob from it.
	 * If the spawner wasn't already empty, plays a fizzle animation.
	 */
	void clear();
}
