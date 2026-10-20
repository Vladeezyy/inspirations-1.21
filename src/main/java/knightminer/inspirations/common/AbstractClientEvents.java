package knightminer.inspirations.common;

import knightminer.inspirations.common.client.ItemColorSources;
import knightminer.inspirations.Inspirations;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClientEvents {
  /*
   * Null safe handlers
   */

  /** Block color by tint index as before 26.1; the level and position are null outside of the world, -1 is white */
  @FunctionalInterface
  public interface BlockColor {
    int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex);
  }

  /** The color with full alpha: since 26.1 the alpha of a tint counts, the handlers give RGB (-1 stays white) */
  private static int opaque(int color) {
    return color | 0xFF000000;
  }

  /** Tint sources for the tint indices 0 to {@code count - 1}: since 26.1 a block has one source per index */
  public static List<BlockTintSource> tintSources(BlockColor handler, int count) {
    List<BlockTintSource> sources = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      int index = i;
      sources.add(new BlockTintSource() {
        @Override
        public int color(BlockState state) {
          return opaque(handler.getColor(state, null, null, index));
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
          return opaque(handler.getColor(state, level, pos, index));
        }
      });
    }
    return sources;
  }

  /**
   * Null safe method to register block colors for the tint indices 0 to {@code count - 1}. Works around an issue
   * where registry events do not fire
   */
  protected static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event, int count, BlockColor handler, Block... blocks) {
    List<BlockTintSource> sources = tintSources(handler, count);
    for (Block block : blocks) {
      if (block != null) {
        event.register(sources, block);
      }
    }
  }

  /** {@link #registerBlockColors(RegisterColorHandlersEvent.BlockTintSources, int, BlockColor, Block...)} for tint index 0 */
  protected static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event, BlockColor handler, Block... blocks) {
    registerBlockColors(event, 1, handler, blocks);
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
