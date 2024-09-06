package net.mcft.copy.exhaustedspawners.event;

import net.mcft.copy.exhaustedspawners.Config;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
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
}
