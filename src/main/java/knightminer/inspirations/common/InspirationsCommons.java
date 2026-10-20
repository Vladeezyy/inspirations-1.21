package knightminer.inspirations.common;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.common.data.ConfigEnabledCondition;
import knightminer.inspirations.common.data.ReplaceItemLootModifier;
import knightminer.inspirations.library.recipe.crafting.ShapelessNoContainerRecipe;
import knightminer.inspirations.tools.InspirationsTools;
import knightminer.inspirations.tweaks.InspirationsTweaks;
import knightminer.inspirations.utility.InspirationsUtility;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import knightminer.inspirations.common.registration.EnumObject;
import dev.vladeezyy.kilnwork.retexture.RetexturedHelper;

import java.util.function.Predicate;

/**
 * Base module for common code between the modules
 */
public class InspirationsCommons extends ModuleBase {

  /**
   * Enum object for vanilla carpet blocks to aid in registration/lookups
   */
  public static final EnumObject<DyeColor,Block> VANILLA_CARPETS = new EnumObject.Builder<DyeColor,Block>(DyeColor.class)
      .put(DyeColor.WHITE, Blocks.WHITE_CARPET)
      .put(DyeColor.ORANGE, Blocks.ORANGE_CARPET)
      .put(DyeColor.MAGENTA, Blocks.MAGENTA_CARPET)
      .put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_CARPET)
      .put(DyeColor.YELLOW, Blocks.YELLOW_CARPET)
      .put(DyeColor.LIME, Blocks.LIME_CARPET)
      .put(DyeColor.PINK, Blocks.PINK_CARPET)
      .put(DyeColor.GRAY, Blocks.GRAY_CARPET)
      .put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_CARPET)
      .put(DyeColor.CYAN, Blocks.CYAN_CARPET)
      .put(DyeColor.PURPLE, Blocks.PURPLE_CARPET)
      .put(DyeColor.BLUE, Blocks.BLUE_CARPET)
      .put(DyeColor.BROWN, Blocks.BROWN_CARPET)
      .put(DyeColor.GREEN, Blocks.GREEN_CARPET)
      .put(DyeColor.RED, Blocks.RED_CARPET)
      .put(DyeColor.BLACK, Blocks.BLACK_CARPET)
      .build();

  @SubscribeEvent
  void register(RegisterEvent event) {
    // recipe serializers
    event.register(Registries.RECIPE_SERIALIZER, helper -> {
      helper.register(Inspirations.getResource("shapeless_no_container"), ShapelessNoContainerRecipe.SERIALIZER);
    });
    // config condition, for recipes and loot tables
    event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, helper -> helper.register(Inspirations.getResource("config"), ConfigEnabledCondition.CODEC));
    event.register(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, helper -> helper.register(Inspirations.getResource("replace_item"), ReplaceItemLootModifier.CODEC));
    event.register(Registries.LOOT_CONDITION_TYPE, helper -> {
      // the loot condition registry holds the codecs since 26.1
      helper.register(Inspirations.getResource("config"), ConfigEnabledCondition.CODEC);
    });
  }

  @SubscribeEvent
  void creativeTabs(BuildCreativeModeTabContentsEvent event) {
    ResourceKey<CreativeModeTab> key = event.getTabKey();
    if (key == CreativeModeTabs.NATURAL_BLOCKS) {
      if (Config.enableFlowers.getAsBoolean()) {
        accept(event, InspirationsBuilding.flower);
      }
      if (Config.smoothBlockCropGrowth.getAsBoolean()) {
        event.accept(InspirationsTweaks.cactusSeeds);
        event.accept(InspirationsTweaks.sugarCaneSeeds);
      }
    }
    else if (key == CreativeModeTabs.BUILDING_BLOCKS) {
      if (Config.enableMulch.getAsBoolean()) {
        accept(event, InspirationsBuilding.mulch);
      }
      if (Config.enablePath.getAsBoolean()) {
        accept(event, InspirationsBuilding.path);
      }
      if (Config.enableEnlightenedBush.getAsBoolean()) {
        accept(event, InspirationsBuilding.enlightenedBush, ItemTags.LEAVES);
      }
    }
    else if (key == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
      if (Config.enableRope.getAsBoolean()) {
        event.accept(InspirationsBuilding.rope);
        event.accept(InspirationsBuilding.vine);
      }
      if (Config.enableBookshelf.getAsBoolean()) {
        // 1.20 listed planks here while the recipe takes the texture from slabs, so creative and crafted shelves
        // never matched and JEI could not find the recipe of a creative shelf (original issue #247)
        accept(event, InspirationsBuilding.shelf, ItemTags.WOODEN_SLABS);
      }
      if (Config.enableColoredBooks.getAsBoolean()) {
        InspirationsBuilding.coloredBook.addVariants(event::accept);
      }
    }
    else if (key == CreativeModeTabs.REDSTONE_BLOCKS) {
      if (Config.enableGlassDoor.getAsBoolean()) {
        event.accept(InspirationsBuilding.glassDoorItem);
        event.accept(InspirationsBuilding.glassTrapdoor);
      }
      if (Config.enableCarpetedTrapdoor.getAsBoolean()) {
        accept(event, InspirationsUtility.carpetedTrapdoors);
      }
      if (Config.enableTorchLever.getAsBoolean()) {
        event.accept(InspirationsUtility.torchLeverItem);
        event.accept(InspirationsUtility.soulLeverItem);
      }
      if (Config.enableCollector.getAsBoolean()) {
        event.accept(InspirationsUtility.collector);
      }
      if (Config.enablePipe.getAsBoolean()) {
        event.accept(InspirationsUtility.pipe);
      }
      if (Config.enableRedstoneBook.getAsBoolean()) {
        event.accept(InspirationsBuilding.redstoneBook);
      }
    }
    else if (key == CreativeModeTabs.FOOD_AND_DRINKS) {
      if (Config.enableHeartbeet.getAsBoolean()) {
        event.accept(InspirationsTweaks.heartbeet);
      }
      if (Config.enableCauldronSoups.getAsBoolean()) {
        event.accept(InspirationsCaudrons.potatoSoupItem);
      }
      if (Config.enableMilkBottles.getAsBoolean()) {
        event.accept(InspirationsCaudrons.milkBottle);
      }
    }
    else if (key == CreativeModeTabs.INGREDIENTS) {
      if (Config.enableCauldronPotions.getAsBoolean()) {
        event.accept(InspirationsCaudrons.splashBottle);
        event.accept(InspirationsCaudrons.lingeringBottle);
      }
      if (Config.enableCauldronDyeing.getAsBoolean()) {
        accept(event, InspirationsCaudrons.simpleDyedWaterBottle);
      }
    }
    else if (key == CreativeModeTabs.TOOLS_AND_UTILITIES) {
      if (Config.enableRedstoneCharger.getAsBoolean()) {
        event.accept(InspirationsTools.redstoneCharger);
        event.accept(InspirationsTools.redstoneArrow);
      }
      if (Config.enableLock.getAsBoolean()) {
        event.accept(InspirationsTools.lock);
        event.accept(InspirationsTools.key);
      }
      if (Config.enableNorthCompass.getAsBoolean()) {
        event.accept(InspirationsTools.northCompass);
      }
      if (Config.enableDimensionCompass.getAsBoolean()) {
        event.accept(InspirationsTools.dimensionCompass);
      }
      if (Config.enableBarometer.getAsBoolean()) {
        event.accept(InspirationsTools.barometer);
      }
      if (Config.enablePhotometer.getAsBoolean()) {
        event.accept(InspirationsTools.photometer);
      }
      if (Config.enableCauldronSoups.getAsBoolean()) {
        event.accept(InspirationsCaudrons.mushroomStewBucket);
        event.accept(InspirationsCaudrons.beetrootSoupBucket);
        event.accept(InspirationsCaudrons.rabbitStewBucket);
        event.accept(InspirationsCaudrons.potatoSoupBucket);
      }
      if (Config.enableCauldronHoney.getAsBoolean()) {
        event.accept(InspirationsCaudrons.honeyBucket);
      }
    }
  }

  /** Adds the given enum object to the tab */
  private static void accept(BuildCreativeModeTabContentsEvent event, EnumObject<?, ? extends ItemLike> object) {
    object.forEach(item -> event.accept(item));
  }

  /** Adds the given enum object to the tab */
  private static void accept(BuildCreativeModeTabContentsEvent event, EnumObject<?, ? extends ItemLike> object, TagKey<Item> textures) {
    Predicate<ItemStack> acceptor =stack -> {
      event.accept(stack);
      // TODO: worth making this a JEI only thing?
      return !Config.get(Config.showAllVariants);
    };
    object.forEach(item -> RetexturedHelper.addTagVariants(acceptor, item, textures));
  }
}
