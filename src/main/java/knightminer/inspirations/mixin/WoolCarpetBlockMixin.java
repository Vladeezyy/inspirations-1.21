package knightminer.inspirations.mixin;

import knightminer.inspirations.tweaks.block.FlatCarpetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

/**
 * Vanilla carpets fit to stairs below them (config tweaks.fittedCarpets). 1.20 replaced the carpet blocks with
 * FlatCarpetBlock; here the vanilla class gets its two methods.
 */
@Mixin(WoolCarpetBlock.class)
public abstract class WoolCarpetBlockMixin extends CarpetBlock {
  private WoolCarpetBlockMixin(Properties properties) {
    super(properties);
  }

  @Shadow
  public abstract DyeColor getColor();

  @Override
  protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
    return FlatCarpetBlock.fitToStairs(getColor(), state, world, pos);
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
    BlockState fitted = FlatCarpetBlock.fitToStairs(getColor(), state, world, pos);
    if (fitted != state) {
      world.setBlock(pos, fitted, 2);
    }
  }
}
