package knightminer.inspirations.common.datagen;

import dev.vladeezyy.kilnwork.retexture.ShapedRetexturedRecipe;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.library.recipe.crafting.ShapelessNoContainerRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.function.UnaryOperator;

/**
 * Helpers for the recipe providers, the parts of Mantle 1.20's IRecipeHelper and ConsumerWrapperBuilder that
 * Inspirations used, on 1.21 recipe outputs.
 */
public interface IInspirationsRecipeBuilder {
  /** Location in the Inspirations namespace */
  default ResourceLocation location(String name) {
    return Inspirations.getResource(name);
  }

  /** Name prefixed with the mod id, for groups */
  default String prefix(String name) {
    return Inspirations.modID + ":" + name;
  }

  /** Registry name of the item */
  default ResourceLocation id(ItemLike item) {
    return BuiltInRegistries.ITEM.getKey(item.asItem());
  }

  /** Location of the id's path under a folder of the Inspirations namespace */
  default ResourceLocation prefix(ResourceLocation id, String prefix) {
    return location(prefix + id.getPath());
  }

  /** Output adding the conditions to every recipe */
  default RecipeOutput withCondition(RecipeOutput output, ICondition... conditions) {
    return output.withConditions(conditions);
  }

  /**
   * Gives a provider another name. RecipeProvider.getName() is final in 1.21 and the data generator needs one name
   * per provider, so each module's recipes are wrapped.
   */
  static net.minecraft.data.DataProvider named(String name, net.minecraft.data.DataProvider provider) {
    return new net.minecraft.data.DataProvider() {
      @Override
      public java.util.concurrent.CompletableFuture<?> run(net.minecraft.data.CachedOutput output) {
        return provider.run(output);
      }

      @Override
      public String getName() {
        return name;
      }
    };
  }

  /** Output changing every recipe before passing it on */
  static RecipeOutput mapping(RecipeOutput output, UnaryOperator<Recipe<?>> mapper) {
    return new RecipeOutput() {
      @Override
      public Advancement.Builder advancement() {
        return output.advancement();
      }

      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        output.accept(id, mapper.apply(recipe), advancement, conditions);
      }
    };
  }

  /** Output turning shaped recipes into recipes that take the texture of the matching ingredient (Kilnwork) */
  static RecipeOutput retextured(RecipeOutput output, Ingredient texture, boolean matchAll) {
    return mapping(output, recipe -> {
      if (recipe instanceof ShapedRecipe shaped) {
        return new ShapedRetexturedRecipe(shaped.getGroup(), shaped.category(), shaped.pattern, shaped.getResultItem(RegistryAccess.EMPTY), shaped.showNotification(), texture, matchAll);
      }
      throw new IllegalArgumentException("Retextured recipes must be shaped: " + recipe);
    });
  }

  /** Output turning shapeless recipes into recipes that keep their containers, such as the bottles of dyes */
  static RecipeOutput noContainer(RecipeOutput output) {
    return mapping(output, recipe -> {
      if (recipe instanceof ShapelessRecipe shapeless) {
        return new ShapelessNoContainerRecipe(shapeless.getGroup(), shapeless.category(), shapeless.getResultItem(RegistryAccess.EMPTY), NonNullList.copyOf(shapeless.getIngredients()));
      }
      throw new IllegalArgumentException("No container recipes must be shapeless: " + recipe);
    });
  }
}
