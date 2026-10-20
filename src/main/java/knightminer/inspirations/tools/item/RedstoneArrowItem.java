package knightminer.inspirations.tools.item;

import knightminer.inspirations.tools.entity.RedstoneArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import javax.annotation.Nullable;

public class RedstoneArrowItem extends ArrowItem {
  public RedstoneArrowItem(Properties builder) {
    super(builder);
  }

  @Override
  public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack weapon) {
    return new RedstoneArrow(world, shooter, stack.copyWithCount(1), weapon);
  }

  @Override
  public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
    RedstoneArrow arrow = new RedstoneArrow(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null);
    arrow.pickup = AbstractArrow.Pickup.ALLOWED;
    return arrow;
  }

  @Override
  public boolean isInfinite(ItemStack stack, ItemStack bow, LivingEntity player) {
    return false;
  }
}
