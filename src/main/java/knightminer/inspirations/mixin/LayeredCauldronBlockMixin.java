package knightminer.inspirations.mixin;

import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.cauldrons.block.BoilingFourLayerCauldronBlock;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The vanilla water cauldron boils over fire: bubbles and damage to entities inside (config cauldrons.replaceWaterCauldron).
 * 1.20 replaced the water cauldron with BoilingThreeLayerCauldronBlock.
 */
@Mixin(LayeredCauldronBlock.class)
public abstract class LayeredCauldronBlockMixin extends AbstractCauldronBlock {
  private LayeredCauldronBlockMixin(Properties properties) {
    super(properties, null);
  }

  /** True for the vanilla water cauldron with the feature on, other layered cauldrons have their own logic */
  private boolean inspirations$boils() {
    return (Object) this == Blocks.WATER_CAULDRON && Config.replaceVanillaCauldrons.getAsBoolean();
  }

  @Inject(method = "entityInside", at = @At("HEAD"))
  private void inspirations$boil(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects, CallbackInfo ci) {
    if (!level.isClientSide() && inspirations$boils() && BoilingFourLayerCauldronBlock.isBoiling(level, pos)) {
      BoilingFourLayerCauldronBlock.boil(entity);
    }
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
    if (inspirations$boils() && BoilingFourLayerCauldronBlock.isBoiling(level, pos)) {
      MiscUtil.addParticles(InspirationsCaudrons.boilingParticle, level, pos, 2, getContentHeight(state), rand);
    }
  }
}
