package net.mcft.copy.exhaustedspawners.event;

import java.util.Random;

import net.mcft.copy.exhaustedspawners.Config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 	Handles all the events regarding the mob spawner and entities.
 *
 * 	@author Anders <Branders> Blomqvist
 */
@EventBusSubscriber
public class SpawnerEventHandler {

	private Random random = new Random();

	/**
	 * 	Enables mobs to have a small chance to drop an egg
	 */
	@SubscribeEvent
	public void onMobDrop(LivingDropsEvent event) {

		boolean causedByPlayer = event.getSource().getEntity() instanceof Player ? true : false;

		// Leave if eggs should only drop when killed by a player
		if(Config.SPAWN_EGG_PLAYER_KILL_REQUIRED.get() && !causedByPlayer)
			return;

		if(random.nextFloat() >= Config.SPAWN_EGG_DROP_CHANCE.get())
			return;

		Entity entity = event.getEntity();
		EntityType<?> entityType = entity.getType();

		// Leave if it was a player
		if(entityType.equals(EntityType.PLAYER))
			return;

		// Entity type string is: "entity.minecraft.pig"
		// Convert to "minecraft:pig"
		String entityName = getEntityName(entityType);

		if(Config.SPAWN_EGG_DROP_BLACKLIST.get().contains(entityName))
			return;

		ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS .getValue(new ResourceLocation(entityName + "_spawn_egg")));

		// Add monster egg to drops
		event.getDrops().add(new ItemEntity(
				entity.level(),
				entity.getX(),
				entity.getY(),
				entity.getZ(),
				itemStack));
	}

	/**
	 * 	Transform the entityType string to colon format: {@code modid:entityname}
	 *
	 * 	Note: {@code entityType.toString()} returns "entity.modid.entityname"
	 *
	 * 	@param entityType
	 * 	@returns entity name as "modid:entityname".
	 */
	private String getEntityName(EntityType<?> entityType) {
		String entity = entityType.toString();
		String[] dotSplit = entity.split("\\.");
		String entityName = dotSplit[1] + ":" + dotSplit[2];
		return entityName;
	}
}
