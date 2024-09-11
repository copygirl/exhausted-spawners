package net.mcft.copy.exhaustedspawners.platform;

import javax.annotation.Nullable;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

import net.mcft.copy.exhaustedspawners.platform.services.IPlatformHelper;

public class FabricPlatformHelper implements IPlatformHelper {

	@Override
	public String getPlatformName() { return "Fabric"; }

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	@Nullable
	public SpawnerBlockEntity getBaseSpawnerBlockEntity(BaseSpawner spawner) {
		return (spawner instanceof IGetSpawnerBlockEntity)
			? ((IGetSpawnerBlockEntity)spawner).getSpawnerBlockEntity()
			: null;
	}
}
