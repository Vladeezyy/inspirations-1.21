package knightminer.inspirations.tweaks.datagen;

import knightminer.inspirations.common.data.ConfigEnabledCondition;
import knightminer.inspirations.common.datagen.IInspirationsRecipeBuilder;
import knightminer.inspirations.utility.InspirationsUtility;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.advancements.Criterion;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;


public class TweaksRecipeProvider extends RecipeProvider implements IInspirationsRecipeBuilder {
  public TweaksRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
    super(packOutput, registries);
  }

  @Override
  protected void buildRecipes(RecipeOutput consumer) {
    // Since these are mass-production recipes, show them after the user makes
    // several of the original.

    // stackable collector
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, InspirationsUtility.collector)
                       // builder is missing count, so use ItemPredicate constructor
                       .unlockedBy("many_collectors", inventoryTrigger(ItemPredicate.Builder.item().of(InspirationsUtility.collector).withCount(MinMaxBounds.Ints.atLeast(5))))
                       .define('T', Tags.Items.RODS_WOODEN)
                       .define('S', Tags.Items.STRINGS)
                       .define('D', Items.DROPPER)
                       .pattern("  T")
                       .pattern(" TS")
                       .pattern("TDS")
                       .save(withCondition(consumer, ConfigEnabledCondition.COLLECTOR, ConfigEnabledCondition.UNSTACKABLE_ALTS), location("tweaks/collector_stackable"));

    // stackable dispenser
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, Items.DISPENSER)
                       // builder is missing count, so use ItemPredicate constructor
                       .unlockedBy("many_collectors", inventoryTrigger(ItemPredicate.Builder.item().of(Items.DISPENSER).withCount(MinMaxBounds.Ints.atLeast(5))))
                       .define('T', Tags.Items.RODS_WOODEN)
                       .define('S', Tags.Items.STRINGS)
                       .define('D', Items.DROPPER)
                       .pattern(" TS")
                       .pattern("TDS")
                       .pattern(" TS")
                       .save(withCondition(consumer, ConfigEnabledCondition.UNSTACKABLE_ALTS), location("tweaks/dispenser_stackable"));
  }
}
