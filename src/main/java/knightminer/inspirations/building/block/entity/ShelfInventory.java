package knightminer.inspirations.building.block.entity;

import dev.vladeezyy.kilnwork.util.LegacyItemStacks;
import knightminer.inspirations.library.InspirationsRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Books and items on a shelf, one per slot. A vanilla container since NeoForge moved item handlers to the transfer API
 * (the capability wraps it, {@code VanillaContainerWrapper}); the slot rules of 1.20 are in {@link #canPlaceItem}.
 */
public class ShelfInventory implements Container {
	/** Max size of the shelf */
	public static final int MAX_ITEMS = 16;

	/** Tile entity holding this inventory */
	private final ShelfBlockEntity parent;
	/** List of items in this inventory */
	private final NonNullList<ItemStack> stacks = NonNullList.withSize(MAX_ITEMS, ItemStack.EMPTY);

	/** Slots holding items that are not books (the renderer draws those), -1 to count again */
	private int itemMask = -1;
	/** Book cache generation the mask was counted with */
	private int itemMaskGeneration;

	public ShelfInventory(ShelfBlockEntity parent) {
		this.parent = parent;
	}

	/** Called when the slot content change */
	private void onSlotChanged(int slot, ItemStack oldStack) {
		itemMask = -1;
		parent.onSlotChanged(slot, oldStack, stacks.get(slot));
	}

	/**
	 * Bit per slot holding an item that is not a book: what the block entity renderer draws, asked every frame, so it is
	 * counted again only after a change or a reload of the book tag
	 */
	public int getItemMask() {
		int generation = InspirationsRegistry.getBookGeneration();
		if (itemMask < 0 || itemMaskGeneration != generation) {
			int mask = 0;
			for (int i = 0; i < MAX_ITEMS; i++) {
				ItemStack stack = stacks.get(i);
				if (!stack.isEmpty() && !InspirationsRegistry.isBook(stack)) {
					mask |= 1 << i;
				}
			}
			itemMask = mask;
			itemMaskGeneration = generation;
		}
		return itemMask;
	}

	/* Container */

	@Override
	public int getContainerSize() {
		return MAX_ITEMS;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return stacks.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return getStackInSlot(slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (isSlotValid(slot)) {
			setStackInSlot(slot, stack);
		}
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack current = getStackInSlot(slot);
		if (amount <= 0 || current.isEmpty()) {
			return ItemStack.EMPTY;
		}
		setStackInSlot(slot, ItemStack.EMPTY);
		return current;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack current = getStackInSlot(slot);
		if (isSlotValid(slot)) {
			stacks.set(slot, ItemStack.EMPTY);
			itemMask = -1;
		}
		return current;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return canHoldItem(slot, stack);
	}

	@Override
	public void setChanged() {
		parent.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(parent, player);
	}

	@Override
	public void clearContent() {
		java.util.Collections.fill(stacks, ItemStack.EMPTY);
		itemMask = -1;
	}

	/* Helpers */

	/** Checks if the slot is valid */
	private boolean isSlotValid(int slot) {
		return slot >= 0 && slot < MAX_ITEMS;
	}

	/**
	 * Checks if the slot can hold this item in general, ignoring current contents
	 * Considers neighbor slots, unlike {@link #isItemValid(int, ItemStack)}
	 */
	public boolean canHoldItem(int slot, ItemStack stack) {
		// invalid index, bad
		if (!isSlotValid(slot) || stack.isEmpty()) {
			return false;
		}
		// cannot have a item in the stack before the current
		if (slot != 0 && slot != 8) {
			ItemStack previous = stacks.get(slot - 1);
			if (!previous.isEmpty() && !InspirationsRegistry.isBook(previous)) {
				return false;
			}
		}
		// books are always valid
		if (InspirationsRegistry.isBook(stack)) {
			return true;
		}
		// if last item in the shelf is only valid for books
		if (slot == 7 || slot == 15) {
			return false; // TODO: consider checking neighbor
		}
		// must have no stack in the next slot
		return stacks.get(slot + 1).isEmpty();
	}

	/** Checks if the given slot can accept this item */
	public boolean canInsertItem(int slot, ItemStack stack) {
		return canHoldItem(slot, stack) && stacks.get(slot).isEmpty();
	}


	/* Read write */

	public ItemStack getStackInSlot(int slot) {
		return isSlotValid(slot) ? stacks.get(slot) : ItemStack.EMPTY;
	}

	public void setStackInSlot(int slot, ItemStack stack) {
		if (stack.isEmpty() || canHoldItem(slot, stack)) {
			ItemStack oldStack = stacks.get(slot);
			this.stacks.set(slot, stack);
			onSlotChanged(slot, oldStack);
		}
	}


	/* NBT */
	private static final String SLOT_TAG = "Slot";

	/** Writes the items, as {@code [{Slot: n, ...}]} */
	public void serialize(ValueOutput output, String key) {
		ValueOutput.TypedOutputList<ItemStackWithSlot> list = output.list(key, ItemStackWithSlot.CODEC);
		for (int i = 0; i < MAX_ITEMS; i++) {
			ItemStack stack = stacks.get(i);
			if (!stack.isEmpty()) {
				list.add(new ItemStackWithSlot(i, stack));
			}
		}
	}

	/** Reads the items, also the format of 1.20 worlds; false if the key is missing */
	public boolean deserialize(ValueInput input, String key) {
		var list = input.read(key, CompoundTag.CODEC.listOf());
		if (list.isEmpty()) {
			return false;
		}
		// slots missing from the list are empty, 1.20 kept what was there before
		java.util.Collections.fill(stacks, ItemStack.EMPTY);
		itemMask = -1;
		for (CompoundTag itemTag : list.get()) {
			int slot = itemTag.getByteOr(SLOT_TAG, (byte) 0) & 0xFF;
			if (isSlotValid(slot)) {
				stacks.set(slot, LegacyItemStacks.parse(input.lookup(), itemTag));
			}
		}
		return true;
	}
}
