package knightminer.inspirations.tools;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.core.component.DataComponentExactPredicate;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.common.network.InspirationsNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.LockCode;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ToolsEvents {
  @SubscribeEvent
  public static void lockAndUnlock(RightClickBlock event) {
    if (!Config.enableLock.getAsBoolean()) {
      return;
    }

    // first, ensure we have a valid item to use
    Player player = event.getEntity();
    ItemStack stack = player.getItemInHand(event.getHand());

    boolean isKey = stack.getItem() == InspirationsTools.key;
    boolean isLock = stack.getItem() == InspirationsTools.lock;

    if (!isKey && !isLock) {
      return;
    }
    BlockEntity te = event.getLevel().getBlockEntity(event.getPos());

    if (te instanceof BaseContainerBlockEntity lockable) {

      // a lock opens with an item of the same name since 1.21.2 (the lock is an item predicate)
      LockCode heldCode = new LockCode(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components()
        .exact(DataComponentExactPredicate.expect(DataComponents.CUSTOM_NAME, stack.getHoverName())).build()).build());

      // lock code
      if (isLock) {
        // already locked: display message
        if (lockable.lockKey != LockCode.NO_LOCK) {
          player.displayClientMessage(Component.translatable(Inspirations.prefix("lock.fail.locked")), true);
        } else if (!stack.has(DataComponents.CUSTOM_NAME)) {
          player.displayClientMessage(Component.translatable(Inspirations.prefix("lock.fail.blank")), true);
        } else {
          // lock the container
          lockable.lockKey = heldCode;
          lockable.setChanged();
          if (!player.isCreative()) {
            stack.shrink(1);
          }
          player.displayClientMessage(Component.translatable(Inspirations.prefix("lock.success")), true);
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        // if the player is not sneaking, just open the chest as normal with the key
      } else if (player.isCrouching()) {
        if (lockable.lockKey != LockCode.NO_LOCK) {
          // if the key matches the lock, take off the lock and give it to the player
          if (lockable.lockKey.unlocksWith(stack)) {
            lockable.lockKey = LockCode.NO_LOCK;
            lockable.setChanged();
            ItemStack lock = new ItemStack(InspirationsTools.lock);
            // the key opened it, so the lock had its name
            lock.set(DataComponents.CUSTOM_NAME, stack.get(DataComponents.CUSTOM_NAME));
            player.getInventory().placeItemBackInInventory(lock);
            player.displayClientMessage(Component.translatable(Inspirations.prefix("unlock.success")), true);
          } else {
            player.displayClientMessage(Component.translatable(Inspirations.prefix("unlock.fail.no_match")), true);
          }
        } else {
          player.displayClientMessage(Component.translatable(Inspirations.prefix("unlock.fail.unlocked")), true);
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public static void vineBreakEvent(BreakEvent event) {
    if (!Config.harvestHangingVines.getAsBoolean()) {
      return;
    }

    // stop if on client or already canceled
    if (event.isCanceled()) {
      return;
    }
    if (event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel world)) {
      return;
    }

    // check conditions: must be shearing vines and not creative
    Player player = event.getPlayer();
    if (player.isCreative()) {
      return;
    }
    Block block = event.getState().getBlock();
    if (!(block instanceof VineBlock vine)) {
      return;
    }
    ItemStack shears = player.getMainHandItem();
    // 1.20 returned here when holding shears, so the vines were only pulled down by hand and never with shears
    if (!shears.canPerformAction(ItemAbilities.SHEARS_DIG)) {
      return;
    }

    BlockPos pos = event.getPos().below();
    BlockState state = world.getBlockState(pos);

    // iterate down until we find either a non-vine or the vine can stay
    int count = 0;
    while (state.getBlock() == block && vine.isShearable(player, shears, world, pos) && !vineCanStay(world, state, pos)) {
      count++;
      for (ItemStack stack : Block.getDrops(state, world, pos, null, player, shears)) {
        Block.popResource(world, pos, stack);
      }
      pos = pos.below();
      state = world.getBlockState(pos);
    }
    // break all the vines we dropped as items,
    // mainly for safety even though vines should break it themselves
    for (int i = 0; i < count; i++) {
      pos = pos.above();
      world.removeBlock(pos, false);
    }
  }

  private static boolean vineCanStay(Level world, BlockState state, BlockPos pos) {
    // check if any of the four sides allows the vine to stay
    for (Direction side : Direction.Plane.HORIZONTAL) {
      if (state.getValue(VineBlock.getPropertyForFace(side)) && VineBlock.isAcceptableNeighbour(world, pos.relative(side), side)) {
        return true;
      }
    }

    return false;
  }

  /* TODO: move to global loot tables
  @SubscribeEvent
  public static void dropMelon(HarvestDropsEvent event) {
    if (!Config.shearsReclaimMelons.get() || event.getState().getBlock() != Blocks.MELON) {
      return;
    }

    PlayerEntity player = event.getHarvester();
    if (player == null || player.isCreative()) {
      return;
    }

    ItemStack shears = player.getHeldItemMainhand();
    Item item = shears.getItem();
    if (!(item instanceof ShearsItem || item.getToolTypes(shears).contains(InspirationsRegistry.SHEAR_TYPE))) {
      return;
    }

    // ensure we have 9 melons drop
    List<ItemStack> drops = event.getDrops();
    Iterator<ItemStack> iterator = drops.iterator();
    boolean foundMelon = false;
    while (iterator.hasNext()) {
      ItemStack stack = iterator.next();
      if (stack.getItem() == Items.MELON_SLICE) {
        if (!foundMelon) {
          stack.setCount(9);
          foundMelon = true;
        } else {
          iterator.remove();
        }
      }
    }
  }
   */

  /** Thorns, fire aspect and knockback on a shield act on attackers whose hit the shield blocked */
  @SubscribeEvent(priority = EventPriority.LOWEST)
  static void onShieldHit(LivingShieldBlockEvent event) {
    if (!Config.moreShieldEnchantments.getAsBoolean() || !event.getBlocked()) {
      return;
    }
    LivingEntity target = event.getEntity();
    if (!(target.level() instanceof ServerLevel level) || !target.isBlocking()) {
      return;
    }
    ItemStack stack = target.getUseItem();
    var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    int thorns = stack.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.THORNS));
    int fire = stack.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.FIRE_ASPECT));
    int knockback = stack.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.KNOCKBACK));
    if (thorns == 0 && fire == 0 && knockback == 0) {
      return;
    }

    DamageSource source = event.getDamageSource();
    Entity attacker = source.getDirectEntity();
    // Apply shield enchantments if the player can be hurt by the source,
    // and they are have blocked it.
    if (attacker != null && !target.isInvulnerableTo(level, source)) {
      // the chance and damage of 1.20 ThornsEnchantment, enchantments are data in 1.21
      if (thorns > 0 && level.random.nextFloat() < 0.15F * thorns) {
        attacker.hurt(level.damageSources().thorns(target), thorns > 10 ? thorns - 10 : 1 + level.random.nextInt(4));
        stack.hurtAndBreak(1, target, target.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
      }
      if (fire > 0) {
        attacker.igniteForSeconds(fire * 4);
      }
      if (knockback > 0) {
        if (attacker instanceof LivingEntity) {
          ((LivingEntity)attacker).knockback(knockback * 0.5F, Mth.sin(target.getYRot() * 0.017453292F), -Mth.cos(target.getYRot() * 0.017453292F));
          if (attacker instanceof ServerPlayer) {
            InspirationsNetwork.sendPacket(attacker, new ClientboundSetEntityMotionPacket(attacker));
          }
        } else {
          attacker.push(-Mth.sin(target.getYRot() * 0.017453292F) * knockback * 0.5f, 0.1D, Mth.cos(target.getYRot() * 0.017453292F) * knockback * 0.5f);
        }
      }
    }
  }
}
