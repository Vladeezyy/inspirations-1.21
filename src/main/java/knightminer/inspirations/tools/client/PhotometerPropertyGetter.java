package knightminer.inspirations.tools.client;

import net.minecraft.world.entity.ItemOwner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import knightminer.inspirations.common.client.ItemColorSources;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public class PhotometerPropertyGetter implements ItemColorSources.ItemProperty {
  @Override
  public float get(ItemStack stack, @Nullable ClientLevel clientWorld, @Nullable ItemOwner owner, int seed) {
    LivingEntity entityIn = owner == null ? null : owner.asLivingEntity();
    Entity entity = ItemColorSources.entityOf(owner);
    Level world = clientWorld;
    if (entity == null) {
      return 0;
    }
    if (world == null) {
      world = entity.level();
    }

    // if currently holding the item, use the block the player is looking at
    BlockPos pos = null;
    if (entity == Minecraft.getInstance().player) {
      Player player = Minecraft.getInstance().player;
      if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
        HitResult trace = Minecraft.getInstance().hitResult;
        if (trace != null && trace.getType() == HitResult.Type.BLOCK) {
          pos = ((BlockHitResult)trace).getBlockPos();
          if (world.getBlockState(pos).isSolidRender()) {
            pos = pos.relative(((BlockHitResult)trace).getDirection());
          }
        }
      }
    }
    // if any part failed, just use the entity position
    if (pos == null) {
      pos = entity.blockPosition();
    }

    // only use block light, skylight is not too useful
    return world.getBrightness(LightLayer.BLOCK, pos);
  }
}
