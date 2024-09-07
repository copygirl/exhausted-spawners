package net.mcft.copy.exhaustedspawners.compat;

import java.util.Optional;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.ExhaustedSpawners;
import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class SpawnerRemainingPlugin implements IWailaPlugin {

	private static ResourceLocation ID = new ResourceLocation(ExhaustedSpawners.MOD_ID, "remaining");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ComponentProvider.INSTANCE, SpawnerBlockEntity.class);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(ComponentProvider.INSTANCE, SpawnerBlock.class);
	}

	public enum ComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
		INSTANCE;

		@Override
		public ResourceLocation getUid() { return SpawnerRemainingPlugin.ID; }

		@Override
		public void appendServerData(CompoundTag data, BlockAccessor accessor) {
			var spawner = Optional.of(accessor.getBlockEntity())
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
		@OnlyIn(Dist.CLIENT)
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			var data = accessor.getServerData();
			if (!data.contains("remaining")) return;

			var helper = tooltip.getElementHelper();
			tooltip.add(Component.translatable(
					ExhaustedSpawners.MOD_ID + ".remaining",
					data.getInt("remaining"),
					data.getInt("limit")));

			var eggItem = Optional.of(new ResourceLocation(data.getString("entity")))
				.map(ForgeRegistries.ENTITY_TYPES::getValue)
				.map(ForgeSpawnEggItem::fromEntityType)
				.orElse(null);
			if (eggItem == null) return;

			var item = new ItemStack(eggItem);
			tooltip.append(helper.spacer(2, 0));
			tooltip.append(helper.smallItem(item).translate(new Vec2(0, -2)));
		}
	}
}
