package net.mcft.copy.exhaustedspawners.event;

import java.util.Optional;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.api.ILimitedSpawner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class SpawnerHarvestHandler {

	@SubscribeEvent
	public void onBreakSpeedEvent(BreakSpeed event) {
		if (!(event.getState().getBlock() instanceof SpawnerBlock)) return;

		var multiplier = Config.SPAWNER_BREAK_SPEED.get().floatValue();
		event.setNewSpeed(event.getOriginalSpeed() * multiplier);
	}

	@SubscribeEvent
	public void onBreakEvent(BreakEvent event) {
		if (!(event.getState().getBlock() instanceof SpawnerBlock)) return;

		var pos   = event.getPos();
		var level = event.getLevel();
		if (level.isClientSide()) return;

		var player = event.getPlayer();
		if (player.isCreative()) return;

		var heldTool    = player.getMainHandItem();
		var silkTouch   = heldTool.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0;
		var correctTool = player.hasCorrectToolForDrops(event.getState());
		if (!correctTool) silkTouch = false; // Can't silk touch using incorrect tool.

		var spawner = Optional.of(level.getBlockEntity(pos))
			.map(SpawnerBlockEntity.class::cast)
			.map(SpawnerBlockEntity::getSpawner)
			.map(ILimitedSpawner.class::cast);

		var remaining  = spawner.map(ILimitedSpawner::getRemaining).orElse(0).intValue();
		var entityType = spawner.map(ILimitedSpawner::getSpawnedEntityType).orElse(null);
		var eggItem    = ForgeSpawnEggItem.fromEntityType(entityType);

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
		if ((eggItem != null) && (eggsToDrop > 0) && (level instanceof Level))
			Block.popResource((Level)level, pos, new ItemStack(eggItem, eggsToDrop));

		if (silkTouch && Config.SPAWNER_SILK_TOUCH.get()) {
			// If the spawner is going to be silk-touched, don't drop any experience.
			event.setExpToDrop(0);
		} else {
			var baseAmount   = Config.XP_BASE.get().intValue();
			var remainingCap = Config.XP_REMAINING_CAP.get().intValue();
			var perRemaining = Config.XP_PER_REMAINING.get().doubleValue();

			// Reduce amount of remaining mobs depending on spawn
			// eggs dropped for the purpose of XP drop calculations.
			remaining = Math.max(0, remaining - eggsToDrop * refillAmount);

			var amount = baseAmount + Math.min(remainingCap, remaining) * perRemaining;
			event.setExpToDrop((int)(amount + random.nextDouble() * amount + random.nextDouble() * amount));
		}
	}
}
