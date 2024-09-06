package net.mcft.copy.exhaustedspawners.event;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class SpawnerRefillingHandler {

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

			var refill = Config.AMOUNT_REFILLED.get().intValue();
			if ((refill > 0) && (isEmpty || (spawnType == eggType))) {

				var configurated_limit = Config.SPAWN_LIMIT.get().intValue();
				var overfill_behavior  = Config.OVERFILL_BEHAVIOR.get();
				var spawned = spawner.getSpawned();
				var limit   = spawner.getLimit();

				var remaining  = !isEmpty ? spawner.getRemaining() : 0;
				var over_limit = (remaining + refill) - configurated_limit;

				if (isEmpty) {
					spawner.setSpawnedEntityType(eggType);
					limit = spawned;
				} else if (over_limit > 0)
					switch (overfill_behavior) {
						case DENY   : refill = 0; break;
						case FIZZLE : refill -= over_limit; break;
						case ALLOW  : over_limit = 0; break;
					}

				if (refill > 0) {
					spawner.setLimit(limit + refill);

					// If we went over the limit, play a fizzle animation at the spawner.
					if (over_limit > 0) level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);

					player.awardStat(Stats.ITEM_USED.get(eggItem));
					player.swing(event.getHand());
					heldStack.shrink(1);
				}
			}

			event.setCanceled(true);
		}
	}
}
