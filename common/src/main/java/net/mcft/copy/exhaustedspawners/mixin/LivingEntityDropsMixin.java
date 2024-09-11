package net.mcft.copy.exhaustedspawners.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.mcft.copy.exhaustedspawners.event.SpawnEggDropHandler;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropsMixin {

	@Shadow
	public abstract boolean shouldDropLoot();

	@Redirect(method = "dropAllDeathLoot", at = @At(value = "INVOKE",
	          target = "Lnet/minecraft/world/entity/LivingEntity;shouldDropLoot()Z"))
	public boolean redirectShouldDropLoot(LivingEntity entity, DamageSource source) {
		return shouldDropLoot() && SpawnEggDropHandler.onDropAddDeathLoot((LivingEntity)(Object)this, source);
	}
}
