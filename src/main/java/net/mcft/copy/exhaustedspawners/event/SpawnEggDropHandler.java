package net.mcft.copy.exhaustedspawners.event;

import net.mcft.copy.exhaustedspawners.Config;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class SpawnEggDropHandler {

	@SubscribeEvent
	public void onLivingDropsEvent(LivingDropsEvent event) {
		var chance = Config.SPAWN_EGG_DROP_CHANCE.get().doubleValue();
		if (chance <= 0.0) return; // Functionality is disabled.

		var causedByPlayer = event.getSource().getEntity() instanceof Player;
		if (Config.SPAWN_EGG_PLAYER_KILL_REQUIRED.get() && !causedByPlayer) return;

		var entity     = event.getEntity();
		var entityType = entity.getType();
		var entityId   = EntityType.getKey(entityType);

		// Check if RNGesus is with us this day.
		if (entity.getRandom().nextFloat() >= chance) return;

		// If entity id is contained in blacklist, don't drop anything.
		if (Config.SPAWN_EGG_DROP_BLACKLIST.get().contains(entityId.toString())) return;

		var spawnEgg = ForgeSpawnEggItem.fromEntityType(entityType);
		if (spawnEgg == null) return; // No spawn egg found.

		event.getDrops().add(new ItemEntity(entity.level(),
				entity.getX(), entity.getY(), entity.getZ(),
				new ItemStack(spawnEgg)));
	}
}
