package knightminer.inspirations.library.recipe.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

/**
 * Shapeless recipe variant that suppresses container returns.
 */
public class ShapelessNoContainerRecipe extends ShapelessRecipe {
  public static final MapCodec<ShapelessNoContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
    CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
    Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(recipe -> recipe.inputs)
  ).apply(inst, ShapelessNoContainerRecipe::new));
  public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessNoContainerRecipe> STREAM_CODEC = StreamCodec.composite(
    Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
    CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
    ItemStackTemplate.STREAM_CODEC, recipe -> recipe.result,
    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), recipe -> recipe.inputs,
    ShapelessNoContainerRecipe::new);
  /** Serializer reading the shapeless format, a record of the codecs since 26.1 */
  public static final RecipeSerializer<ShapelessNoContainerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

  private final ItemStackTemplate result;
  private final List<Ingredient> inputs;

  public ShapelessNoContainerRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> inputs) {
    super(commonInfo, bookInfo, result, inputs);
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
    return (RecipeSerializer<ShapelessRecipe>)(RecipeSerializer<?>)SERIALIZER;
  }
}
