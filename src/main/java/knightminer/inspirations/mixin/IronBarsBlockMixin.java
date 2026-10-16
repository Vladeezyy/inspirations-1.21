package knightminer.inspirations.mixin;

import knightminer.inspirations.building.block.RopeBlock;
import knightminer.inspirations.common.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Iron bars can be climbed when a rope hangs below them (config building.climbableBars). 1.20 replaced the block with
 * ClimbablePaneBlock; glass panes share the class, so only the iron bars block itself is changed.
 */
@Mixin(IronBarsBlock.class)
public abstract class IronBarsBlockMixin {
  public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity) {
    if (state.is(Blocks.IRON_BARS) && Config.climbableIronBars.get() && world.getBlockState(pos.below()).getBlock() instanceof RopeBlock) {
      return true;
    }
    // NeoForge's default
    return state.is(BlockTags.CLIMBABLE);
  }
}
