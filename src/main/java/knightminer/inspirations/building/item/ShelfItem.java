package knightminer.inspirations.building.item;

import dev.vladeezyy.kilnwork.retexture.RetexturedBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;


/**
 * Texture block item (texture tooltip), burnable
 */
public class ShelfItem extends RetexturedBlockItem {
  public ShelfItem(Block block, Item.Properties properties) {
    super(block, properties);
  }
  // the fuel (300 ticks) is the cooking_fuel component since 26.3, see InspirationsBuilding.defaultComponents
}
