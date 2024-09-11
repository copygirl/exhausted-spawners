package net.mcft.copy.exhaustedspawners.event;

import java.util.Optional;

import net.minecraft.stats.Stats;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;

@EventBusSubscriber
public class SpawnerRefillingHandler {

	public static void register() {
		MinecraftForge.EVENT_BUS.register(new SpawnerRefillingHandler());
	}

	@SubscribeEvent
	public void onRightClickBlockEvent(RightClickBlock event) {
		var pos   = event.getPos();
		var level = event.getLevel();
		var block = level.getBlockState(pos).getBlock();
		if (!(block instanceof SpawnerBlock)) return;

		var heldStack = event.getItemStack();
		if (!(heldStack.getItem() instanceof SpawnEggItem)) return;
		var eggItem = (SpawnEggItem)heldStack.getItem();

		event.setCanceled(true);
		if (level.isClientSide) return;

		var spawner = Optional.ofNullable(level.getBlockEntity(pos))
			.map(SpawnerBlockEntity.class::cast)
			.map(SpawnerBlockEntity::getSpawner)
			.map(ILimitedSpawner.class::cast)
			.orElse(null);
		if (spawner == null) return;

		var player = event.getEntity();
		if (player.isCreative()) {
			// Keep the default behavior of SpawnEggItem, but
			// also make sure to reset the remaining mob count.
			event.setCanceled(false);
			spawner.setLimit(-1);
		} else if (player.isSpectator()) {
			// Do nothing if spectator.
		} else {
			var remaining = spawner.getRemaining();
			var isEmpty   = remaining <= 0;

			var spawnType = spawner.getSpawnedEntityType();
			var eggType   = eggItem.getType(heldStack.getTag());
			// Can't refill a spawner when using the wrong type of spawn egg.
			if (!isEmpty && (spawnType != eggType)) return;

			var refill = Config.AMOUNT_REFILLED.get().intValue();
			if (refill <= 0) return;

			var configLimit = Config.SPAWN_LIMIT.get().intValue();
			var overLimit   = (remaining + refill) - configLimit;
			var limit       = spawner.getLimit();

			if (isEmpty) {
				spawner.setSpawnedEntityType(eggType);
				limit = spawner.getSpawned();
			} else if (overLimit > 0) {
				var behavior = Config.OVERFILL_BEHAVIOR.get();
				switch (behavior) {
					case DENY   : refill = 0; break;
					case FIZZLE : refill -= overLimit; break;
					case ALLOW  : overLimit = 0; break;
				}
				if (refill <= 0) return;
			}

			spawner.setLimit(limit + refill);

			// If we went over the limit, play a fizzle animation at the spawner.
			if (overLimit > 0) level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);

			player.awardStat(Stats.ITEM_USED.get(eggItem));
			player.swing(event.getHand());
			heldStack.shrink(1);
		}
	}
}
