package knightminer.inspirations.tweaks;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.InspirationsBuilding;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.component.Consumables;
import com.google.common.collect.ImmutableSet;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.common.InspirationsCommons;
import knightminer.inspirations.common.ModuleBase;
import knightminer.inspirations.tweaks.block.BlockCropBlock;
import knightminer.inspirations.tweaks.block.CactusCropBlock;
import knightminer.inspirations.tweaks.block.FittedCarpetBlock;
import knightminer.inspirations.tweaks.block.FlatCarpetBlock;
import knightminer.inspirations.tweaks.block.SugarCaneCropBlock;
import knightminer.inspirations.tweaks.block.WetHopperBlock;
import knightminer.inspirations.tweaks.util.SmoothGrowthListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import knightminer.inspirations.common.registration.BlockRegistryAdapter;
import knightminer.inspirations.common.registration.ItemRegistryAdapter;
import knightminer.inspirations.common.registration.EnumObject;

import java.util.Objects;

public class InspirationsTweaks extends ModuleBase {
  // blocks
  public static BlockCropBlock cactus;
  public static BlockCropBlock sugarCane;
  public static HopperBlock wetHopper;
  public static HopperBlock dryHopper;
  // enum
  public static EnumObject<DyeColor,FittedCarpetBlock> fitCarpets = EnumObject.empty();
  public static EnumObject<DyeColor,Block> flatCarpets = EnumObject.empty();

  // items
  public static Item sugarCaneSeeds;
  public static Item cactusSeeds;
  public static Item heartbeet;


  @SubscribeEvent
  void register(RegisterEvent event) {
    event.register(Registries.BLOCK, helper -> {
      BlockRegistryAdapter registry = new BlockRegistryAdapter(helper);

      // vanilla carpets turn into fitted carpets through a mixin (WoolCarpetBlockMixin), NeoForge cannot replace them
      EnumObject.Builder<DyeColor,Block> flatBuilder = new EnumObject.Builder<>(DyeColor.class);
      EnumObject.Builder<DyeColor,FittedCarpetBlock> fittedBuilder = new EnumObject.Builder<>(DyeColor.class);
      for (DyeColor color : DyeColor.values()) {
        Block original = InspirationsCommons.VANILLA_CARPETS.get(color);
        // named like the vanilla carpet
        Block.Properties props = Block.Properties.ofFullCopy(original).overrideDescription(original.getDescriptionId());
        flatBuilder.put(color, original);
        // bounding box messes with sprinting on stairs, so disable
        fittedBuilder.put(color, registry.make(() -> new FittedCarpetBlock(color, props.noCollision()), color.getSerializedName() + "_fitted_carpet"));
      }
      flatCarpets = flatBuilder.build();
      fitCarpets = fittedBuilder.build();

      // the vanilla hopper takes water through a mixin (HopperBlockMixin) and turns into this block
      dryHopper = (HopperBlock) Blocks.HOPPER;
      wetHopper = registry.make(() -> new WetHopperBlock(Block.Properties.ofFullCopy(Blocks.HOPPER).overrideDescription(Blocks.HOPPER.getDescriptionId())), "wet_hopper");

      cactus = registry.make(() -> new CactusCropBlock(() -> Blocks.CACTUS, BlockBehaviour.Properties.ofFullCopy(Blocks.CACTUS)), "cactus");
      sugarCane = registry.make(() -> new SugarCaneCropBlock(() -> Blocks.SUGAR_CANE, BlockBehaviour.Properties.ofFullCopy(Blocks.SUGAR_CANE)), "sugar_cane");
    });
    event.register(Registries.ITEM, helper -> {
      ItemRegistryAdapter registry = new ItemRegistryAdapter(helper);
      Item.Properties props = new Item.Properties();

      // items named as themselves, not as the block (ItemNameBlockItem before 1.21.2)
      cactusSeeds = registry.make(() -> new BlockItem(cactus, props), "cactus_seeds");
      sugarCaneSeeds = registry.make(() -> new BlockItem(sugarCane, props), "sugar_cane_seeds");
      heartbeet = registry.register(
        new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(2.4f).build(),
          Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 100), 1)).build()),
        "heartbeet");
    });
    event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
      // The wet hopper keeps the hopper block entity. It's an immutable set, so we need to replace it entirely.
      synchronized (BlockEntityTypes.HOPPER) {
        BlockEntityTypes.HOPPER.validBlocks = new ImmutableSet.Builder<Block>()
            .addAll(BlockEntityTypes.HOPPER.validBlocks)
            .add(wetHopper)
            .build();
      }
    });
  }

  @SubscribeEvent
  public void setup(FMLCommonSetupEvent event) {
    event.enqueueWork(this::registerDispenserBehavior);

    NeoForge.EVENT_BUS.addListener(new SmoothGrowthListener(Blocks.CACTUS, cactus));
    NeoForge.EVENT_BUS.addListener(new SmoothGrowthListener(Blocks.SUGAR_CANE, sugarCane));
    // 1.20 had this listener static on the module, which is registered as an instance on the mod bus, so it never ran
    NeoForge.EVENT_BUS.addListener(InspirationsTweaks::loadLoot);
  }




  private static void loadLoot(LootTableLoadEvent event) {
    addToVanillaLoot(event, "entities/cave_spider");
    addToVanillaLoot(event, "entities/skeleton");
  }

  private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();

  /** Compost chances are item components since 26.3; 80 % for heartbeets is the mod's own chance provider */
  @SubscribeEvent
  void defaultComponents(ModifyDefaultComponentsEvent event) {
    InspirationsBuilding.compostable(event, cactusSeeds, ContextIntProviders.COMPOSTABLE_LOW);
    InspirationsBuilding.compostable(event, sugarCaneSeeds, ContextIntProviders.COMPOSTABLE_LOW);
    InspirationsBuilding.compostable(event, heartbeet, ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Inspirations.getResource("compostable/heartbeet")));
  }

  private void registerDispenserBehavior() {
    DispenseItemBehavior behavior = (source, stack) -> {
      // 1.20 did not return here, so a disabled config both dropped an anvil and placed one
      if (!Config.dispensersPlaceAnvils.getAsBoolean()) {
        return DEFAULT.dispense(source, stack);
      }
      // get basic data
      Direction facing = source.state().getValue(DispenserBlock.FACING);
      Level world = source.level();
      BlockPos pos = source.pos().relative(facing);

      DirectionalPlaceContext context = new DirectionalPlaceContext(world, pos, facing, stack, facing.getOpposite());

      // place() gives CONSUME on the server, 1.20 compared with SUCCESS and also dropped a second anvil every time
      if (((BlockItem)stack.getItem()).place(context).consumesAction()) {
        return stack;
      } else {
        // if we cannot place it, toss the item
        return DEFAULT.dispense(source, stack);
      }
    };

    DispenserBlock.registerBehavior(Blocks.ANVIL, behavior);
    DispenserBlock.registerBehavior(Blocks.CHIPPED_ANVIL, behavior);
    DispenserBlock.registerBehavior(Blocks.DAMAGED_ANVIL, behavior);
  }
}
