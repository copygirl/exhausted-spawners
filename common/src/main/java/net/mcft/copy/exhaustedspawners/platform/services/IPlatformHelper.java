package net.mcft.copy.exhaustedspawners.platform.services;

import javax.annotation.Nullable;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public interface IPlatformHelper {

	/** Gets the name of the current platform. */
	String getPlatformName();

	/** Checks if a mod with the given ID is loaded. */
	boolean isModLoaded(String modId);

	/** Check if the game is currently in a development environment. */
	boolean isDevelopmentEnvironment();

	/** Gets the name of the environment type as a string. */
	default String getEnvironmentName() {
		return isDevelopmentEnvironment() ? "development" : "production";
	}

	/** Gets the block entity owning this BaseSpawner instance. */
	@Nullable SpawnerBlockEntity getBaseSpawnerBlockEntity(BaseSpawner spawner);
}
