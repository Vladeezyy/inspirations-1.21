package knightminer.inspirations.building.datagen;

import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.building.block.type.MulchType;
import knightminer.inspirations.building.block.type.PathType;
import knightminer.inspirations.building.block.type.ShelfType;
import knightminer.inspirations.common.data.ConfigEnabledCondition;
import knightminer.inspirations.common.datagen.IInspirationsRecipeBuilder;
import knightminer.inspirations.library.InspirationsTags;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.advancements.Criterion;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.Locale;
import java.util.Objects;

public class BuildingRecipeProvider extends RecipeProvider implements IInspirationsRecipeBuilder {
  public BuildingRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
    super(packOutput, registries);
  }

  @Override
  protected void buildRecipes(RecipeOutput consumer) {
    // glass doors
    RecipeOutput glassDoorCondition = withCondition(consumer, ConfigEnabledCondition.GLASS_DOOR);
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, InspirationsBuilding.glassDoor)
                       .unlockedBy("has_glass", has(Items.GLASS_PANE))
                       .define('G', Items.GLASS_PANE)
                       .pattern("GG")
                       .pattern("GG")
                       .pattern("GG")
                       .save(glassDoorCondition, prefix(id(InspirationsBuilding.glassDoor), "building/"));
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, InspirationsBuilding.glassTrapdoor, 2)
                       .unlockedBy("has_glass", has(Items.GLASS_PANE))
                       .define('G', Items.GLASS_PANE)
                       .pattern("GGG")
                       .pattern("GGG")
                       .save(glassDoorCondition, prefix(id(InspirationsBuilding.glassTrapdoor), "building/"));

    // rope
    RecipeOutput ropeCondition = withCondition(consumer, ConfigEnabledCondition.ROPE);
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, InspirationsBuilding.rope, 3)
                       .unlockedBy("has_string", has(Tags.Items.STRINGS))
                       .define('S', Items.STRING)
                       .pattern("SS")
                       .pattern("SS")
                       .pattern("SS")
                       .save(ropeCondition, prefix(id(InspirationsBuilding.rope), "building/"));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, InspirationsBuilding.vine, 3)
                       .unlockedBy("has_vines", has(Items.VINE))
                       .define('V', Items.VINE)
                       .pattern("V")
                       .pattern("V")
                       .pattern("V")
                       .save(ropeCondition, prefix(id(InspirationsBuilding.vine), "building/"));

    // path
    RecipeOutput pathCondition = withCondition(consumer, ConfigEnabledCondition.PATH);
    addPath(pathCondition, PathType.BRICK, Ingredient.of(Items.BRICKS), has(Items.BRICKS));
    addPath(pathCondition, PathType.ROCK, Ingredient.of(Tags.Items.COBBLESTONES), has(Tags.Items.COBBLESTONES));
    addPath(pathCondition, PathType.ROUND, Ingredient.of(Items.STONE), has(Items.STONE));
    addPath(pathCondition, PathType.TILE, Ingredient.of(Items.STONE_BRICKS), has(Items.STONE_BRICKS));

    // mulch
    RecipeOutput mulchCondition = withCondition(consumer, ConfigEnabledCondition.MULCH);
    // make plain in stonecutter
    ItemLike plainMulch = InspirationsBuilding.mulch.get(MulchType.PLAIN);
    SingleItemRecipeBuilder.stonecutting(Ingredient.of(ItemTags.PLANKS), RecipeCategory.BUILDING_BLOCKS, plainMulch)
                           .unlockedBy("hasPlanks", has(ItemTags.PLANKS))
                           .save(mulchCondition, location("building/mulch/" + MulchType.PLAIN.getSerializedName()));
    // dye for other colors
    InspirationsBuilding.mulch.forEach((type, mulch) -> {
      DyeColor dye = type.getDye();
      if (dye != null) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, mulch)
                              .unlockedBy("has_mulch", has(plainMulch))
                              .requires(plainMulch)
                              .requires(dye.getTag())
                              .save(mulchCondition, location("building/mulch/" + type.getSerializedName()));
      }
    });

    // colored books
    RecipeOutput bookConditions = withCondition(consumer, ConfigEnabledCondition.COLORED_BOOKS);
    String bookGroup = prefix("colored_book");
    for (DyeColor color : DyeColor.values()) {
      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, MiscUtil.setColor(new ItemStack(InspirationsBuilding.coloredBook), MiscUtil.getColor(color)))
                            .unlockedBy("has_bookshelf", has(InspirationsTags.Items.BOOKSHELVES))
                            .group(bookGroup)
                            .requires(Items.BOOK)
                            .requires(color.getTag())
                            .save(bookConditions, location("building/books/" + color.getSerializedName()));
    }

    // flowers
    // add dye crafting recipes
    RecipeOutput flowerConditions = withCondition(consumer, ConfigEnabledCondition.FLOWERS);
    InspirationsBuilding.flower.forEach((type, flower) -> {
      Item dye = type.getDye();
      ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, dye)
                            .unlockedBy("has_flower", has(flower))
                            .group(Objects.requireNonNull(dye).toString())
                            .requires(flower)
                            .save(flowerConditions, prefix(id(dye), "building/flower/"));
    });

    // bookshelves
    RecipeOutput bookshelfConditions = withCondition(consumer, ConfigEnabledCondition.BOOKSHELF);
    String shelfGroup = prefix("bookshelf");
    InspirationsBuilding.shelf.forEach((type, shelf) -> {
      String[] variants = getShelfVariants(type);
      for (String variant : variants) {
        ShapedRecipeBuilder builder =
            ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, shelf, 2)
                               .group(shelfGroup)
                               .unlockedBy("has_slabs", has(ItemTags.WOODEN_SLABS))
                               .unlockedBy("has_book", has(Items.BOOK))
                               .define('S', ItemTags.WOODEN_SLABS)
                               .pattern("SSS")
                               .pattern(variant)
                               .pattern("SSS");
        // add extra items for the variant
        switch (type) {
          case ANCIENT -> builder.define('P', Items.PAPER);
          case TOMES -> builder.define('B', Items.BOOK);
          case RAINBOW -> builder.define('R', Tags.Items.DYES_RED)
                                 .define('G', Tags.Items.DYES_GREEN)
                                 .define('B', Tags.Items.DYES_BLUE);
        }
        // add texture and build
        String suffix = variants.length > 1 ? "_" + variant.toLowerCase(Locale.US) : "";
        builder.save(IInspirationsRecipeBuilder.retextured(bookshelfConditions, Ingredient.of(ItemTags.WOODEN_SLABS), true), location("building/bookshelves/" + type.getSerializedName() + suffix));
      }
    });

    // enlightened bushes
    RecipeOutput bushConditions = withCondition(consumer, ConfigEnabledCondition.ENLIGHTENED_BUSH);
    String bushGroup = prefix("enlightened_bush");
    InspirationsBuilding.enlightenedBush.forEach((type, bush) -> {
      ShapedRecipeBuilder builder =
          ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, bush)
                             .group(bushGroup)
                             .unlockedBy("has_leaves", has(ItemTags.LEAVES))
                             .unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
                             .define('L', ItemTags.LEAVES)
                             .define('G', Tags.Items.DUSTS_GLOWSTONE);
      // white uses no dye
      DyeColor dye = type.getDye();
      if (dye != null) {
        // First line - dye above the middle.
        builder = builder.define('D', dye.getTag()).pattern(" D ");
      }
      builder.pattern("GLG");
      // add texture and build
      builder.save(IInspirationsRecipeBuilder.retextured(bushConditions, Ingredient.of(ItemTags.LEAVES), false), location("building/enlightened_bush/" + type.getSerializedName()));
    });
  }

  /**
   * Adds recipes for a path type.
   * @param consumer   Recipe consumer
   * @param type       Path type
   * @param ingredient Ingredient for crafting
   * @param criteria   Criteria instance
   */
  private void addPath(RecipeOutput consumer, PathType type, Ingredient ingredient, Criterion<?> criteria) {
    // crafting
    ItemLike path = InspirationsBuilding.path.get(type);
    // skip round path crafting, conflict with pressure plates
    if (type != PathType.ROUND) {
      ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, path, 6)
                         .unlockedBy("has_item", criteria)
                         .define('C', ingredient)
                         .pattern("CC")
                         .save(consumer, location("building/path/" + type.getSerializedName() + "_crafting"));
    }
    // stonecutting
    SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.DECORATIONS, path, 6)
                           .unlockedBy("has_stone", criteria)
                           .save(consumer, location("building/path/" + type.getSerializedName() + "_cutting"));
  }

  /**
   * Gets recipe variants for a shelf type
   * @param type Shelf type
   * @return Array of recipe variants
   */
  private static String[] getShelfVariants(ShelfType type) {
    return switch (type) {
      default -> new String[]{" S "};
      case ANCIENT -> new String[]{"PPP"};
      case TOMES -> new String[]{" B "};
      case RAINBOW -> new String[]{"RGB", "RBG", "GRB"};
    };
  }
}
