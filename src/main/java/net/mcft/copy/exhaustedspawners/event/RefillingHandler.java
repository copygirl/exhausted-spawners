package net.mcft.copy.exhaustedspawners.event;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.ExhaustedSpawners;
import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class RefillingHandler {

	@SubscribeEvent
	public void onRightClickBlockEvent(RightClickBlock event) {
		var level = event.getLevel();
		if (level.isClientSide) return;

		var pos   = event.getPos();
		var block = level.getBlockState(pos).getBlock();
		if (!(block instanceof SpawnerBlock)) return;

		var heldStack = event.getItemStack();
		if (!(heldStack.getItem() instanceof SpawnEggItem)) return;
		var eggItem = (SpawnEggItem)heldStack.getItem();

		var tileEntity = level.getBlockEntity(pos);
		if (!(tileEntity instanceof SpawnerBlockEntity)) return;
		var spawner = (ILimitedSpawner)((SpawnerBlockEntity)tileEntity).getSpawner();

		var player = event.getEntity();
		if (player.isCreative()) {
			// Keep the default behavior of SpawnEggItem, but
			// also make sure to reset the remaining mob count.
			spawner.setLimit(-1);
		} else if (player.isSpectator()) {
			// Do nothing if spectator.
		} else {
			var isEmpty   = spawner.isEmpty();
			var spawnType = spawner.getSpawnedEntityType();
			var eggType   = eggItem.getType(heldStack.getTag());

			var charge  = Config.AMOUNT_REFILLED.get().intValue();
			var spawned = spawner.getSpawned();
			var limit   = spawner.getLimit();

			if ((charge > 0) && (isEmpty || (spawnType == eggType))) {

				if (isEmpty) {
					spawner.setSpawnedEntityType(eggType);
					spawner.setLimit(spawned + charge);
					ExhaustedSpawners.LOGGER.info("EMPTY! spawned={}, limit={}", spawner.getSpawned(), spawner.getLimit());
				} else {
					spawner.setLimit(limit + charge);
					ExhaustedSpawners.LOGGER.info("REFILL! spawned={}, limit={}", spawner.getSpawned(), spawner.getLimit());
				}

				player.awardStat(Stats.ITEM_USED.get(eggItem));
				player.swing(event.getHand());
				heldStack.shrink(1);
			}

			event.setCanceled(true);
		}
	}
}
