package knightminer.inspirations.library;

import knightminer.inspirations.Inspirations;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.resources.Identifier;

public class InspirationsTags {
  public static class Blocks {
    public static final TagKey<Block> MULCH = tag("mulch");
    public static final TagKey<Block> CARPETED_TRAPDOORS = tag("carpeted_trapdoors");
    public static final TagKey<Block> CARPETED_PRESSURE_PLATES = tag("carpeted_pressure_plates");
    public static final TagKey<Block> SHELVES = tag("shelves");
    public static final TagKey<Block> ENLIGHTENED_BUSHES = tag("enlightened_bushes");

    /** Blocks with this tag act as fire for the cauldron */
    public static final TagKey<Block> CAULDRON_FIRE = tag("cauldron_fire");
    /** Blocks that act as ice to surround the cauldron */
    public static final TagKey<Block> CAULDRON_ICE = tag("cauldron_ice");

    /**
     * Creates a block tag for Inspirations
     */
    private static TagKey<Block> tag(String name) {
      return BlockTags.create(Inspirations.getResource(name));
    }
  }

  public static class Items {
    // Duplicates of above. Flower pots and pressure plates have no item form.
    public static final TagKey<Item> MULCH = tag("mulch");
    public static final TagKey<Item> CARPETED_TRAPDOORS = tag("carpeted_trapdoors");
    public static final TagKey<Item> BOOKSHELVES = tag("bookshelves");
    public static final TagKey<Item> ENLIGHTENED_BUSHES = tag("enlightened_bushes");

    /**
     * Items with this tag are registered to perform cauldron recipes.
     */
    public static final TagKey<Item> DISP_CAULDRON_RECIPES = tag("cauldron_recipes");

    /**
     * Items with this tag are registered to have fluid tank functionality.
     */
    public static final TagKey<Item> DISP_FLUID_TANKS = tag("fluid_containers");

    public static final TagKey<Item> MILK_CONTAINERS = tag("milk_containers");

    /**
     * Forge tags for anything that is a book
     */
    public static final TagKey<Item> COMMON_BOOKS = commonTag("books");

    /**
     * Items which are valid to be placed on bookshelves.
     */
    public static final TagKey<Item> SHELF_BOOKS = tag("books");

    /**
     * Vanilla carpets and shulker boxes, for recipe use.
     */
    public static final TagKey<Item> CARPETS = tag("carpets");
    public static final TagKey<Item> SHULKER_BOXES = tag("shulker_boxes");
    public static final TagKey<Item> TERRACOTTA = tag("terracotta");

    /**
     * Creates an item tag for Inspirations
     */
    private static TagKey<Item> tag(String name) {
      return ItemTags.create(Inspirations.getResource(name));
    }
    private static TagKey<Item> commonTag(String name) {
      return ItemTags.create(Identifier.fromNamespaceAndPath("c", name));
    }
  }

  public static class Fluids {
    /** Contains Inspirations honey fluids (still and flowing)). The common tag is {@code c:<name>}. */
    public static final TagKey<Fluid> HONEY = tag("honey");
    /** Contains Inspirations beetroot soup fluids (still and flowing)). The common tag is {@code c:<name>}. */
    public static final TagKey<Fluid> BEETROOT_SOUP = tag("beetroot_soup");
    /** Contains Inspirations mushroom stew fluids (still and flowing)). The common tag is {@code c:<name>}. */
    public static final TagKey<Fluid> MUSHROOM_STEW = tag("mushroom_stew");
    /** Contains Inspirations rabbit stew fluids (still and flowing). The common tag is {@code c:<name>}. */
    public static final TagKey<Fluid> RABBIT_STEW = tag("rabbit_stew");
    /** Contains Inspirations potato soup fluids (still and flowing) */
    public static final TagKey<Fluid> POTATO_SOUP = tag("potato_soup");

    private static TagKey<Fluid> tag(String name) {
      return FluidTags.create(Inspirations.getResource(name));
    }
  }

  /** Common tags of the shared {@code c} namespace, and Mantle's soup tag kept for compatibility */
  public static class Common {
    public static final TagKey<Item> SPLASH_BOTTLE = ItemTags.create(Identifier.fromNamespaceAndPath("c", "bottles/splash"));
    public static final TagKey<Item> LINGERING_BOTTLE = ItemTags.create(Identifier.fromNamespaceAndPath("c", "bottles/lingering"));
    public static final TagKey<Fluid> HONEY = FluidTags.create(Identifier.fromNamespaceAndPath("c", "honey"));
    public static final TagKey<Fluid> BEETROOT_SOUP = FluidTags.create(Identifier.fromNamespaceAndPath("c", "beetroot_soup"));
    public static final TagKey<Fluid> MUSHROOM_STEW = FluidTags.create(Identifier.fromNamespaceAndPath("c", "mushroom_stew"));
    public static final TagKey<Fluid> RABBIT_STEW = FluidTags.create(Identifier.fromNamespaceAndPath("c", "rabbit_stew"));
    /** Mantle's tag of all soups, other mods of SlimeKnights read it */
    public static final TagKey<Fluid> SOUP = FluidTags.create(Identifier.fromNamespaceAndPath("mantle", "soup"));
  }
}
