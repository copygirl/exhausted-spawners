package net.mcft.copy.exhaustedspawners.platform;

import javax.annotation.Nullable;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import net.mcft.copy.exhaustedspawners.platform.services.IPlatformHelper;

public class ForgePlatformHelper implements IPlatformHelper {

	@Override
	public String getPlatformName() { return "Forge"; }

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	@Nullable
	public SpawnerBlockEntity getBaseSpawnerBlockEntity(BaseSpawner spawner) {
		return (SpawnerBlockEntity)spawner.getSpawnerBlockEntity();
	}
}
