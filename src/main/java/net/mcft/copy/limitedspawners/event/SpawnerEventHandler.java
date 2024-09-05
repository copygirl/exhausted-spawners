package net.mcft.copy.limitedspawners.event;

import java.util.Random;

import net.mcft.copy.limitedspawners.Config;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
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
	 * 	Change hardness for Spawner Block.
	 * 
	 * 	It calculates a new break speed for the custom hardness. If the custom hardness
	 * 	is the same as Spawner default it will calculate the same break speed as vanilla.
	 * 
	 * 	@implNote The wiki tick is at 20 but it gave wrong results when new and original
	 * 	hardness was the same. Example: newHardness = 5.0 should give 0.95 seconds for a
	 * 	diamond pick but it gave 3.1 seconds.
	 * 
	 * 	@see https://minecraft.fandom.com/wiki/Breaking 
	 * 
	 * 	@param event
	 */
	@SubscribeEvent
	public void onBreakSpeedEvent(PlayerEvent.BreakSpeed event) {
		if(event.getState().getBlock() instanceof SpawnerBlock) {

			float newHardness = Config.SPAWNER_HARDNESS.get().floatValue();
			float originalHardness = 5.0f;

			// First we calculate how many seconds it will take with new hardness and
			// original break speed. We want to solve for break speed later.
			float dmg = event.getOriginalSpeed() / newHardness;
			dmg /= 100;
			float ticks = Math.round(1 / dmg);
			float seconds = ticks / 64f;

			// now do it reverse and insert original hardness 
			float ticks2 = seconds * 64f;
			float dmg2 = 1 / ticks2;
			dmg2 *= 100;
			int newBreakSpeed = Math.round(dmg2 * originalHardness);

			event.setNewSpeed(newBreakSpeed);
		}
	}

	/**
	 * 	Prevent XP drop when spawner is destroyed with silk touch.
	 * 	The Spawner block is returned via loot tables json
	 */
	@SubscribeEvent
	public void onBlockBreakEvent(BlockEvent.BreakEvent event) {

		// Check if a spawner broke
		if(event.getState().getBlock() instanceof SpawnerBlock) {

			ListTag list = event.getPlayer().getMainHandItem().getEnchantmentTags();

			// Check if we broke the spawner with silk touch and if it's not disabled in config
			if(checkSilkTouch(list) && Config.SPAWNER_SILK_TOUCH.get()) {

				// Set 0 EXP
				event.setExpToDrop(0);
			}	
		}
	}

	/**
	 * 	Check if Spawner block was powered by redstone or not. Used to disable
	 * 	or enable the spawner.
	 */
	@SubscribeEvent
	public void onNotifyEvent(BlockEvent.NeighborNotifyEvent event) {

		Level level = (Level)event.getLevel();

		for(Direction dir : Direction.values()) {
			BlockPos pos = event.getPos().relative(dir);
			if(level.getBlockState(pos).getBlock() instanceof SpawnerBlock) {

				SpawnerBlockEntity spawner = (SpawnerBlockEntity)level.getBlockEntity(pos);
				BaseSpawner logic = spawner.getSpawner();
				CompoundTag nbt = new CompoundTag();

				// Get current spawner config values
				nbt = logic.save(nbt);

				if(level.hasNeighborSignal(pos)) {
					short value = nbt.getShort("RequiredPlayerRange");

					// If spawner got disabled via GUI and then we toggle off by redstone
					// we don't need to do this.
					if(nbt.getShort("SpawnRange") > 4)
						return;

					// Read current range and save it temporary in SpawnRange field
					nbt.putShort("SpawnRange", value);

					// Turn off spawner
					nbt.putShort("RequiredPlayerRange", (short) 0);
				}
				else {
					// Read what the previus range was (before this spawner was set to range = 0)
					short pr = nbt.getShort("SpawnRange");

					// If spawner was activated via GUI before, then we dont need to do this
					if(pr <= 4)
						return;

					// Set the range backt to what it was
					nbt.putShort("RequiredPlayerRange", pr);

					// Set SpawnRange back to default=4
					nbt.putShort("SpawnRange", (short) 4);
				}

				// Update block
				logic.load(level, pos, nbt);
				spawner.setChanged();
				BlockState blockstate = level.getBlockState(pos);
				level.sendBlockUpdated(pos, blockstate, blockstate, 3);
			}
		}
	}   

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
	 * 	Drops the Monster Egg which is inside the spawner when the spawner is harvested.
	 * 	This is only server side.
	 * 
	 * 	@param pos Spawner block position
	 * 	@param world World reference for spawning
	 */
	private void dropMonsterEgg(BlockPos pos, Level level) {
		BlockState blockstate = level.getBlockState(pos);
		SpawnerBlockEntity spawner = (SpawnerBlockEntity)level.getBlockEntity(pos);
		BaseSpawner logic = spawner.getSpawner();

		// Get entity ResourceLocation string from spawner by creating a empty compound which we make our 
		// spawner logic write to. We can then access what type of entity id the spawner has inside
		CompoundTag nbt = new CompoundTag();
		nbt = logic.save(nbt);
		String entity_string = nbt.get("SpawnData").toString();

		// Leave if the spawner does not contain an entity
		if(entity_string.indexOf("\"") == -1)
			return;

		// Strips the string
		// Example: {id: "minecraft:xxx_xx"} --> minecraft:xxx_xx
		entity_string = entity_string.substring(entity_string.indexOf("\"") + 1);
		entity_string = entity_string.substring(0, entity_string.indexOf("\""));

		// Leave if the spawner does not contain an egg	
		if(entity_string.equalsIgnoreCase(EntityType.AREA_EFFECT_CLOUD.toString()))
			return;

		// Get the entity mob egg and put in an ItemStack
		ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity_string + "_spawn_egg")));

		// Get random fly-out position offsets
		double d0 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;
		double d1 = (double)(level.random.nextFloat() * 0.7F) + (double)0.06F + 0.6D;
		double d2 = (double)(level.random.nextFloat() * 0.7F) + (double)0.15F;

		// Create entity item
		ItemEntity entityItem = new ItemEntity(
				level, 
				(double)pos.getX() + d0, 
				(double)pos.getY() + d1, 
				(double)pos.getZ() + d2, 
				itemStack);
		entityItem.setDefaultPickUpDelay();

		// Spawn entity item (egg)
		level.addFreshEntity(entityItem);

		// Replace the entity inside the spawner with default entity
		logic.setEntityId(EntityType.AREA_EFFECT_CLOUD, level, level.random, pos);
		spawner.setChanged();
		level.sendBlockUpdated(pos, blockstate, blockstate, 3);
	}

	/**
	 * 	Check a tools item enchantment list contains Silk Touch enchant
	 * 	I don't know if there's a better way to do this	
	 * 
	 * 	@param NBTTagList of enchantment
	 * 	@return true/false
	 */
	private boolean checkSilkTouch(ListTag list) {
		if(list.getAsString().indexOf("minecraft:silk_touch") != -1)
			return true;
		else
			return false;
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
