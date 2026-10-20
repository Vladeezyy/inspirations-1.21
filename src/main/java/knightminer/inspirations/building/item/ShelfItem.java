package knightminer.inspirations.building.item;

import dev.vladeezyy.kilnwork.retexture.RetexturedBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;

/**
 * Texture block item (texture tooltip) that is burnable
 */
public class ShelfItem extends RetexturedBlockItem {
  public ShelfItem(Block block, Item.Properties properties) {
    super(block, properties);
  }

  @Override
  public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
    return 300;
  }
}
