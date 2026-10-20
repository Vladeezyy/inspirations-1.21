package knightminer.inspirations.library.recipe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import knightminer.inspirations.common.InspirationsCommons;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

/**
 * Shapeless recipe variant that suppresses container returns.
 */
public class ShapelessNoContainerRecipe extends ShapelessRecipe {
  private final String group;
  private final ItemStack result;
  private final List<Ingredient> inputs;

  /**
   * Creates a new recipe using all required properties
   * @param group   Recipe group
   * @param result  Recipe result
   * @param inputs  Recipe inputs
   */
  public ShapelessNoContainerRecipe(String group, CraftingBookCategory category, ItemStack result, List<Ingredient> inputs) {
    super(group, category, result, inputs);
    this.group = group;
    this.result = result;
    this.inputs = inputs;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
    return NonNullList.withSize(input.size(), ItemStack.EMPTY);
  }

  @SuppressWarnings("unchecked")
  @Override
  public RecipeSerializer<ShapelessRecipe> getSerializer() {
    // the serializer makes this subclass, vanilla's return type names the parent
    return (RecipeSerializer<ShapelessRecipe>)(RecipeSerializer<?>)InspirationsCommons.shapelessNoContainer;
  }

  /**
   * Serializer reading the shapeless format (the fields of ShapelessRecipe are not visible here since 1.21.2)
   */
  public static class Serializer implements RecipeSerializer<ShapelessNoContainerRecipe> {
    private static final MapCodec<ShapelessNoContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
      CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
      ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
      Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.inputs)
    ).apply(inst, ShapelessNoContainerRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, ShapelessNoContainerRecipe> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8, recipe -> recipe.group,
      CraftingBookCategory.STREAM_CODEC, ShapelessRecipe::category,
      ItemStack.STREAM_CODEC, recipe -> recipe.result,
      Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.inputs,
      ShapelessNoContainerRecipe::new);

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
