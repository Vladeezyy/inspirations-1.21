package knightminer.inspirations.tweaks.block;

import knightminer.inspirations.tweaks.InspirationsTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.Optional;

/** Hopper full of water, without a waterlogged property. Turns back into the vanilla hopper when the water is taken. */
public class WetHopperBlock extends HopperBlock implements SimpleWaterloggedBlock {
  public WetHopperBlock(Properties props) {
    super(props);
  }

  @Override
  protected FluidState getFluidState(BlockState state) {
    return Fluids.WATER.getSource(false);
  }

  // Duplicate SimpleWaterloggedBlock's code, but don't use the property.
  @Override
  public boolean canPlaceLiquid(@Nullable Player player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
    // No new fluid can be inserted.
    return false;
  }

  @Override
  public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluid) {
    return false;
  }

  @Override
  public ItemStack pickupBlock(@Nullable Player player, LevelAccessor world, BlockPos pos, BlockState state) {
    world.setBlock(pos, HopperWaterlogging.swap(state, InspirationsTweaks.dryHopper), 3);
    return new ItemStack(Items.WATER_BUCKET);
  }

  @Override
  public Optional<SoundEvent> getPickupSound() {
    return Optional.of(SoundEvents.BUCKET_FILL);
  }

  @Override
  protected void onRemove(BlockState state, net.minecraft.world.level.Level world, BlockPos pos, BlockState newState, boolean moving) {
    // drying keeps the block entity and its items
    if (!HopperWaterlogging.isSwap(state, newState)) {
      super.onRemove(state, world, pos, newState, moving);
    }
  }

  @Override
  public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
    return new ItemStack(Items.HOPPER);
  }
}
