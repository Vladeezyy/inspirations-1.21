package knightminer.inspirations.building.block.entity;

import net.minecraft.util.Prediction;
import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.building.block.ShelfBlock;
import knightminer.inspirations.building.block.menu.ShelfContainerMenu;
import knightminer.inspirations.common.network.InspirationsNetwork;
import knightminer.inspirations.common.network.InventorySlotSyncPacket;
import knightminer.inspirations.library.InspirationsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;


import dev.vladeezyy.kilnwork.retexture.IRetexturedBlockEntity;
import dev.vladeezyy.kilnwork.block.NameableBlockEntity;
import dev.vladeezyy.kilnwork.retexture.RetexturedHelper;
import dev.vladeezyy.kilnwork.retexture.RetexturedBlockEntity;
import dev.vladeezyy.kilnwork.KilnworkRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShelfBlockEntity extends NameableBlockEntity implements IRetexturedBlockEntity {
  private static final String TAG_TEXTURE = "texture";

  public static final ModelProperty<Integer> BOOKS = new ModelProperty<>();
  private static final Component TITLE = Component.translatable("gui.inspirations.shelf.name");

  /**
   * Cached enchantment bonus, so we are not constantly digging the inventory
   */
  private float enchantBonus = Float.NaN;

  // inventory
  private final ShelfInventory inventory = new ShelfInventory(this);

  // display
  private Block texture = Blocks.AIR;

  private int books = 0;

  public ShelfBlockEntity(BlockPos pos, BlockState state) {
    super(InspirationsBuilding.shelfTileEntity, pos, state, TITLE);
  }

  /**
   * Determines which index to modify based on the hit vector and held item
   * @param held   Currently held item
   * @param click  Click vector
   * @return  Index of book clicked, or -1 if the item cannot be placed
   */
  private int getIndexFromHit(ItemStack held, Vec3 click) {
    Direction dir = getBlockState().getValue(ShelfBlock.FACING).getCounterClockWise();
    // location clicked on the block, 0 to 1
    double clicked = (dir.getStepX() * (click.x - 0.5)) + (dir.getStepZ() * (click.z - 0.5)) + 0.5;
    // pixel clicked, 0 to 15
    int pixel = Mth.clamp((int)(clicked * 16), 0, 15);
    // shelf index clicked, 0 to 7
    int shelfIndex = pixel / 2;
    // index for the whole shelf
    int slotIndex = (click.y <= 0.4375 ? 8 : 0) + shelfIndex;
    // if there is an item at the current index, we will remove it
    if (!inventory.getStackInSlot(slotIndex).isEmpty()) {
      return slotIndex;
    }
    // item at the previous index means remove that (assuming its a book)
    boolean previousEmpty = false;
    if (shelfIndex != 0) {
      ItemStack previous = inventory.getStackInSlot(slotIndex - 1);
      if (!previous.isEmpty() && !InspirationsRegistry.isBook(previous)) {
        return slotIndex - 1;
      }
      previousEmpty = previous.isEmpty();
    }
    // at this point we are adding an item, find the proper index to add
    if (held.isEmpty()) {
      return -1;
    }
    // for books, the current is proper
    if (InspirationsRegistry.isBook(held)) {
      return slotIndex;
    }
    // if the next slot if filled (or invalid), this spot is invalid
    // if the previous is invalid however, we can use that
    if (shelfIndex == 7 || !inventory.getStackInSlot(slotIndex + 1).isEmpty()) {
      return previousEmpty ? slotIndex - 1 : -1;
    }
    // centering the item feels more natural, so on even pixels attempt to offset back one
    if (previousEmpty && pixel % 2 == 0) {
      // shelf index 1 always works (0 will never hit this)
      if (shelfIndex == 1) {
        return slotIndex - 1;
      }
      // at 2 or more, need to ensure the item before the previous is empty or a book
      ItemStack beforePrevious = inventory.getStackInSlot(slotIndex - 2);
      if (beforePrevious.isEmpty() || InspirationsRegistry.isBook(beforePrevious)) {
        return slotIndex - 1;
      }
    }
    return slotIndex;
  }

  /*
   * Book logic
   */

  /**
   * Interacts with the TE, adding or removing a book if possible
   * @param player  Player interacting
   * @param hand    Hand used
   * @param click   Block relative click location
   * @return  True if the shelf was modified
   */
  public boolean interact(Player player, InteractionHand hand, Vec3 click) {
    ItemStack stack = player.getItemInHand(hand);
    int index = getIndexFromHit(stack, click);
    if (index == -1) {
      return false;
    }

    // if it contains a book, take the book out
    ItemStack current = inventory.getStackInSlot(index);
    if (!current.isEmpty()) {
      if (level != null && !level.isClientSide()) {
        player.getInventory().placeItemBackInInventory(current, Prediction.SERVER_ONLY);
        inventory.setStackInSlot(index, ItemStack.EMPTY);
      }
      return true;
    }

    // try adding book
    if (inventory.canInsertItem(index, stack)) {
      if (level != null && !level.isClientSide()) {
        inventory.setStackInSlot(index, stack.split(1));
      }
      return true;
    }
    return false;
  }

  /**
   * Called when the shelf items change
   * @param slot       Slot index
   * @param oldStack   Old stack in the slot
   * @param newStack   New stack in the slot, may be same instance as the old if the size just changed
   */
  public void onSlotChanged(int slot, ItemStack oldStack, ItemStack newStack) {
    // slot update
    setChangedFast();
    Level world = getLevel();
    if (world != null) {
      if (!world.isClientSide()) {
        InspirationsNetwork.sendToClients(world, this.worldPosition, new InventorySlotSyncPacket(newStack, slot, worldPosition));
      }
      else {
        // update displayed book
        int oldBooks = this.books;
        int books;
        if (InspirationsRegistry.isBook(newStack)) {
          books = oldBooks | 1 << slot;
        } else {
          books = oldBooks & ~(1 << slot);
        }
        if (books != oldBooks) {
          this.books = books;
          RetexturedHelper.onTextureUpdated(this);
        }
      }

      // if we have redstone books and either the old stack xor the new one is a book, update
      if (oldStack.getItem() == InspirationsBuilding.redstoneBook ^ newStack.getItem() == InspirationsBuilding.redstoneBook) {
        world.updateNeighbourForOutputSignal(worldPosition, this.getBlockState().getBlock());
      }
    }
    // clear bonus to recalculate it
    enchantBonus = Float.NaN;
  }


  /*
   * Inventory
   */

  /** Gets the shelf inventory */
  public ShelfInventory getInventory() {
    return inventory;
  }



  /*
   * GUI
   */

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int winId, Inventory playerInv, Player player) {
    return new ShelfContainerMenu(winId, playerInv, this);
  }


  /*
   * Extra logic
   */

  /** Gets the value to display on comparators */
  public int getComparatorPower() {
    // return largest slot index of redstone book
    for (int i = ShelfInventory.MAX_ITEMS; i >= 0; i--) {
      if (inventory.getStackInSlot(i).getItem() == InspirationsBuilding.redstoneBook) {
        return i;
      }
    }
    return 0;
  }

  /** Gets the power for an enchantment table */
  public float getEnchantPower() {
    // if we have a cached value, use that
    if (!Float.isNaN(enchantBonus)) {
      return enchantBonus;
    }
    // simple sum of all books with the power of a full shelf
    float books = 0;
    for (int i = 0; i < ShelfInventory.MAX_ITEMS; i++) {
      ItemStack stack = inventory.getStackInSlot(i);
      if (!stack.isEmpty()) {
        float power = InspirationsRegistry.getBookEnchantingPower(stack);
        if (power > 0) {
          books += power;
        }
      }
    }

    // divide by 14 since that is the number of books in a shelf
    enchantBonus = books / ShelfInventory.MAX_ITEMS;
    return enchantBonus;
  }


  /* Item stack components: the texture moves between the block and its item */

  @Override
  protected void applyImplicitComponents(DataComponentGetter components) {
    super.applyImplicitComponents(components);
    Block block = components.get(KilnworkRegistries.TEXTURE);
    this.texture = block != null ? block : Blocks.AIR;
  }

  @Override
  protected void collectImplicitComponents(DataComponentMap.Builder components) {
    super.collectImplicitComponents(components);
    IRetexturedBlockEntity.collectImplicitComponents(this, components);
  }

  @Override
  public void removeComponentsFromTag(ValueOutput output) {
    super.removeComponentsFromTag(output);
    output.discard(TAG_TEXTURE);
  }

  /*
   * Rendering
   */

  @Override
  public Block getTexture() {
    return texture;
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  @Override
  public void updateTexture(Block texture) {
    Block oldTexture = this.texture;
    this.texture = texture;
    if (oldTexture != texture) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  /** Refreshes the book property */
  private void refreshBooks() {
    int books = 0;
    for (int i = 0; i < ShelfInventory.MAX_ITEMS; i++) {
      // non books will render in the TESR
      if (InspirationsRegistry.isBook(inventory.getStackInSlot(i))) {
        books |= 1 << i;
      }
    }
    this.books = books;
  }

  @Nonnull
  @Override
  public ModelData getModelData() {
    // the texture property refuses air: untextured shelves leave it out
    return RetexturedHelper.getModelDataBuilder(getTexture()).with(BOOKS, books).build();
  }


  /*
   * Networking
   */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }


  /* NBT */
  private static final String TAG_ITEMS = "Items";

  @Override
  public void saveSynced(ValueOutput output) {
    super.saveSynced(output);
    inventory.serialize(output, TAG_ITEMS);
    if (texture != Blocks.AIR) {
      output.putString(TAG_TEXTURE, getTextureName());
    }
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    boolean needsUpdate = false;
    if (inventory.deserialize(input, TAG_ITEMS)) {
      refreshBooks();
      needsUpdate = true;
    }
    String textureName = RetexturedBlockEntity.readTextureName(input);
    if (textureName != null) {
      texture = RetexturedHelper.getBlock(textureName);
      needsUpdate = true;
    }
    if (needsUpdate) {
      RetexturedHelper.onTextureUpdated(this);
    }
  }
}
