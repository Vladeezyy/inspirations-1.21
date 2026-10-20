package knightminer.inspirations.common.client;

import knightminer.inspirations.Inspirations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Generic container screen that simply draws the given background
 * @param <T> Container type
 */
@SuppressWarnings("WeakerAccess")
public class BackgroundContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
  /** Background drawn for this screen */
  protected final Identifier background;

  /**
   * Creates a new screen instance
   * @param container  Container class
   * @param inventory  Player inventory
   * @param name       Container name
   * @param background Container background
   */
  public BackgroundContainerScreen(T container, Inventory inventory, Component name, int height, Identifier background) {
    super(container, inventory, name, 176, height);
    this.background = background;
  }

  @Override
  protected void init() {
    super.init();
    this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
  }

  @Override
  public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
    super.extractBackground(graphics, mouseX, mouseY, partialTicks);
    graphics.blit(RenderPipelines.GUI_TEXTURED, this.background, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
  }

  public static class Factory<T extends AbstractContainerMenu> implements ScreenConstructor<T,BackgroundContainerScreen<T>> {
    private final Identifier background;
    private final int height;

    /**
     * Creates a factory from the given background location
     * @param height     Screen height
     * @param background Background location
     */
    public Factory(int height, Identifier background) {
      this.height = height;
      this.background = background;
    }

    /**
     * Creates a factory from the container name
     * @param height Screen height
     * @param name   Name of this container
     */
    public Factory(int height, String name) {
      this(height, Inspirations.getResource(String.format("textures/gui/%s.png", name)));
    }

    @Override
    public BackgroundContainerScreen<T> create(T container, Inventory inventory, Component name) {
      return new BackgroundContainerScreen<>(container, inventory, name, height, background);
    }
  }
}
