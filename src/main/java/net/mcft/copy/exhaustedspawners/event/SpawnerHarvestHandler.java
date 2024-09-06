package net.mcft.copy.exhaustedspawners.event;

import net.mcft.copy.exhaustedspawners.Config;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.SpawnerBlock;
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
		if (!Config.SPAWNER_SILK_TOUCH.get()) return;

		var player      = event.getPlayer();
		var heldTool    = player.getMainHandItem();
		var silkTouch   = heldTool.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0;
		var correctTool = player.hasCorrectToolForDrops(event.getState());

		// Don't drop any experience if the spawner is silk-touched.
		if (silkTouch && correctTool) event.setExpToDrop(0);
	}
}
