package net.mcft.copy.exhaustedspawners.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import net.mcft.copy.exhaustedspawners.Config;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class SpawnEggDropHandler {

	private List<ItemStack> capturedEquipment;

	@SubscribeEvent
	public void onLivingDeathEvent(LivingDeathEvent event) {
		// Capture the current equipment of the dying entity so we can check it later.
		capturedEquipment = Arrays.stream(EquipmentSlot.values())
				.map(event.getEntity()::getItemBySlot)
				.filter(Predicate.not(ItemStack::isEmpty))
				.toList();
	}

	/** Clears any items from the drops collection that weren't part of the entity's equipment. */
	private void clearNonEquipment(Collection<ItemEntity> drops) {
		drops.removeIf(itemEntity -> !capturedEquipment.contains(itemEntity.getItem()));
	}

	@SubscribeEvent
	public void onLivingDropsEvent(LivingDropsEvent event) {
		var chance = Config.SPAWN_EGG_DROP_CHANCE.get().doubleValue();
		if (chance <= 0.0) return; // Functionality is disabled.

		var killer = event.getSource().getEntity();
		var causedByPlayer = killer instanceof Player;
		if (Config.SPAWN_EGG_PLAYER_KILL_REQUIRED.get() && !causedByPlayer) return;

		// Apply looting bonus to drop chance.
		chance += event.getLootingLevel() * Config.SPAWN_EGG_DROP_CHANCE_LOOTING_BONUS.get();

		// TODO: Technically incorrect, but there might not be a way to get the actual item used for the kill.
		var killerItem = (killer instanceof LivingEntity) ? ((LivingEntity)killer).getMainHandItem() : ItemStack.EMPTY;
		var silkTouch  = killerItem.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0;

		// If not using a silk touch item, multiply drop chance by non-silktouch modifier.
		if (!silkTouch) chance *= Config.SPAWN_EGG_NON_SILK_TOUCH_MODIFIER.get();

		var entity = event.getEntity();

		// If a silk touch item is used, and drops should be cleared when using silk touch, do that.
		if (silkTouch && Config.SPAWN_EGG_CLEAR_DROPS_WHEN_SILK_TOUCH.get()) clearNonEquipment(event.getDrops());

		// Check if RNGesus is with us this day.
		if (entity.getRandom().nextFloat() >= chance) return;

		var entityType = entity.getType();
		var entityId   = EntityType.getKey(entityType);

		// If entity id is contained in blacklist, don't drop anything.
		if (Config.SPAWN_EGG_DROP_BLACKLIST.get().contains(entityId.toString())) return;

		var spawnEgg = ForgeSpawnEggItem.fromEntityType(entityType);
		if (spawnEgg == null) return; // No spawn egg found.

		// If mob drops should be cleared when a spawn egg is dropped, do that.
		if (Config.SPAWN_EGG_CLEAR_DROPS_WHEN_EGG.get()) clearNonEquipment(event.getDrops());

		event.getDrops().add(new ItemEntity(entity.level(),
				entity.getX(), entity.getY(), entity.getZ(),
				new ItemStack(spawnEgg)));
	}
}
