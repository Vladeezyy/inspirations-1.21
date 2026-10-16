package knightminer.inspirations.cauldrons.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

/** Recipe for transforming a bottle, depending on a vanilla brewing recipe to get the ingredient */
public class BottleBrewingRecipe extends BrewingRecipe {
  /**
   * Brewing of empty bottles
   * @param input       Bottle to brew
   * @param ingredient  Ingredient, the one of the matching potion container mix
   * @param output      Brewed bottle
   */
  public BottleBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
    super(input, ingredient, output);
  }
}
