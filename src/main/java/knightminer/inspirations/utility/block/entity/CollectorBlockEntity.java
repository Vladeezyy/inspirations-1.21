package knightminer.inspirations.utility.block.entity;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import knightminer.inspirations.utility.InspirationsUtility;
import knightminer.inspirations.utility.block.menu.CollectorContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;

import dev.vladeezyy.kilnwork.block.InventoryBlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

public class CollectorBlockEntity extends InventoryBlockEntity {
  private static final Component TITLE = Component.translatable("gui.inspirations.collector");

  /** Item handler of the block we are facing, created once per facing (server only) */
  @Nullable
  private BlockCapabilityCache<ResourceHandler<ItemResource>, Direction> facingHandler;
  /** Cache of the bounds to check for items */
  @Nullable
  private AABB itemBounds;

  public CollectorBlockEntity(BlockPos pos, BlockState state) {
    super(InspirationsUtility.tileCollector, pos, state, TITLE, 9);
  }

  /**
   * Called on redstone pulse to collect items from the world
   */
  public void collect() {
    if (level == null) {
      return;
    }

    // if there is an item handler, extract the items
    ResourceHandler<ItemResource> handler = getHandler();
    if (handler != null) {
      extractItem(handler);
    } else {
      // collect items from world
      boolean collected = false;
      for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, getItemBounds(), EntitySelector.ENTITY_STILL_ALIVE)) {
        ItemStack insert = entity.getItem();
        // no need to simulate, if successful we have to modify the stack regardless
        // insert a copy: a stack that fits whole would otherwise be shared with the item entity (issue #227)
        int inserted = insert.isEmpty() ? 0 : ResourceHandlerUtil.insertStacking(itemHandler, ItemResource.of(insert), insert.getCount(), null);
        ItemStack remainder = insert.copyWithCount(insert.getCount() - inserted);
        // if the stack changed, we were successful
        if (remainder.getCount() < insert.getCount()) {
          collected = true;
          // empty means item is gone
          if (remainder.isEmpty()) {
            entity.discard();
          } else {
            entity.setItem(remainder);
          }
        }
      }
      // play sound. Plays dispenser dispense if success and dispenser fail if not
      level.levelEvent(collected ? LevelEvent.SOUND_DISPENSER_DISPENSE : LevelEvent.SOUND_DISPENSER_FAIL, worldPosition, 0);
    }
  }

  /**
   * Gets the item handler this collector is facing
   * @return  Item handler
   */
  @Nullable
  private ResourceHandler<ItemResource> getHandler() {
    if (!(level instanceof ServerLevel serverLevel)) {
      return null;
    }
    if (facingHandler == null) {
      // the cache follows the neighbor by itself, so it is made once per facing
      Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
      facingHandler = BlockCapabilityCache.create(Capabilities.Item.BLOCK, serverLevel, worldPosition.relative(facing), facing.getOpposite(), () -> !isRemoved(), () -> {});
    }
    return facingHandler.getCapability();
  }

  /**
   * Gets the bounds for grabbing items
   * @return  Item bounds
   */
  private AABB getItemBounds() {
    if (itemBounds == null) {
      BlockPos offset = worldPosition.relative(getBlockState().getValue(BlockStateProperties.FACING));
      itemBounds = new AABB(offset.getX(), offset.getY(), offset.getZ(), offset.getX() + 1, offset.getY() + 1, offset.getZ() + 1);
    }
    return itemBounds;
  }

  /**
   * Logic to transfer a single item
   * @param neighbor  Neighbor to extract items from
   */
  private void extractItem(ResourceHandler<ItemResource> neighbor) {
    // the first single item that fits moves over (one transaction for both sides)
    ResourceHandlerUtil.moveFirstStacking(neighbor, itemHandler, resource -> true, 1, null);
  }

  @Override
  public void setBlockState(BlockState state) {
    Direction old = getBlockState().getValue(BlockStateProperties.FACING);
    super.setBlockState(state);
    // rotated: the cache and the item area face somewhere else now
    if (state.getValue(BlockStateProperties.FACING) != old) {
      this.facingHandler = null;
      this.itemBounds = null;
    }
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack itemstack) {
    // mantle checks stack size which breaks some things when using stacks bigger than 1
    return slot < getContainerSize();
  }

  /*
   * GUI
   */

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int winId, Inventory playerInv, Player player) {
    return new CollectorContainerMenu(winId, playerInv, this);
  }
}
