package net.mcft.copy.exhaustedspawners.event;

import java.util.Optional;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;

public final class SpawnerRefillingHandler {
	private SpawnerRefillingHandler() {  }

	public static void register() {
		UseBlockCallback.EVENT.register(SpawnerRefillingHandler::onUseBlock);
	}

	private static InteractionResult onUseBlock(
			Player player, Level level, InteractionHand hand, HitResult hit) {
		if (!(hit instanceof BlockHitResult)) return InteractionResult.PASS;

		var pos   = ((BlockHitResult)hit).getBlockPos();
		var block = level.getBlockState(pos).getBlock();
		if (block != Blocks.SPAWNER) return InteractionResult.PASS;

		var heldStack = player.getItemInHand(hand);
		if (!(heldStack.getItem() instanceof SpawnEggItem)) return InteractionResult.PASS;
		var eggItem = (SpawnEggItem)heldStack.getItem();

		if (level.isClientSide) return InteractionResult.PASS;

		var spawner = Optional.ofNullable(level.getBlockEntity(pos))
			.map(SpawnerBlockEntity.class::cast)
			.map(SpawnerBlockEntity::getSpawner)
			.map(ILimitedSpawner.class::cast)
			.orElse(null);
		if (spawner == null) return InteractionResult.FAIL;

		if (player.isCreative()) {
			// Keep the default behavior of SpawnEggItem, but
			// also make sure to reset the remaining mob count.
			spawner.setLimit(-1);
			return InteractionResult.PASS;
		} else if (player.isSpectator()) {
			// Do nothing if spectator.
			return InteractionResult.PASS;
		} else {
			var remaining = spawner.getRemaining();
			var isEmpty   = remaining <= 0;

			var spawnType = spawner.getSpawnedEntityType();
			var eggType   = eggItem.getType(heldStack.getTag());
			// Can't refill a spawner when using the wrong type of spawn egg.
			if (!isEmpty && (spawnType != eggType)) return InteractionResult.FAIL;

			var refill = Config.AMOUNT_REFILLED.get().intValue();
			if (refill <= 0) return InteractionResult.FAIL;

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
				if (refill <= 0) return InteractionResult.FAIL;
			}

			spawner.setLimit(limit + refill);

			// If we went over the limit, play a fizzle animation at the spawner.
			if (overLimit > 0) level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);

			player.awardStat(Stats.ITEM_USED.get(eggItem));
			player.swing(hand);
			heldStack.shrink(1);

			return InteractionResult.SUCCESS;
		}
	}
}
