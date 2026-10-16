package knightminer.inspirations.utility.datagen;

import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.common.InspirationsCommons;
import knightminer.inspirations.common.data.ConfigEnabledCondition;
import knightminer.inspirations.common.datagen.IInspirationsRecipeBuilder;
import knightminer.inspirations.library.InspirationsTags;
import knightminer.inspirations.utility.InspirationsUtility;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.advancements.Criterion;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;


public class UtilityRecipeProvider extends RecipeProvider implements IInspirationsRecipeBuilder {
  public UtilityRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
    super(packOutput, registries);
  }

  @Override
  protected void buildRecipes(RecipeOutput consumer) {
    // collector
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, InspirationsUtility.collector)
                       .unlockedBy("has_rod", has(Items.FISHING_ROD))
                       .define('C', Tags.Items.COBBLESTONES)
                       .define('R', Tags.Items.DUSTS_REDSTONE)
                       .define('F', Items.FISHING_ROD)
                       .pattern("CCC")
                       .pattern("CFC")
                       .pattern("CRC")
                       .save(withCondition(consumer, ConfigEnabledCondition.COLLECTOR), prefix(id(InspirationsUtility.collector), "utility/"));

    // pipe
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, InspirationsUtility.pipe, 4)
                       .unlockedBy("has_hopper", has(Items.HOPPER))
                       .define('I', Tags.Items.INGOTS_IRON)
                       .define('P', ItemTags.PLANKS)
                       .pattern("IPI")
                       .save(withCondition(consumer, ConfigEnabledCondition.PIPE), prefix(id(InspirationsUtility.pipe), "utility/"));

    // redstone book
    ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, InspirationsBuilding.redstoneBook)
                          .unlockedBy("has_bookshelf", has(InspirationsTags.Items.BOOKSHELVES))
                          .requires(Tags.Items.LEATHERS)
                          .requires(Items.PAPER).requires(Items.PAPER)
                          .requires(Tags.Items.DUSTS_REDSTONE)
                          .save(withCondition(consumer, ConfigEnabledCondition.BOOKSHELF, ConfigEnabledCondition.REDSTONE_BOOK), prefix(id(InspirationsBuilding.redstoneBook), "utility/"));

    // torch lever
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, InspirationsUtility.torchLeverItem)
            .unlockedBy("has_torch", has(Items.TORCH))
            .unlockedBy("has_lever", has(Items.LEVER))
            .define('S', Tags.Items.COBBLESTONES)
            .define('T', Items.TORCH)
            .pattern("T")
            .pattern("S")
            .save(withCondition(consumer, ConfigEnabledCondition.TORCH_LEVER), prefix(id(InspirationsUtility.torchLeverItem), "utility/"));

    // soul torch lever
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, InspirationsUtility.soulLeverItem)
            .unlockedBy("has_soul_torch", has(Items.SOUL_TORCH))
            .unlockedBy("has_lever", has(Items.LEVER))
            .define('S', Tags.Items.COBBLESTONES)
            .define('T', Items.SOUL_TORCH)
            .pattern("T")
            .pattern("S")
            .save(withCondition(consumer, ConfigEnabledCondition.TORCH_LEVER), prefix(id(InspirationsUtility.soulLeverItem), "utility/"));

    // carpeted trapdoor.
    RecipeOutput trapdoorConfig = withCondition(consumer, ConfigEnabledCondition.CARPETED_TRAPDOOR);
    String carpetedGroup = prefix("carpeted_trapdoor");
    InspirationsUtility.carpetedTrapdoors.forEach((color, trapdoor) ->
                                                      ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, trapdoor)
                                                                         .unlockedBy("has_carpet", has(InspirationsTags.Items.CARPETS))
                                                                         .group(carpetedGroup)
                                                                         .define('C', InspirationsCommons.VANILLA_CARPETS.get(color))
                                                                         .define('T', ItemTags.WOODEN_TRAPDOORS)
                                                                         .pattern("C")
                                                                         .pattern("T")
                                                                         .save(trapdoorConfig, location("utility/carpeted_trapdoor/" + color.getSerializedName()))
                                                 );
  }
}
