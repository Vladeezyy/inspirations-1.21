package knightminer.inspirations.tweaks.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

import java.util.function.BooleanSupplier;

/**
 * A brewing recipe type which can be applied to all three potion item types.
 */
public class NormalBrewingRecipe implements IBrewingRecipe {
  private final BooleanSupplier enabled;
  private final Holder<Potion> start;
  private final Ingredient catalyst;
  private final Holder<Potion> output;

  public NormalBrewingRecipe(Holder<Potion> start, Ingredient catalyst, Holder<Potion> output, BooleanSupplier enabledFunc) {
    this.start = start;
    this.catalyst = catalyst;
    this.output = output;
    this.enabled = enabledFunc;
  }

  public Ingredient getCatalyst() {
    return catalyst;
  }

  public Holder<Potion> getStart() {
    return start;
  }

  public Holder<Potion> getOutput() {
    return output;
  }

  @Override
  public boolean isInput(ItemStack input) {
    Item item = input.getItem();
    if (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
      return input.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(start);
    }
    return false;
  }

  @Override
  public boolean isIngredient(ItemStack ingredient) {
    return catalyst.test(ingredient);
  }

  @Override
  public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
    if (!enabled.getAsBoolean()) {
      return ItemStack.EMPTY;
    }
    if (!catalyst.test(ingredient)) {
      return ItemStack.EMPTY;
    }
    Item item = input.getItem();
    if (isInput(input)) {
      return PotionContents.createItemStack(item, output);
    }
    return ItemStack.EMPTY;
  }
}
