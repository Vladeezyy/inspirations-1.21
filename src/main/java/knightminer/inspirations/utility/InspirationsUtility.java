package knightminer.inspirations.utility;


import knightminer.inspirations.common.ModuleBase;
import knightminer.inspirations.utility.block.CarpetedPressurePlateBlock;
import knightminer.inspirations.utility.block.CarpetedTrapdoorBlock;
import knightminer.inspirations.utility.block.CollectorBlock;
import knightminer.inspirations.utility.block.PipeBlock;
import knightminer.inspirations.utility.block.TorchLeverBlock;
import knightminer.inspirations.utility.block.TorchLeverWallBlock;
import knightminer.inspirations.utility.block.entity.CollectorBlockEntity;
import knightminer.inspirations.utility.block.entity.PipeBlockEntity;
import knightminer.inspirations.utility.block.menu.CollectorContainerMenu;
import knightminer.inspirations.utility.block.menu.PipeContainerMenu;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import knightminer.inspirations.common.registration.BlockEntityTypeRegistryAdapter;
import knightminer.inspirations.common.registration.BlockRegistryAdapter;
import knightminer.inspirations.common.registration.ContainerTypeRegistryAdapter;
import knightminer.inspirations.common.registration.ItemRegistryAdapter;
import knightminer.inspirations.common.registration.EnumObject;

public class InspirationsUtility extends ModuleBase {
  // blocks
  public static Block torchLeverWall;
  public static Block torchLeverFloor;
  public static Block soulLeverWall;
  public static Block soulLeverFloor;
  //public static Block bricksButton;
  //public static Block netherBricksButton;
  public static EnumObject<DyeColor,CarpetedTrapdoorBlock> carpetedTrapdoors = EnumObject.empty();
  public static EnumObject<DyeColor,CarpetedPressurePlateBlock> carpetedPressurePlates = EnumObject.empty();
  public static Block collector;
  public static Block pipe;

  // Items
  public static Item torchLeverItem;
  public static Item soulLeverItem;

  // Tile entities
  public static BlockEntityType<CollectorBlockEntity> tileCollector;
  public static BlockEntityType<PipeBlockEntity> tilePipe;

  // Inventory containers
  public static MenuType<CollectorContainerMenu> contCollector;
  public static MenuType<PipeContainerMenu> contPipe;

  @SubscribeEvent
  public void register(RegisterEvent event) {
    event.register(Registries.BLOCK, helper -> {
      BlockRegistryAdapter registry = new BlockRegistryAdapter(helper);

      torchLeverFloor = registry.make(() -> new TorchLeverBlock(
          BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH).sound(SoundType.WOOD),
          ParticleTypes.FLAME
      ), "torch_lever");
      torchLeverWall = registry.make(() -> new TorchLeverWallBlock(
          BlockBehaviour.Properties.ofFullCopy(Blocks.WALL_TORCH).overrideLootTable(torchLeverFloor.getLootTable()).overrideDescription(torchLeverFloor.getDescriptionId()),
          ParticleTypes.FLAME
      ), "wall_torch_lever");

      soulLeverFloor = registry.make(() -> new TorchLeverBlock(
          BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_TORCH),
          ParticleTypes.SOUL_FIRE_FLAME
      ), "soul_torch_lever");
      soulLeverWall = registry.make(() -> new TorchLeverWallBlock(
          BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_WALL_TORCH).overrideLootTable(soulLeverFloor.getLootTable()).overrideDescription(soulLeverFloor.getDescriptionId()),
          ParticleTypes.SOUL_FIRE_FLAME
      ), "wall_soul_torch_lever");

      //Block.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 10.0F).sound(SoundType.STONE).randomTicks()
      //bricksButton = registerBlock(r, new BricksButtonBlock(BricksButtonBlock.BRICK_BUTTON), "bricks_button");
      //netherBricksButton = registerBlock(r, new BricksButtonBlock(BricksButtonBlock.NETHER_BUTTON), "nether_bricks_button");

      carpetedTrapdoors = registry.registerEnum(color -> new CarpetedTrapdoorBlock(
        Block.Properties.of().mapColor(color).ignitedByLava().instrument(NoteBlockInstrument.BASS).strength(3.0F).sound(SoundType.WOOL)
      ), DyeColor.values(), "carpeted_trapdoor");
      carpetedPressurePlates = registry.registerEnum(color -> new CarpetedPressurePlateBlock(
        // named like the carpet on top, other mods (Jade) show it
        Block.Properties.of().mapColor(color).strength(0.5F).sound(SoundType.WOOL)
          .overrideDescription(String.format("block.minecraft.%s_carpet", color.getName())), color
      ), DyeColor.values(), "carpeted_pressure_plate");

      collector = registry.make(() -> new CollectorBlock(
        Block.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(3.5F).sound(SoundType.STONE)
      ), "collector");
      pipe = registry.make(() -> new PipeBlock(
        Block.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 8.0F).sound(SoundType.METAL)
      ), "pipe");
    });
    event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
      BlockEntityTypeRegistryAdapter registry = new BlockEntityTypeRegistryAdapter(helper);

      tileCollector = registry.register(CollectorBlockEntity::new, collector, "collector");
      tilePipe = registry.register(PipeBlockEntity::new, pipe, "pipe");
    });
    event.register(Registries.MENU, helper -> {
      ContainerTypeRegistryAdapter registry = new ContainerTypeRegistryAdapter(helper);

      contCollector = registry.registerType(CollectorContainerMenu::new, "collector");
      contPipe = registry.registerType(PipeContainerMenu::new, "pipe");
    });
    event.register(Registries.ITEM, helper -> {
      Item.Properties props = new Item.Properties();
      ItemRegistryAdapter registry = new ItemRegistryAdapter(helper);

      // itemblocks
      torchLeverItem = registry.registerBlockItem(torchLeverFloor, p -> new StandingAndWallBlockItem(torchLeverFloor, torchLeverWall, Direction.DOWN, props));
      soulLeverItem = registry.registerBlockItem(soulLeverFloor, p -> new StandingAndWallBlockItem(soulLeverFloor, soulLeverWall, Direction.DOWN, props));
      //registerBlockItem(r, bricksButton, ItemGroup.REDSTONE);
      //registerBlockItem(r, netherBricksButton, ItemGroup.REDSTONE);
      registry.registerDefaultBlockItem(carpetedTrapdoors);
      registry.registerDefaultBlockItem(collector);
      registry.registerDefaultBlockItem(pipe);
    });
  }

  @SubscribeEvent
  void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(Capabilities.Item.BLOCK, tileCollector, (collector, side) -> collector.getItemHandler());
    event.registerBlockEntity(Capabilities.Item.BLOCK, tilePipe, (pipe, side) -> pipe.getItemHandler());
  }

}
