package net.mcft.copy.exhaustedspawners.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.mcft.copy.exhaustedspawners.Config;
import net.mcft.copy.exhaustedspawners.ExhaustedSpawners;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(SpawnerBlock.class)
public abstract class SpawnerHarvestMixin extends Block {
	private SpawnerHarvestMixin(Properties properties) { super(properties); }

	public void playerDestroy(
			Level level, Player player, BlockPos pos, BlockState state,
			@Nullable BlockEntity blockEntity, ItemStack held) {
		super.playerDestroy(level, player, pos, state, blockEntity, held);
		if (!Config.SPAWNER_SILK_TOUCH.get()) return;

		// Drop the spawner block when the correct tool with silk touch is used.
		if (player.hasCorrectToolForDrops(state) && (held.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0))
			popResource(level, pos, new ItemStack((Block)(Object)this));
		// Otherwise, just drop the regular amount of experience.
		else popExperience((ServerLevel)level, pos, 15 + level.random.nextInt(15) + level.random.nextInt(15));
	}

	@Inject(remap = false, method = "getExpDrop", at = @At("HEAD"), cancellable = true)
	private void beforeGetExpDrop(
			BlockState state, LevelReader world, RandomSource random, BlockPos pos,
			int fortune, int silktouch, CallbackInfoReturnable<Integer> cir) {
		// Don't drop any experience if the silk touch config is on as it'll be handled above.
		if (Config.SPAWNER_SILK_TOUCH.get()) cir.setReturnValue(0);
	}
}
