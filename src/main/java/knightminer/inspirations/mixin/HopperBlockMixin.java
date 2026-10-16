package knightminer.inspirations.mixin;

import knightminer.inspirations.tweaks.block.HopperWaterlogging;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * The vanilla hopper takes water and becomes inspirations:wet_hopper (config tweaks.waterlogHoppers). 1.20 replaced
 * the hopper with DryHopperBlock; the hopper has no waterlogged property, so its block states stay vanilla.
 */
@Mixin(HopperBlock.class)
public abstract class HopperBlockMixin implements SimpleWaterloggedBlock {
  @Override
  public boolean canPlaceLiquid(@Nullable Player player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
    return HopperWaterlogging.canPlaceLiquid(fluid);
  }

  @Override
  public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluid) {
    return HopperWaterlogging.placeLiquid(world, pos, state, fluid);
  }

  @Override
  public ItemStack pickupBlock(@Nullable Player player, LevelAccessor world, BlockPos pos, BlockState state) {
    // a dry hopper has no water to give
    return ItemStack.EMPTY;
  }

  @Override
  public Optional<SoundEvent> getPickupSound() {
    return Optional.empty();
  }

  @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
  private void inspirations$wetInWater(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
    BlockState state = cir.getReturnValue();
    if (state != null) {
      cir.setReturnValue(HopperWaterlogging.forPlacement(state, context.getLevel().getFluidState(context.getClickedPos())));
    }
  }

  @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
  private void inspirations$keepContentsOnSwap(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
    // turning wet keeps the block entity and its items
    if (HopperWaterlogging.isSwap(state, newState)) {
      ci.cancel();
    }
  }
}
