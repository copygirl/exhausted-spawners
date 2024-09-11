package net.mcft.copy.exhaustedspawners.mixin;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.mcft.copy.exhaustedspawners.Config;

@Mixin(SpawnerBlock.class)
@ParametersAreNonnullByDefault
public abstract class SpawnerBlockMixin extends BaseEntityBlock {
	protected SpawnerBlockMixin(Properties properties) { super(properties); }

	@SuppressWarnings("deprecation")
	public float getDestroyProgress(BlockState state, Player player, BlockGetter getter, BlockPos pos) {
		var result = super.getDestroyProgress(state, player, getter, pos);
		return result * Config.SPAWNER_BREAK_SPEED.get().floatValue();
	}

	// Experience drops are handled in SpawnerHarvestHandler::onBlockBreakEvent.
	@ModifyVariable(method = "spawnAfterBreak", at = @At("STORE"))
	public int preventExperienceDrops(int original) { return 0; }
}
