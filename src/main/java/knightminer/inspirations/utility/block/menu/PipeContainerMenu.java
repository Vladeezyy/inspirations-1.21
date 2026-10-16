package knightminer.inspirations.utility.block.menu;

import knightminer.inspirations.utility.InspirationsUtility;
import knightminer.inspirations.utility.block.entity.PipeBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import dev.vladeezyy.kilnwork.inventory.BaseContainerMenu;

import javax.annotation.Nullable;

public class PipeContainerMenu extends BaseContainerMenu<PipeBlockEntity> {
  public PipeContainerMenu(int winId, Inventory inventoryPlayer, @Nullable PipeBlockEntity tile) {
    super(InspirationsUtility.contPipe, winId, inventoryPlayer, tile);
    // a client that could not find the block entity still needs the slot, or the server's slot updates break
    this.addSlot(new Slot(tile != null ? tile : new SimpleContainer(1), 0, 80, 20));
    addInventorySlots();
  }

  public PipeContainerMenu(int windowId, Inventory inv, FriendlyByteBuf data) {
    this(windowId, inv, getTileEntityFromBuf(data, PipeBlockEntity.class));
  }

  @Override
  protected int getInventoryXOffset() {
    return 8;
  }

  @Override
  protected int getInventoryYOffset() {
    return 51;
  }
}
