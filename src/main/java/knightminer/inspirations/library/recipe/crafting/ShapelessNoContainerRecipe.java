package knightminer.inspirations.library.recipe.crafting;

import com.mojang.serialization.MapCodec;
import knightminer.inspirations.common.InspirationsCommons;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * Shapeless recipe variant that suppresses container returns.
 */
public class ShapelessNoContainerRecipe extends ShapelessRecipe {
  /**
   * Creates a new recipe using all required properties
   * @param group   Recipe group
   * @param result  Recipe result
   * @param inputs  Recipe inputs
   */
  public ShapelessNoContainerRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> inputs) {
    super(group, category, result, inputs);
  }

  /**
   * Create a new recipe instance by copying an existing shapeless recipe
   * @param orig  Shapeless recipe to copy
   */
  private ShapelessNoContainerRecipe(ShapelessRecipe orig) {
    super(orig.getGroup(), orig.category(), orig.getResultItem(RegistryAccess.EMPTY), orig.getIngredients());
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    return NonNullList.withSize(input.size(), ItemStack.EMPTY);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return InspirationsCommons.shapelessNoContainer;
  }

  /**
   * Serializer reading the shapeless format
   */
  public static class Serializer implements RecipeSerializer<ShapelessNoContainerRecipe> {
    private static final MapCodec<ShapelessNoContainerRecipe> CODEC = RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(ShapelessNoContainerRecipe::new, recipe -> recipe);
    private static final StreamCodec<RegistryFriendlyByteBuf, ShapelessNoContainerRecipe> STREAM_CODEC = RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(ShapelessNoContainerRecipe::new, recipe -> recipe);

    @Override
    public MapCodec<ShapelessNoContainerRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ShapelessNoContainerRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
