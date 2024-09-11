package net.mcft.copy.exhaustedspawners.event;

import java.util.Optional;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;

public final class SpawnerHarvestHandler {
	private SpawnerHarvestHandler() {  }

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register(SpawnerHarvestHandler::onBlockBreakEvent);
	}

	private static void onBlockBreakEvent(
			Level level, Player player, BlockPos pos,
			BlockState state, BlockEntity blockEntity) {
		if (state.getBlock() != Blocks.SPAWNER) return;
		if (level.isClientSide) return;
		if (player.isCreative()) return;

		var heldTool    = player.getMainHandItem();
		var silkTouch   = EnchantmentHelper.hasSilkTouch(heldTool);
		var correctTool = player.hasCorrectToolForDrops(state);
		if (!correctTool) silkTouch = false; // Can't silk touch using incorrect tool.

		var spawner = Optional.of(blockEntity)
			.map(SpawnerBlockEntity.class::cast)
			.map(SpawnerBlockEntity::getSpawner)
			.map(ILimitedSpawner.class::cast);

		var remaining  = spawner.map(ILimitedSpawner::getRemaining).orElse(0).intValue();
		var entityType = spawner.map(ILimitedSpawner::getSpawnedEntityType).orElse(null);
		var eggItem    = SpawnEggItem.byId(entityType);

		var refillAmount  = Config.AMOUNT_REFILLED.get().intValue();
		var maxEggsToDrop = (refillAmount > 0) ? (remaining / refillAmount) : 0;
		var eggDropChance = silkTouch ? Config.DROP_CHANCE_PER_EGG_SILK_TOUCH.get().doubleValue()
		                              : Config.DROP_CHANCE_PER_EGG.get().doubleValue();

		var eggsToDrop = 0;
		var random = level.getRandom();
		if ((eggItem != null) && (eggDropChance > 0))
			for (var i = 0; i < maxEggsToDrop; i++)
				if (random.nextFloat() <= eggDropChance)
					eggsToDrop++;

		// Drop spawn eggs, if any.
		if ((eggItem != null) && (eggsToDrop > 0))
			Block.popResource((Level)level, pos, new ItemStack(eggItem, eggsToDrop));

		if (silkTouch && Config.SPAWNER_SILK_TOUCH.get()) {
			// If the spawner is going to be silk-touched, don't drop any experience.
		} else {
			var baseAmount   = Config.XP_BASE.get().intValue();
			var remainingCap = Config.XP_REMAINING_CAP.get().intValue();
			var perRemaining = Config.XP_PER_REMAINING.get().doubleValue();

			// Reduce amount of remaining mobs depending on spawn
			// eggs dropped for the purpose of XP drop calculations.
			remaining = Math.max(0, remaining - eggsToDrop * refillAmount);

			var amount = baseAmount + Math.min(remainingCap, remaining) * perRemaining;
			var exp = (int)(amount + random.nextDouble() * amount + random.nextDouble() * amount);
			ExperienceOrb.award((ServerLevel)level, Vec3.atCenterOf(pos), exp);
		}
	}
}
