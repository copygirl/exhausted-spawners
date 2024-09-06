package net.mcft.copy.exhaustedspawners.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import net.mcft.copy.exhaustedspawners.Config;
import net.minecraft.tags.TagKey;
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

	/** Clears any items from drops that weren't part of the entity's equipment. */
	private void clearNonEquipment(Collection<ItemEntity> drops) {
		drops.removeIf(itemEntity -> !capturedEquipment.contains(itemEntity.getItem()));
	}

	@SubscribeEvent
	public void onLivingDropsEvent(LivingDropsEvent event) {
		var killer = event.getSource().getEntity();
		var causedByPlayer = killer instanceof Player;
		if (Config.PLAYER_KILL_REQUIRED.get() && !causedByPlayer) return;

		// TODO: Technically incorrect, but there might not be a way to get the actual item used for the kill.
		var weapon    = (killer instanceof LivingEntity) ? ((LivingEntity)killer).getMainHandItem() : ItemStack.EMPTY;
		var silkTouch = weapon.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0;
		var looting   = event.getLootingLevel(); // Has other looting bonuses applied.

		var chance = silkTouch
			? Config.EGG_DROP_CHANCE_SILK_TOUCH.get()
			: Config.EGG_DROP_CHANCE.get() + looting * Config.EGG_DROP_LOOTING_BONUS.get();

		// If a silk touch item is used, and drops should be cleared when using silk touch, do that.
		if (silkTouch && Config.CLEAR_DROPS_ON_SILK_TOUCH.get()) clearNonEquipment(event.getDrops());

		var entity     = event.getEntity();
		var entityType = entity.getType();
		var entityId   = EntityType.getKey(entityType);

		// Babies don't drop spawn eggs without silk touch.
		if (!silkTouch && entity.isBaby()) return;

		// Check if RNGesus is with us this day.
		if (entity.getRandom().nextFloat() >= chance) return;

		// If entity id is contained in blacklist, don't drop anything.
		var blacklist = Config.EGG_DROP_BLACKLIST.get();
		if (blacklist.contains(entityId.toString())) return;

		// If any of the entity's type's tags are contained in the blacklist, don't drop anything.
		var blacklistTags = blacklist.stream().filter(i -> i.startsWith("#")).map(i -> i.substring(1)).toList();
		var entityTags    = entityType.getTags().map(TagKey::location).map(Object::toString);
		if (entityTags.anyMatch(blacklistTags::contains)) return;

		var spawnEgg = ForgeSpawnEggItem.fromEntityType(entityType);
		if (spawnEgg == null) return; // No spawn egg found.

		// If drops should be cleared when a spawn egg is dropped, do that.
		if (Config.CLEAR_DROPS_ON_EGG.get()) clearNonEquipment(event.getDrops());

		// Add spawn egg to the drops where the entity died.
		event.getDrops().add(new ItemEntity(entity.level(),
				entity.getX(), entity.getY(), entity.getZ(),
				new ItemStack(spawnEgg)));
	}
}
