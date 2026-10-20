package knightminer.inspirations.common;

import knightminer.inspirations.common.client.ItemColorSources;
import knightminer.inspirations.Inspirations;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

public abstract class AbstractClientEvents {
  /*
   * Null safe handlers
   */

  /**
   * Null safe method to register block colors. Works around an issue where registry events do not fire
   * @param blockColors Block colors instance
   * @param handler     Color handler logic
   * @param blocks      List of blocks to register
   */
  protected static void registerBlockColors(BlockColors blockColors, BlockColor handler, Block... blocks) {
    for (Block block : blocks) {
      if (block != null) {
        blockColors.register(handler, block);
      }
    }
  }

  /**
   * Null safe method to register item colors, read by the {@code inspirations:item_color} tint sources of the item
   * definitions
   * @param handler    Color handler logic
   * @param items      List of items to register
   */
  protected static void registerItemColors(ItemColorSources.ItemColor handler, ItemLike... items) {
    ItemColorSources.registerColor(handler, items);
  }

  /**
   * Null safe way to register a model property, read by the {@code inspirations:property} of the item definitions
   * @param item Item with the property
   * @param name Property name, as in the item definition
   * @param prop Property getter instance
   */
  protected static void registerModelProperty(@Nullable ItemLike item, String name, ItemColorSources.ItemProperty prop) {
    ItemColorSources.registerProperty(item, name, prop);
  }

  /**
   * Null safe way to reigster a screen factory
   * @param type    Container type
   * @param factory Screen factory
   * @param <M>     Container type
   * @param <U>     Screen type
   */
  protected static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreenFactory(RegisterMenuScreensEvent event, @Nullable MenuType<? extends M> type, MenuScreens.ScreenConstructor<M,U> factory) {
    if (type != null) {
      event.register(type, factory);
    }
  }
}
