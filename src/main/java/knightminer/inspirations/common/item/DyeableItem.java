package knightminer.inspirations.common.item;

import knightminer.inspirations.library.MiscUtil;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/** Hidable item that is dyeable, color in {@code minecraft:dyed_color} */
public class DyeableItem extends Item {
  public DyeableItem(Properties properties) {
    super(properties);
  }

  /** Adds all variants to the given consumer */
  public void addVariants(Consumer<ItemStack> consumer) {
    for (DyeColor color : DyeColor.values()) {
      consumer.accept(MiscUtil.setColor(new ItemStack(this), MiscUtil.getColor(color)));
    }
  }
}
