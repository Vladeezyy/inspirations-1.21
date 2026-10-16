package knightminer.inspirations.tweaks.block;

import knightminer.inspirations.common.Config;
import knightminer.inspirations.tweaks.InspirationsTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Logic of the waterlogged hopper. In 1.20 the vanilla hopper was replaced by DryHopperBlock; NeoForge 1.21 cannot
 * replace vanilla blocks, so HopperBlockMixin calls this for the vanilla hopper and WetHopperBlock is the wet one.
 * The hopper keeps its block entity when switching, so items are not dropped.
 */
public final class HopperWaterlogging {
  private HopperWaterlogging() {}

  /** True if the feature is on */
  public static boolean enabled() {
    return Config.waterlogHopper.getAsBoolean();
  }

  /** Copies facing and enabled to the other hopper block */
  public static BlockState swap(BlockState state, Block block) {
    return block.defaultBlockState()
                .setValue(HopperBlock.FACING, state.getValue(HopperBlock.FACING))
                .setValue(HopperBlock.ENABLED, state.getValue(HopperBlock.ENABLED));
  }

  /** The dry hopper takes water */
  public static boolean canPlaceLiquid(Fluid fluid) {
    return enabled() && fluid == Fluids.WATER;
  }

  /** Puts water into a dry hopper: becomes a wet hopper */
  public static boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluid) {
    if (!canPlaceLiquid(fluid.getType())) {
      return false;
    }
    if (!world.isClientSide()) {
      world.setBlock(pos, swap(state, InspirationsTweaks.wetHopper), 3);
      world.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(world));
    }
    return true;
  }

  /** State to place: wet in water */
  public static BlockState forPlacement(BlockState state, FluidState fluid) {
    if (enabled() && state.getBlock() instanceof HopperBlock && fluid.getType() == Fluids.WATER) {
      return swap(state, InspirationsTweaks.wetHopper);
    }
    return state;
  }

  /** True if one hopper block becomes the other, the block entity stays */
  public static boolean isSwap(BlockState state, BlockState newState) {
    Block old = state.getBlock();
    Block next = newState.getBlock();
    return old != next && (old == InspirationsTweaks.wetHopper || old == InspirationsTweaks.dryHopper)
           && (next == InspirationsTweaks.wetHopper || next == InspirationsTweaks.dryHopper);
  }
}
