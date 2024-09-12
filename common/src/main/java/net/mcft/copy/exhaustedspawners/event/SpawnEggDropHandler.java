package net.mcft.copy.exhaustedspawners.event;

import java.util.Optional;
import java.util.Map.Entry;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.SpawnEggItem;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.Config.DropListBehavior;

public final class SpawnEggDropHandler {
	private SpawnEggDropHandler() {  }

	/** Called when a LivingEntity dies and is supposed to drop its loot.
	 *  Returns whether the default loot should be dropped by the entity. */
	public static boolean onDropAddDeathLoot(LivingEntity entity, DamageSource source) {
		var killer = source.getEntity();
		var causedByPlayer = killer instanceof Player;
		if (Config.PLAYER_KILL_REQUIRED.get() && !causedByPlayer) return true;

		var silkTouch = false;
		var looting   = 0;
		if (killer instanceof LivingEntity) {
			var weapon = ((LivingEntity)killer).getMainHandItem();
			silkTouch  = EnchantmentHelper.hasSilkTouch(weapon);
			looting    = EnchantmentHelper.getMobLooting((LivingEntity)killer);
		}

		// If using silk touch and drops are cleared on silk touch, do not drop default loot.
		var dropDefaultLoot = !(silkTouch && Config.CLEAR_DROPS_ON_SILK_TOUCH.get());

		// Babies don't drop spawn eggs without silk touch.
		if (!silkTouch && entity.isBaby()) return dropDefaultLoot;

		// Only slimes that are tiny and wouldn't spawn more slimes can drop spawn eggs.
		if ((entity instanceof Slime) && !((Slime)entity).isTiny()) return dropDefaultLoot;

		var entityType = entity.getType();
		var eggDropChance = silkTouch
			? Config.DROP_CHANCE_SILK_TOUCH.get()
			: Config.DROP_CHANCE.get() + looting * Config.DROP_LOOTING_BONUS.get();

		// Multiply drop chance depending on 'drop_list' entry (if any).
		eggDropChance *= lookupDropChanceMultiplier(entityType);

		// Check if RNGesus is with us this day.
		if (entity.getRandom().nextFloat() >= eggDropChance) return dropDefaultLoot;

		// Drop spawn egg where the entity died.
		Optional.of(entityType).map(SpawnEggItem::byId).ifPresent(entity::spawnAtLocation);

		// If drops are cleared when an egg is dropped, do not drop default loot.
		return dropDefaultLoot && !Config.CLEAR_DROPS_ON_EGG.get();
	}

	private static double lookupDropChanceMultiplier(EntityType<?> entityType) {
		var entityId = EntityType.getKey(entityType);

		var id_lookup = Config.CACHED_DROP_LIST_IDS.get(entityId);
		if (id_lookup != null) return id_lookup;

		var tag_lookup = Config.CACHED_DROP_LIST_TAGS.entrySet().stream()
			.filter(entry -> entityType.is(entry.getKey()))
			.map(Entry::getValue).findFirst();
		if (tag_lookup.isPresent()) return tag_lookup.get();

		// Not found in 'drop_list', use the default depending on 'drop_list_behavior'.
		return (Config.DROP_LIST_BEHAVIOR.get() == DropListBehavior.ALLOW) ? 0.0 : 1.0;
	}
}
