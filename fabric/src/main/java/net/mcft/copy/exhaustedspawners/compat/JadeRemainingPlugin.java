package net.mcft.copy.exhaustedspawners.compat;

import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;
import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.Constants;

@WailaPlugin
public class JadeRemainingPlugin implements IWailaPlugin {

	private static ResourceLocation ID
		= new ResourceLocation(Constants.MOD_ID, "remaining");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ComponentProvider.INSTANCE, SpawnerBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(ComponentProvider.INSTANCE, SpawnerBlock.class);
	}

	public enum ComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
		INSTANCE;

		@Override
		public ResourceLocation getUid() { return JadeRemainingPlugin.ID; }

		@Override
		public void appendServerData(CompoundTag data, BlockAccessor accessor) {
			var spawner = Optional.ofNullable(accessor.getBlockEntity())
				.map(SpawnerBlockEntity.class::cast)
				.map(SpawnerBlockEntity::getSpawner)
				.map(ILimitedSpawner.class::cast)
				.orElse(null);
			if (spawner == null) return;

			var configLimit = Config.SPAWN_LIMIT.get();
			if (configLimit <= 0) return;
			var entityType = spawner.getSpawnedEntityType();
			if (entityType == null) return;

			data.putInt("remaining", spawner.getRemaining());
			data.putInt("limit", configLimit);
			data.putString("entity", EntityType.getKey(entityType).toString());
		}

		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			var data = accessor.getServerData();
			if (!data.contains("remaining")) return;

			var helper = tooltip.getElementHelper();
			tooltip.add(Component.translatable(
					Constants.MOD_ID + ".remaining",
					data.getInt("remaining"),
					data.getInt("limit")));

			var entityType = new ResourceLocation(data.getString("entity"));
			var eggItem = BuiltInRegistries.ENTITY_TYPE
					.getOptional(entityType)
					.map(SpawnEggItem::byId)
					.orElse(null);
			if (eggItem == null) return;

			var item = new ItemStack(eggItem);
			tooltip.append(helper.spacer(2, 0));
			tooltip.append(helper.smallItem(item).translate(new Vec2(0, -2)));
		}
	}
}
