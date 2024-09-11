package net.mcft.copy.exhaustedspawners.mixin;

import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.mcft.copy.exhaustedspawners.platform.IGetSpawnerBlockEntity;

@Mixin(targets = "net.minecraft.world.level.block.entity.SpawnerBlockEntity$1")
public abstract class SpawnerBlockEntityMixin implements IGetSpawnerBlockEntity {

	@Final
	@Shadow(aliases = "field_27219")
	private SpawnerBlockEntity field_27219;

	@Override
	public SpawnerBlockEntity getSpawnerBlockEntity() {
		return field_27219;
	}
}
