package knightminer.inspirations.tools.entity;

import knightminer.inspirations.tools.InspirationsTools;
import knightminer.inspirations.tools.block.RedstoneChargeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import static knightminer.inspirations.tools.InspirationsTools.redstoneCharge;

public class RedstoneArrow extends AbstractArrow {
  public RedstoneArrow(EntityType<RedstoneArrow> entType, Level world) {
    super(entType, world);
  }

  public RedstoneArrow(Level world, double x, double y, double z, ItemStack pickup, @Nullable ItemStack weapon) {
    super(InspirationsTools.entRSArrow, x, y, z, world, pickup, weapon);
    init();
  }

  public RedstoneArrow(Level world, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weapon) {
    super(InspirationsTools.entRSArrow, shooter, world, pickup, weapon);
    init();
  }

  private void init() {
    this.setBaseDamage(0.25);
  }

  private static final Component NAME = Component.translatable("item.inspirations.charged_arrow");

  @Override
  public Component getName() {
    if (this.hasCustomName()) {
      return super.getName();
    } else {
      return NAME;
    }
  }

  @Override
  protected ItemStack getDefaultPickupItem() {
    return new ItemStack(InspirationsTools.redstoneArrow, 1);
  }

  /**
   * Called when the arrow hits a block or an entity
   */
  @Override
  protected void onHitBlock(BlockHitResult raytrace) {
    // get to the block the arrow is on
    Direction sideHit = raytrace.getDirection();
    BlockPos pos = raytrace.getBlockPos().relative(sideHit);

    // if there is a block there, try the block next to that
    Level level = level();
    if (!level.getBlockState(pos).canBeReplaced(new DirectionalPlaceContext(level, pos, sideHit, ItemStack.EMPTY, sideHit))) {
      pos = pos.relative(sideHit);
      if (!level.getBlockState(pos).canBeReplaced(new DirectionalPlaceContext(level, pos, sideHit, ItemStack.EMPTY, sideHit))) {
        super.onHitBlock(raytrace);
        return;
      }
    }

    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
    BlockState state = redstoneCharge.defaultBlockState().setValue(RedstoneChargeBlock.FACING, sideHit.getOpposite());
    level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
    redstoneCharge.setPlacedBy(level, pos, state, null, ItemStack.EMPTY);

    this.discard();
  }
}
