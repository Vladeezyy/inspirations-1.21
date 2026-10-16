package knightminer.inspirations.utility.block.entity;

import knightminer.inspirations.common.Config;
import knightminer.inspirations.utility.InspirationsUtility;
import knightminer.inspirations.utility.block.PipeBlock;
import knightminer.inspirations.utility.block.menu.PipeContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import dev.vladeezyy.kilnwork.block.InventoryBlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.HolderLookup;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class PipeBlockEntity extends InventoryBlockEntity {
  /** Server tick logic */
  public static final BlockEntityTicker<PipeBlockEntity> SERVER_TICKER = (level, pos, state, te) -> te.tick();
  private static final Component TITLE = Component.translatable("gui.inspirations.pipe");

  /* Number of ticks before transfer is allowed again */
  private short cooldown = 0;

  /** Item handler of the block we output to, created once per facing (server only) */
  @Nullable
  private BlockCapabilityCache<IItemHandler, Direction> facingHandler = null;

  public PipeBlockEntity(BlockPos pos, BlockState state) {
    super(InspirationsUtility.tilePipe, pos, state, TITLE, 1);
  }

  private void tick() {
    // do not function if facing up when disallowed
    Direction facing = this.getBlockState().getValue(PipeBlock.FACING);
    if (!Config.get(Config.pipeUpwards) && facing == Direction.UP) {
      return;
    }

    // process cooldown
    cooldown--;
    if (cooldown > 0) {
      return;
    }
    cooldown = 0;

    // no items, skip
    if (this.isEmpty()) {
      return;
    }

    // transfer items if we have a handler
    IItemHandler handler = getHandler(facing);
    if (handler != null) {
      transferItem(handler, facing);
    }
  }

  /**
   * Gets the item handler in the given direction
   * @param facing  Direction
   * @return  Item handler
   */
  @Nullable
  private IItemHandler getHandler(Direction facing) {
    if (!(level instanceof ServerLevel serverLevel)) {
      return null;
    }
    // the cache follows the neighbor by itself, so it is made once per facing
    if (facingHandler == null) {
      facingHandler = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, serverLevel, worldPosition.relative(facing), facing.getOpposite(), () -> !isRemoved(), () -> {});
    }
    return facingHandler.getCapability();
  }

  /**
   * Logic to transfer a single item
   * @param neighbor  Neighbor to transfer items into
   */
  private void transferItem(IItemHandler neighbor, Direction facing) {
    ItemStack stack = getItem(0);
    if (stack.isEmpty()) {
      return;
    }

    ItemStack copy = stack.copy();
    copy.setCount(1);
    // if we successfully place it in, shrink it here
    if (ItemHandlerHelper.insertItemStacked(neighbor, copy, false).isEmpty()) {
      // set cooldown on the hopper
      if (level != null && level.getBlockEntity(worldPosition.relative(facing)) instanceof HopperBlockEntity hop) {
        hop.setCooldown(8);
      }

      // remove the stack if empty
      stack.shrink(1);
      if (stack.isEmpty()) {
        this.setItem(0, ItemStack.EMPTY);
      }
      cooldown = 8;

      this.setChanged();
    }
  }

  @Override
  public void setBlockState(BlockState state) {
    Direction old = getBlockState().getValue(PipeBlock.FACING);
    super.setBlockState(state);
    // rotated: the output is somewhere else now
    if (state.getValue(PipeBlock.FACING) != old) {
      this.facingHandler = null;
    }
  }

  @Override
  public void setItem(int slot, ItemStack itemstack) {
    super.setItem(slot, itemstack);
    cooldown = 7; // set the cooldown to prevent instant retransfer
  }


  /* GUI */

  @Override
  public AbstractContainerMenu createMenu(int winId, Inventory inv, Player entity) {
    return new PipeContainerMenu(winId, inv, this);
  }


  /* NBT */

  private static final String TAG_COOLDOWN = "cooldown";

  @Override
  protected void loadAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.loadAdditional(tags, registries);
    this.cooldown = tags.getShort(TAG_COOLDOWN);
  }

  @Override
  protected void saveAdditional(CompoundTag tags, HolderLookup.Provider registries) {
    super.saveAdditional(tags, registries);
    tags.putShort(TAG_COOLDOWN, this.cooldown);
  }
}
