package knightminer.inspirations.tweaks.block;

import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.Blocks;
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
  public boolean canPlaceLiquid(@Nullable LivingEntity player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
    // No new fluid can be inserted.
    return false;
  }

  @Override
  public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluid) {
    return false;
  }

  @Override
  public ItemStack pickupBlock(@Nullable LivingEntity player, LevelAccessor world, BlockPos pos, BlockState state) {
    world.setBlock(pos, HopperWaterlogging.swap(state, InspirationsTweaks.dryHopper), 3);
    return new ItemStack(Items.WATER_BUCKET);
  }

  @Override
  public Optional<SoundEvent> getPickupSound() {
    return Optional.of(SoundEvents.BUCKET_FILL);
  }

  @Override
  public boolean shouldChangedStateKeepBlockEntity(BlockState old) {
    // turning wet keeps the block entity and its items
    return HopperWaterlogging.isSwap(old, defaultBlockState()) || super.shouldChangedStateKeepBlockEntity(old);
  }

  @Override
  protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
    return new ItemStack(Items.HOPPER);
  }
}
