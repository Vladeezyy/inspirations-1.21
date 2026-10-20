package knightminer.inspirations.building.client;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import knightminer.inspirations.building.block.entity.ShelfInventory;
import knightminer.inspirations.building.block.menu.ShelfContainerMenu;
import knightminer.inspirations.common.client.BackgroundContainerScreen;
import knightminer.inspirations.library.InspirationsRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Screen for bookshelves */
public class ShelfContainerScreen extends BackgroundContainerScreen<ShelfContainerMenu> {
	private static final Identifier BACKGROUND = Inspirations.getResource("textures/gui/shelf.png");
	/** Part of the background texture: x, y, width, height */
	private record Element(int u, int v, int width, int height) {
		void draw(GuiGraphicsExtractor graphics, int x, int y) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, u, v, width, height, 256, 256);
		}
	}
	private static final Element SLOT_COVER = new Element(176, 0, 18, 18);
	private static final Element BOOK_SLOT_ICON = new Element(176, 18, 16, 16);

	/**
	 * Creates a new screen instance
	 * @param container  Container class
	 * @param inventory  Player inventory
	 * @param name       Container name
	 */
	public ShelfContainerScreen(ShelfContainerMenu container, Inventory inventory, Component name) {
		super(container, inventory, name, 156, BACKGROUND);
	}

	/** Checks if a slot should be covered, drawing the cover if needed */
	private void checkBookIcon(GuiGraphicsExtractor graphics, ShelfInventory inventory, int index) {
		// draw icon if the slot is empty and the next is filled, means books only
		if (inventory.getStackInSlot(index).isEmpty() && ((index % 8 == 7) || !inventory.getStackInSlot(index + 1).isEmpty())) {
			Slot slot = getMenu().getSlot(index);
			BOOK_SLOT_ICON.draw(graphics, this.leftPos + slot.x, this.topPos + slot.y);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		// draw book icon in slots that can only hold a book
		ShelfBlockEntity shelf = getMenu().getTile();
		if (shelf != null) {
			ShelfInventory inventory = shelf.getInventory();
			for (int i = 0; i < 16; i++) {
				checkBookIcon(graphics, inventory, i);
			}
		}
	}

	/** Checks if a slot should be covered, drawing the cover if needed */
	private void checkCoverSlot(GuiGraphicsExtractor graphics, ShelfInventory inventory, int index) {
		// draw cover if the slot is empty and the previous is filled with a non-book
		if (inventory.getStackInSlot(index).isEmpty()) {
			ItemStack previous = inventory.getStackInSlot(index - 1);
			if (!previous.isEmpty() && !InspirationsRegistry.isBook(previous)) {
				Slot slot = getMenu().getSlot(index);
				SLOT_COVER.draw(graphics, slot.x - 1, slot.y - 1);
			}
		}
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {
		super.extractLabels(graphics, x, y);
		// draw cover to block invalid slots, drawn in foreground to cover the slot highlight
		ShelfBlockEntity shelf = getMenu().getTile();
		if (shelf != null) {
			ShelfInventory inventory = shelf.getInventory();
			// draw each of the two rows, skipping the first slot
			for (int i = 1; i < 8; i++) {
				checkCoverSlot(graphics, inventory, i);
			}
			for (int i = 9; i < 16; i++) {
				checkCoverSlot(graphics, inventory, i);
			}
		}
	}
}
