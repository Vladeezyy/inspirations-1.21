package knightminer.inspirations.building;

import knightminer.inspirations.common.datagen.IInspirationsRecipeBuilder;
import knightminer.inspirations.building.block.EnlightenedBushBlock;
import knightminer.inspirations.building.block.GlassDoorBlock;
import knightminer.inspirations.building.block.GrowableFlowerBlock;
import knightminer.inspirations.building.block.MulchBlock;
import knightminer.inspirations.building.block.PathBlock;
import knightminer.inspirations.building.block.RopeBlock;
import knightminer.inspirations.building.block.ShelfBlock;
import knightminer.inspirations.building.block.entity.EnlightenedBushBlockEntity;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import knightminer.inspirations.building.block.menu.ShelfContainerMenu;
import knightminer.inspirations.building.block.type.BushType;
import knightminer.inspirations.building.block.type.FlowerType;
import knightminer.inspirations.building.block.type.MulchType;
import knightminer.inspirations.building.block.type.PathType;
import knightminer.inspirations.building.block.type.ShelfType;
import knightminer.inspirations.building.datagen.BuildingRecipeProvider;
import knightminer.inspirations.building.item.ShelfItem;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.common.ModuleBase;
import knightminer.inspirations.common.item.DyeableItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import knightminer.inspirations.common.registration.BlockEntityTypeRegistryAdapter;
import knightminer.inspirations.common.registration.BlockRegistryAdapter;
import knightminer.inspirations.common.registration.ContainerTypeRegistryAdapter;
import knightminer.inspirations.common.registration.ItemRegistryAdapter;
import knightminer.inspirations.common.registration.EnumObject;

import java.util.function.Supplier;

/**
 * Module containing all the building blocks
 */
public class InspirationsBuilding extends ModuleBase {
  // blocks
  public static RopeBlock rope;
  public static RopeBlock vine;
  public static Block glassDoor;
  public static Block glassTrapdoor;
  // enum
  public static EnumObject<ShelfType,ShelfBlock> shelf = EnumObject.empty();
  public static EnumObject<MulchType,MulchBlock> mulch = EnumObject.empty();
  public static EnumObject<PathType,PathBlock> path = EnumObject.empty();
  public static EnumObject<BushType,EnlightenedBushBlock> enlightenedBush = EnumObject.empty();
  // flowers
  public static EnumObject<FlowerType,GrowableFlowerBlock> flower = EnumObject.empty();
  public static EnumObject<FlowerType,FlowerPotBlock> flowerPot = EnumObject.empty();

  // items
  public static Item glassDoorItem;
  public static Item redstoneBook;
  // emum
  public static DyeableItem coloredBook;

  // Tile Entities
  public static BlockEntityType<ShelfBlockEntity> shelfTileEntity;
  public static BlockEntityType<EnlightenedBushBlockEntity> enlightenedBushTileEntity;

  // Container Types
  public static MenuType<ShelfContainerMenu> shelfContainer;

  @SubscribeEvent
  void register(RegisterEvent event) {
    event.register(Registries.BLOCK, helper -> {
      BlockRegistryAdapter registry = new BlockRegistryAdapter(helper);

      // normal shelf uses a less regular naming
      BlockBehaviour.Properties shelfProps = Block.Properties.of().mapColor(MapColor.WOOD).ignitedByLava().instrument(NoteBlockInstrument.BASS).strength(2.0F, 5.0F).sound(SoundType.WOOD).noOcclusion();
      shelf = new EnumObject.Builder<ShelfType,ShelfBlock>(ShelfType.class)
          .put(ShelfType.NORMAL, registry.register(new ShelfBlock(shelfProps), "shelf"))
          .putAll(registry.registerEnum(type -> new ShelfBlock(shelfProps), ShelfType.FANCY, "shelf"))
          .build();
      rope = registry.register(new RopeBlock(Items.STICK,
              Block.Properties.of().mapColor(MapColor.PODZOL).ignitedByLava().sound(SoundType.WOOL).strength(0.5F)
      ), "rope");
      vine = registry.register(new RopeBlock(Items.BAMBOO,
              Block.Properties.of().mapColor(MapColor.PLANT).ignitedByLava().sound(SoundType.GRASS).strength(0.5F)
      ), "vine");
      // iron bars are climbable through a mixin (ClimbableIronBarsMixin), NeoForge cannot replace vanilla blocks

      BlockBehaviour.Properties glassDoorProps = Block.Properties.of().mapColor(MapColor.NONE).instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion();
      glassDoor = registry.register(new GlassDoorBlock(glassDoorProps), "glass_door");
      glassTrapdoor = registry.register(new TrapDoorBlock(GlassDoorBlock.GLASS_TYPE, glassDoorProps), "glass_trapdoor");

      mulch = registry.registerEnum(type -> new MulchBlock(
              Properties.of().mapColor(type.getColor()).ignitedByLava().instrument(NoteBlockInstrument.BASS).sound(SoundType.WET_GRASS).strength(0.6F)
      ), MulchType.values(), "mulch");
      path = registry.registerEnum(type -> new PathBlock(
              Block.Properties.of().mapColor(type.getColor()).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 10F), type.getShape()
      ), PathType.values(), "path");
      enlightenedBush = registry.registerEnum(type -> new EnlightenedBushBlock(
              Block.Properties.of().mapColor(type.getMapColor()).lightLevel(state -> 15).strength(0.2F).sound(SoundType.GRASS).noOcclusion(), type.getColor()
      ), BushType.values(), "enlightened_bush");

      // flowers, have no base name
      BlockBehaviour.Properties flowerProps = Block.Properties.of().mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY).strength(0F).sound(SoundType.GRASS).noCollission();
      flower = new EnumObject.Builder<FlowerType,GrowableFlowerBlock>(FlowerType.class)
          .put(FlowerType.CYAN, registry.register(new GrowableFlowerBlock(MobEffects.SLOW_FALLING, 4, null, flowerProps), "cyan_flower"))
          .put(FlowerType.SYRINGA, registry.register(new GrowableFlowerBlock(MobEffects.HUNGER, 8, (DoublePlantBlock)Blocks.LILAC, flowerProps), "syringa"))
          .put(FlowerType.PAEONIA, registry.register(new GrowableFlowerBlock(MobEffects.WATER_BREATHING, 5, (DoublePlantBlock)Blocks.PEONY, flowerProps), "paeonia"))
          .put(FlowerType.ROSE, registry.register(new GrowableFlowerBlock(MobEffects.DIG_SPEED, 7, (DoublePlantBlock)Blocks.ROSE_BUSH, flowerProps), "rose"))
          .build();

      // flower pots
      Supplier<FlowerPotBlock> emptyPot = () -> (FlowerPotBlock)Blocks.FLOWER_POT;
      FlowerPotBlock vanillaPot = (FlowerPotBlock)Blocks.FLOWER_POT;
      Block.Properties props = Block.Properties.ofFullCopy(Blocks.FLOWER_POT);
      flowerPot = registry.registerEnum(type -> {
        // create pot and register it with the vanilla pot.
        Block plant = flower.get(type);
        FlowerPotBlock pot = new FlowerPotBlock(emptyPot, () -> plant, props);
        vanillaPot.addPlant(BuiltInRegistries.BLOCK.getKey(plant), () -> pot);
        return pot;
      }, "potted", FlowerType.values());
    });
    event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
      BlockEntityTypeRegistryAdapter registry = new BlockEntityTypeRegistryAdapter(helper);

      shelfTileEntity = registry.register(ShelfBlockEntity::new, shelf, "bookshelf");
      enlightenedBushTileEntity = registry.register(EnlightenedBushBlockEntity::new, enlightenedBush, "enlightened_bush");
    });
    event.register(Registries.MENU, helper -> {
      ContainerTypeRegistryAdapter registry = new ContainerTypeRegistryAdapter(helper);
      shelfContainer = registry.registerType(ShelfContainerMenu::new, "shelf");
    });
    event.register(Registries.ITEM, helper -> {
      ItemRegistryAdapter registry = new ItemRegistryAdapter(helper);
      // common props
      Item.Properties props = new Item.Properties();

      coloredBook = registry.register(new DyeableItem(props), "colored_book");
      redstoneBook = registry.register(props, "redstone_book");

      // item blocks
      registry.registerBlockItem(shelf, block -> new ShelfItem(block, props));
      registry.registerBlockItem(rope, props);
      registry.registerBlockItem(vine, props);

      registry.registerBlockItem(mulch, props);
      registry.registerBlockItem(path, props);
      registry.registerBlockItem(flower, props);
      registry.registerBlockItem(enlightenedBush, props);

      glassDoorItem = registry.register(new DoubleHighBlockItem(glassDoor, props), BuiltInRegistries.BLOCK.getKey(glassDoor));
      registry.registerBlockItem(glassTrapdoor, props);
    });
  }

  @SubscribeEvent
  void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, shelfTileEntity, (shelf, side) -> shelf.getInventory());
  }

  @SubscribeEvent
  void gatherData(GatherDataEvent event) {
    DataGenerator gen = event.getGenerator();
    gen.addProvider(event.includeServer(), IInspirationsRecipeBuilder.named("Inspirations Recipes - Building", new BuildingRecipeProvider(gen.getPackOutput(), event.getLookupProvider())));
  }

  @SubscribeEvent
  void init(FMLCommonSetupEvent event) {
    event.enqueueWork(InspirationsBuilding::registerCompostables);
    // 1.20 had this listener static on the module, which is registered as an instance on the mod bus, so it never ran
    NeoForge.EVENT_BUS.addListener(InspirationsBuilding::loadLoot);
  }

  private static void loadLoot(LootTableLoadEvent event) {
    // Add the drops for the small flowers.
    flower.forEach((type, plant) -> {
      if (type != FlowerType.CYAN) {
        plant.injectLoot(event);
      }
    });
  }

  private static void registerCompostables() {
    for (Block bush : enlightenedBush.values()) {
      ComposterBlock.COMPOSTABLES.put(bush.asItem(), 0.3F);
    }
    ComposterBlock.COMPOSTABLES.put(vine.asItem(), 0.5F);
    for (Block plant : flower.values()) {
      ComposterBlock.COMPOSTABLES.put(plant.asItem(), 0.65F);
    }
  }
}
