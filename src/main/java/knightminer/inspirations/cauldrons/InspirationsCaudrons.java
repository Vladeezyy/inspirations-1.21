package knightminer.inspirations.cauldrons;

import knightminer.inspirations.library.InspirationsTags;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.cauldrons.block.BoilingFourLayerCauldronBlock;
import knightminer.inspirations.cauldrons.block.BoilingThreeLayerCauldronBlock;
import knightminer.inspirations.cauldrons.block.DyeCauldronBlock;
import knightminer.inspirations.cauldrons.block.FourLayerCauldronBlock;
import knightminer.inspirations.cauldrons.block.PotionCauldronBlock;
import knightminer.inspirations.cauldrons.block.SuspiciousStewCauldronBlock;
import knightminer.inspirations.cauldrons.block.entity.DyeCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.SuspiciousStewCauldronBlockEntity;
import knightminer.inspirations.cauldrons.interaction.DecreaseLayerCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.EmptyCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.FillCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.FirstCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.IncreaseLayerCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.TransformCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.DyeItemCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.DyeLeatherItemCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.DyeWaterCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.DyedBottleIntoDyeCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.DyedBottleIntoEmptyCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.DyedBottleIntoWaterCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.FillDyedBottleCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.MixDyeCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.dye.WaterBottleIntoDyeCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.potion.BrewingCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.potion.FillPotionCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.potion.PotionIntoEmptyInteraction;
import knightminer.inspirations.cauldrons.interaction.potion.PotionIntoPotionCauldron;
import knightminer.inspirations.cauldrons.interaction.potion.TipArrowCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.potion.WaterBottleIntoWaterInteraction;
import knightminer.inspirations.cauldrons.interaction.stew.DecreaseSuspiciousStewCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.stew.MixSuspiciousStewCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.stew.SuspiciousStewIntoEmptyCauldronInteraction;
import knightminer.inspirations.cauldrons.interaction.stew.SuspiciousStewingCauldronInteraction;
import knightminer.inspirations.cauldrons.item.EmptyBottleItem;
import knightminer.inspirations.cauldrons.item.MilkBottleItem;
import knightminer.inspirations.cauldrons.item.MixedDyedBottleItem;
import knightminer.inspirations.cauldrons.item.SimpleDyedBottleItem;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.common.ModuleBase;
import knightminer.inspirations.library.MiscUtil;
import knightminer.inspirations.library.recipe.cauldron.CauldronRegistry;
import knightminer.inspirations.tools.InspirationsTools;
import knightminer.inspirations.utility.InspirationsUtility;
import net.minecraft.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import java.util.Set;
import net.neoforged.neoforge.event.RegisterCauldronInteractionEvent;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import knightminer.inspirations.cauldrons.item.FluidContainerItem;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegisterEvent;
import knightminer.inspirations.common.registration.FluidBuilder;
import knightminer.inspirations.common.registration.BlockEntityTypeRegistryAdapter;
import knightminer.inspirations.common.registration.BlockRegistryAdapter;
import knightminer.inspirations.common.registration.FluidRegistryAdapter;
import knightminer.inspirations.common.registration.ItemRegistryAdapter;
import knightminer.inspirations.common.registration.RegistryAdapter;
import knightminer.inspirations.common.registration.EnumObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static knightminer.inspirations.library.recipe.cauldron.CauldronRegistry.ALL_CAULDRONS;
import static knightminer.inspirations.library.recipe.cauldron.CauldronRegistry.exactBlock;
import static knightminer.inspirations.library.recipe.cauldron.CauldronRegistry.fluidTag;
import static knightminer.inspirations.library.recipe.cauldron.CauldronRegistry.itemTag;

public class InspirationsCaudrons extends ModuleBase {
  /** Dispatchers of the cauldrons by id, registered for the block codecs (RegisterCauldronInteractionEvent) */
  private static final Map<String, CauldronInteraction.Dispatcher> DISPATCHERS = new java.util.LinkedHashMap<>();

  /** A new dispatcher {@code inspirations:<name>}; 1.21.1 named the interaction maps {@code inspirations_<name>} */
  private static CauldronInteraction.Dispatcher dispatcher(String name) {
    CauldronInteraction.Dispatcher dispatcher = new CauldronInteraction.Dispatcher();
    DISPATCHERS.put(name, dispatcher);
    return dispatcher;
  }

  /** The bucket interactions every vanilla cauldron has, package-private in 26.1 */
  private static void addDefaultInteractions(CauldronInteraction.Dispatcher dispatcher) {
    dispatcher.put(Items.LAVA_BUCKET, CauldronInteractions::fillLavaInteraction);
    dispatcher.put(Items.WATER_BUCKET, CauldronInteractions::fillWaterInteraction);
    dispatcher.put(Items.POWDER_SNOW_BUCKET, CauldronInteractions::fillPowderSnowInteraction);
  }

  @SubscribeEvent
  void registerDispatchers(RegisterCauldronInteractionEvent.Dispatcher event) {
    DISPATCHERS.forEach((name, dispatcher) -> event.register(Inspirations.getResource(name), dispatcher));
  }

  /** Interactions for the mushroom stew cauldron */
  public static final CauldronInteraction.Dispatcher MUSHROOM_STEW_CAULDRON_INTERACTIONS = dispatcher("mushroom_stew");
  /** Interactions for the beetroot soup cauldron */
  public static final CauldronInteraction.Dispatcher BEETROOT_SOUP_CAULDRON_INTERACTIONS = dispatcher("beetroot_soup");
  /** Interactions for the rabbit stew cauldron */
  public static final CauldronInteraction.Dispatcher RABBIT_STEW_CAULDRON_INTERACTIONS = dispatcher("rabbit_stew");
  /** Interactions for the potato soup cauldron */
  public static final CauldronInteraction.Dispatcher POTATO_SOUP_CAULDRON_INTERACTIONS = dispatcher("potato_soup");
  /** Interactions for the honey cauldron */
  public static final CauldronInteraction.Dispatcher HONEY_CAULDRON_INTERACTIONS = dispatcher("honey");
  /** Interactions for the milk cauldron */
  public static final CauldronInteraction.Dispatcher MILK_CAULDRON_INTERACTIONS = dispatcher("milk");
  /** Interactions for the dye cauldron */
  public static final CauldronInteraction.Dispatcher DYE_CAULDRON_INTERACTIONS = dispatcher("dye");
  /** Interactions for the dye cauldron */
  public static final CauldronInteraction.Dispatcher POTION_CAULDRON_INTERACTIONS = dispatcher("potion");
  /** Interactions for the suspicious stew cauldron */
  public static final CauldronInteraction.Dispatcher SUSPICIOUS_STEW_CAULDRON_INTERACTIONS = dispatcher("suspicious_stew");

  // blocks
  public static FourLayerCauldronBlock mushroomStewCauldron, beetrootSoupCauldron, rabbitStewCauldron, potatoSoupCauldron;
  public static FourLayerCauldronBlock honeyCauldron, milkCauldron, suspiciousStewCauldron;
  public static LayeredCauldronBlock dyeCauldron, potionCauldron;

  public static BlockEntityType<DyeCauldronBlockEntity> dyeCauldronEntity;
  public static BlockEntityType<PotionCauldronBlockEntity> potionCauldronEntity;
  public static BlockEntityType<SuspiciousStewCauldronBlockEntity> suspiciousStewCauldronEntity;

  /** Standard damage source for melting most mobs */
  public static final ResourceKey<DamageType> DAMAGE_BOIL = ResourceKey.create(Registries.DAMAGE_TYPE, Inspirations.getResource("boiling"));

  // items
  public static Item splashBottle;
  public static Item lingeringBottle;
  public static EnumObject<DyeColor,SimpleDyedBottleItem> simpleDyedWaterBottle = EnumObject.empty();
  public static MixedDyedBottleItem mixedDyedWaterBottle;
  public static Item potatoSoupItem;
  public static Item milkBottle;

  // fluids
  // mushroom
  public static FluidType mushroomStewType;
  public static BaseFlowingFluid mushroomStew;
  public static BucketItem mushroomStewBucket;
  public static LiquidBlock mushroomStewBlock;
  // beetroot
  public static FluidType beetrootSoupType;
  public static BaseFlowingFluid beetrootSoup;
  public static BucketItem beetrootSoupBucket;
  public static LiquidBlock beetrootSoupBlock;
  // rabbit
  public static FluidType rabbitStewType;
  public static BaseFlowingFluid rabbitStew;
  public static BucketItem rabbitStewBucket;
  public static LiquidBlock rabbitStewBlock;
  // potato
  public static FluidType potatoSoupType;
  public static BaseFlowingFluid potatoSoup;
  public static BucketItem potatoSoupBucket;
  public static LiquidBlock potatoSoupBlock;
  // honey
  public static FluidType honeyType;
  public static BaseFlowingFluid honey;
  public static BucketItem honeyBucket;
  public static LiquidBlock honeyFluidBlock;

  public static SimpleParticleType boilingParticle;

  public InspirationsCaudrons() {
    NeoForgeMod.enableMilkFluid();
  }

  @SubscribeEvent
  void register(RegisterEvent event) {
    event.register(NeoForgeRegistries.Keys.FLUID_TYPES, helper -> {
      RegistryAdapter<FluidType> adapter = new RegistryAdapter<>(helper);
      // textures are client extensions (CauldronsClientEvents), Mantle read them from JSON in 1.20
      mushroomStewType = adapter.register(new FluidType(fluidBuilder("mushroom_stew").temperature(373).viscosity(1200)), "mushroom_stew");
      beetrootSoupType = adapter.register(new FluidType(fluidBuilder("beetroot_soup").temperature(373).viscosity(1100)), "beetroot_soup");
      rabbitStewType = adapter.register(new FluidType(fluidBuilder("rabbit_stew").temperature(373).viscosity(1400)), "rabbit_stew");
      potatoSoupType = adapter.register(new FluidType(fluidBuilder("potato_soup").temperature(373).viscosity(4000)), "potato_soup");
      honeyType = adapter.register(new FluidType(fluidBuilder("honey").temperature(373).viscosity(4000)), "honey");
    });
    event.register(Registries.FLUID, helper -> {
      FluidRegistryAdapter adapter = new FluidRegistryAdapter(helper);

      mushroomStew = adapter.register(FluidBuilder.create(() -> mushroomStewType).block(() -> mushroomStewBlock).bucket(() -> mushroomStewBucket), "mushroom_stew");
      beetrootSoup = adapter.register(FluidBuilder.create(() -> beetrootSoupType).block(() -> beetrootSoupBlock).bucket(() -> beetrootSoupBucket), "beetroot_soup");
      rabbitStew = adapter.register(FluidBuilder.create(() -> rabbitStewType).block(() -> rabbitStewBlock).bucket(() -> rabbitStewBucket), "rabbit_stew");
      potatoSoup = adapter.register(FluidBuilder.create(() -> potatoSoupType).block(() -> potatoSoupBlock).bucket(() -> potatoSoupBucket), "potato_soup");
      honey = adapter.register(FluidBuilder.create(() -> honeyType).block(() -> honeyFluidBlock).bucket(() -> honeyBucket), "honey");
    });
    event.register(Registries.BLOCK, helper -> {
      BlockRegistryAdapter registry = new BlockRegistryAdapter(helper);

      BlockBehaviour.Properties cauldronProps = Properties.ofFullCopy(Blocks.CAULDRON);
      mushroomStewCauldron = registry.make(() -> new BoilingFourLayerCauldronBlock(cauldronProps, MUSHROOM_STEW_CAULDRON_INTERACTIONS), "mushroom_stew_cauldron");
      beetrootSoupCauldron = registry.make(() -> new BoilingFourLayerCauldronBlock(cauldronProps, BEETROOT_SOUP_CAULDRON_INTERACTIONS), "beetroot_soup_cauldron");
      rabbitStewCauldron = registry.make(() -> new BoilingFourLayerCauldronBlock(cauldronProps, RABBIT_STEW_CAULDRON_INTERACTIONS), "rabbit_stew_cauldron");
      potatoSoupCauldron = registry.make(() -> new BoilingFourLayerCauldronBlock(cauldronProps, POTATO_SOUP_CAULDRON_INTERACTIONS), "potato_soup_cauldron");
      honeyCauldron = registry.make(() -> new FourLayerCauldronBlock(cauldronProps, HONEY_CAULDRON_INTERACTIONS), "honey_cauldron");
      milkCauldron = registry.make(() -> new BoilingFourLayerCauldronBlock(cauldronProps, MILK_CAULDRON_INTERACTIONS), "milk_cauldron");

      mushroomStewBlock = registry.registerFluidBlock(() -> mushroomStew, MapColor.TERRACOTTA_LIGHT_GRAY, 0, "mushroom_stew");
      beetrootSoupBlock = registry.registerFluidBlock(() -> beetrootSoup, MapColor.NETHER, 0, "beetroot_soup");
      rabbitStewBlock = registry.registerFluidBlock(() -> rabbitStew, MapColor.PODZOL, 0, "rabbit_stew");
      potatoSoupBlock = registry.registerFluidBlock(() -> potatoSoup, MapColor.TERRACOTTA_WHITE, 0, "potato_soup");
      honeyFluidBlock = registry.registerFluidBlock(() -> honey, MapColor.COLOR_ORANGE, 0, "honey");

      dyeCauldron = registry.make(() -> new DyeCauldronBlock(cauldronProps), "dye_cauldron");
      potionCauldron = registry.make(() -> new PotionCauldronBlock(cauldronProps), "potion_cauldron");
      suspiciousStewCauldron = registry.make(() -> new SuspiciousStewCauldronBlock(cauldronProps), "suspicious_stew_cauldron");
      // boiling particles in vanilla water cauldrons come from a mixin (WaterCauldronMixin), NeoForge cannot replace the block
    });
    event.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
      BlockEntityTypeRegistryAdapter registry = new BlockEntityTypeRegistryAdapter(helper);

      dyeCauldronEntity = registry.register(DyeCauldronBlockEntity::new, dyeCauldron, "dye_cauldron");
      potionCauldronEntity = registry.register(PotionCauldronBlockEntity::new, potionCauldron, "potion_cauldron");
      suspiciousStewCauldronEntity = registry.register(SuspiciousStewCauldronBlockEntity::new, suspiciousStewCauldron, "suspicious_stew_cauldron");
    });
    event.register(Registries.ITEM, helper -> {
      ItemRegistryAdapter registry = new ItemRegistryAdapter(helper);

      // buckets
      mushroomStewBucket = registry.registerBucket(() -> mushroomStew, "mushroom_stew");
      beetrootSoupBucket = registry.registerBucket(() -> beetrootSoup, "beetroot_soup");
      rabbitStewBucket = registry.registerBucket(() -> rabbitStew, "rabbit_stew");
      potatoSoupBucket = registry.registerBucket(() -> potatoSoup, "potato_soup");
      honeyBucket = registry.registerBucket(() -> honey, "honey");

      // potato soup, drained as 250 mB of soup by fluid handlers (FluidContainerItem)
      potatoSoupItem = registry.make(() -> new Item(
        new Item.Properties().stacksTo(1).food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).build()).usingConvertsTo(Items.BOWL)
      ), "potato_soup");

      // empty bottles
      Item.Properties props = new Item.Properties();
      splashBottle = registry.make(() -> new EmptyBottleItem(props, () -> Items.SPLASH_POTION), "splash_bottle");
      lingeringBottle = registry.make(() -> new EmptyBottleItem(props, () -> Items.LINGERING_POTION), "lingering_bottle");
      Item.Properties bottleProps = new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE);
      milkBottle = registry.make(() -> new MilkBottleItem(bottleProps), "milk_bottle");

      // dyed bottles
      simpleDyedWaterBottle = registry.registerEnum(color -> new SimpleDyedBottleItem(bottleProps, color), DyeColor.values(), "dyed_bottle");
      mixedDyedWaterBottle = registry.make(() -> new MixedDyedBottleItem(bottleProps), "mixed_dyed_bottle");
    });
    event.register(Registries.PARTICLE_TYPE, helper -> {
      RegistryAdapter<ParticleType<?>> registry = new RegistryAdapter<>(helper);
      boilingParticle = registry.make(() -> new SimpleParticleType(false), "boiling");
    });
  }

  @SubscribeEvent
  void registerCapabilities(RegisterCapabilitiesEvent event) {
    // milk bottles and potato soup act as small fluid containers, like Mantle's constant containers of 1.20
    event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new FluidContainerItem(access, potatoSoupItem, new FluidStack(potatoSoup, FluidContainerItem.BOWL), Items.BOWL), potatoSoupItem);
    event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> NeoForgeMod.MILK.isBound() ? new FluidContainerItem(access, milkBottle, new FluidStack(NeoForgeMod.MILK.get(), FluidContainerItem.BOTTLE), Items.GLASS_BOTTLE) : null, milkBottle);
  }


  @SubscribeEvent
  void commonSetup(FMLCommonSetupEvent event) {
    // get a list of all cauldrons
    List<AbstractCauldronBlock> allCauldrons = new ArrayList<>();
    for (Block block : BuiltInRegistries.BLOCK) {
      if (block instanceof AbstractCauldronBlock cauldron) {
        allCauldrons.add(cauldron);
      }
    }

    // add cauldron interactions
    event.enqueueWork(() -> {
      // helper to add to all cauldrons
      // cauldrons share dispatchers since 26.1 (every layered water cauldron uses WATER), each is filled once
      Set<CauldronInteraction.Dispatcher> dispatchers = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
      for (AbstractCauldronBlock cauldron : allCauldrons) {
        if (cauldron.interactions != null) {
          dispatchers.add(cauldron.interactions);
        }
      }
      BiConsumer<Item,CauldronInteraction> addToAll = (item, interaction) -> {
        for (CauldronInteraction.Dispatcher dispatcher : dispatchers) {
          dispatcher.put(item, interaction);
        }
      };

      // default interactions - lets you replace cauldron contents with water/milk
      addDefaultInteractions(MUSHROOM_STEW_CAULDRON_INTERACTIONS);
      addDefaultInteractions(BEETROOT_SOUP_CAULDRON_INTERACTIONS);
      addDefaultInteractions(RABBIT_STEW_CAULDRON_INTERACTIONS);
      addDefaultInteractions(POTATO_SOUP_CAULDRON_INTERACTIONS);
      addDefaultInteractions(HONEY_CAULDRON_INTERACTIONS);
      addDefaultInteractions(MILK_CAULDRON_INTERACTIONS);
      addDefaultInteractions(DYE_CAULDRON_INTERACTIONS);
      addDefaultInteractions(POTION_CAULDRON_INTERACTIONS);
      addDefaultInteractions(SUSPICIOUS_STEW_CAULDRON_INTERACTIONS);

      // make concrete in a cauldron
      if (Config.cauldronConcrete.getAsBoolean()) {
        for (DyeColor dye : DyeColor.values()) {
          CauldronInteractions.WATER.put(getConcretePowder(dye).asItem(), new DecreaseLayerCauldronInteraction(getConcrete(dye), LayeredCauldronBlock.LEVEL));
        }
      }

      // clean the piston
      if (Config.cauldronCleanStickyPiston.getAsBoolean()) {
        CauldronInteractions.WATER.put(Items.STICKY_PISTON, new DecreaseLayerCauldronInteraction(Items.PISTON, LayeredCauldronBlock.LEVEL, SoundEvents.GENERIC_SPLASH));
      }

      // wet the sponge
      if (Config.cauldronWetSponge.getAsBoolean()) {
        CauldronInteraction spongeWet = new EmptyCauldronInteraction(Blocks.WET_SPONGE, SoundEvents.GRASS_PLACE);
        CauldronInteractions.WATER.put(Items.SPONGE, spongeWet);
        DYE_CAULDRON_INTERACTIONS.put(Items.SPONGE, spongeWet);
      }

      // wash the wool
      if (Config.cauldronWashWool.getAsBoolean()) {
        addToList(CauldronInteractions.WATER, new DecreaseLayerCauldronInteraction(Blocks.WOOL.pick(DyeColor.WHITE), LayeredCauldronBlock.LEVEL),
                  Items.WOOL.pick(DyeColor.ORANGE), Items.WOOL.pick(DyeColor.MAGENTA), Items.WOOL.pick(DyeColor.LIGHT_BLUE), Items.WOOL.pick(DyeColor.YELLOW), Items.WOOL.pick(DyeColor.LIME),
                  Items.WOOL.pick(DyeColor.PINK), Items.WOOL.pick(DyeColor.GRAY), Items.WOOL.pick(DyeColor.LIGHT_GRAY), Items.WOOL.pick(DyeColor.CYAN), Items.WOOL.pick(DyeColor.PURPLE),
                  Items.WOOL.pick(DyeColor.BLUE), Items.WOOL.pick(DyeColor.BROWN), Items.WOOL.pick(DyeColor.GREEN), Items.WOOL.pick(DyeColor.RED), Items.WOOL.pick(DyeColor.BLACK));
        // wash the bed
        addToList(CauldronInteractions.WATER, new DecreaseLayerCauldronInteraction(Blocks.BED.pick(DyeColor.WHITE), LayeredCauldronBlock.LEVEL),
                  Items.BED.pick(DyeColor.ORANGE), Items.BED.pick(DyeColor.MAGENTA), Items.BED.pick(DyeColor.LIGHT_BLUE), Items.BED.pick(DyeColor.YELLOW), Items.BED.pick(DyeColor.LIME),
                  Items.BED.pick(DyeColor.PINK), Items.BED.pick(DyeColor.GRAY), Items.BED.pick(DyeColor.LIGHT_GRAY), Items.BED.pick(DyeColor.CYAN), Items.BED.pick(DyeColor.PURPLE),
                  Items.BED.pick(DyeColor.BLUE), Items.BED.pick(DyeColor.BROWN), Items.BED.pick(DyeColor.GREEN), Items.BED.pick(DyeColor.RED), Items.BED.pick(DyeColor.BLACK));
        // wash the carpet
        addToList(CauldronInteractions.WATER, new DecreaseLayerCauldronInteraction(Blocks.CARPET.pick(DyeColor.WHITE), LayeredCauldronBlock.LEVEL),
                  Items.CARPET.pick(DyeColor.ORANGE), Items.CARPET.pick(DyeColor.MAGENTA), Items.CARPET.pick(DyeColor.LIGHT_BLUE), Items.CARPET.pick(DyeColor.YELLOW), Items.CARPET.pick(DyeColor.LIME),
                  Items.CARPET.pick(DyeColor.PINK), Items.CARPET.pick(DyeColor.GRAY), Items.CARPET.pick(DyeColor.LIGHT_GRAY), Items.CARPET.pick(DyeColor.CYAN), Items.CARPET.pick(DyeColor.PURPLE),
                  Items.CARPET.pick(DyeColor.BLUE), Items.CARPET.pick(DyeColor.BROWN), Items.CARPET.pick(DyeColor.GREEN), Items.CARPET.pick(DyeColor.RED), Items.CARPET.pick(DyeColor.BLACK));
        // wash the carpeted trapdoor
        addToList(CauldronInteractions.WATER, new DecreaseLayerCauldronInteraction(InspirationsUtility.carpetedTrapdoors.get(DyeColor.WHITE), LayeredCauldronBlock.LEVEL),
                  Arrays.stream(DyeColor.values()).filter(dye -> dye != DyeColor.WHITE).map(color -> InspirationsUtility.carpetedTrapdoors.get(color).asItem()).toArray(Item[]::new));

        // wash the compass
        CauldronInteractions.WATER.put(InspirationsTools.dimensionCompass, CauldronInteractions::dyedItemIteration);
      }

      // milk
      if (Config.enableCauldronMilk.getAsBoolean()) {
        // milk buckets
        addToAll.accept(Items.MILK_BUCKET, new FillCauldronInteraction(milkCauldron));
        MILK_CAULDRON_INTERACTIONS.put(Items.BUCKET, new EmptyCauldronInteraction(Items.MILK_BUCKET, SoundEvents.BUCKET_FILL));

        // milk bottles
        CauldronInteractions.EMPTY.put(milkBottle, new FillCauldronInteraction(milkCauldron, 1, Items.GLASS_BOTTLE));
        MILK_CAULDRON_INTERACTIONS.put(milkBottle, IncreaseLayerCauldronInteraction.fourLevel(Items.GLASS_BOTTLE));
        MILK_CAULDRON_INTERACTIONS.put(Items.GLASS_BOTTLE, new DecreaseLayerCauldronInteraction(milkBottle, FourLayerCauldronBlock.LEVEL));
      }

      // honey
      if (Config.enableCauldronHoney.getAsBoolean()) {
        // honey buckets
        CauldronInteraction fillHoney = new FillCauldronInteraction(honeyCauldron);
        addToAll.accept(honeyBucket, fillHoney);
        CauldronRegistry.register(ALL_CAULDRONS, fluidTag(InspirationsTags.Common.HONEY), fillHoney);
        HONEY_CAULDRON_INTERACTIONS.put(Items.BUCKET, new EmptyCauldronInteraction(honeyBucket, SoundEvents.BUCKET_FILL));

        // honey bottles
        CauldronInteractions.EMPTY.put(Items.HONEY_BOTTLE,   new FillCauldronInteraction(honeyCauldron, 1, Items.GLASS_BOTTLE));
        HONEY_CAULDRON_INTERACTIONS.put(Items.HONEY_BOTTLE, IncreaseLayerCauldronInteraction.fourLevel(Items.GLASS_BOTTLE));
        HONEY_CAULDRON_INTERACTIONS.put(Items.GLASS_BOTTLE, new DecreaseLayerCauldronInteraction(Items.HONEY_BOTTLE, FourLayerCauldronBlock.LEVEL));

        // honey blocks
        // not sure if I will bring back solid honey in cauldrons, for now just act like its liquid
        CauldronInteraction honeyToBlock = new EmptyCauldronInteraction(Blocks.HONEY_BLOCK, false, SoundEvents.HONEY_BLOCK_BREAK);
        CauldronInteraction honeyToSugar = new DecreaseLayerCauldronInteraction(() -> new ItemStack(Items.SUGAR, 3), FourLayerCauldronBlock.LEVEL, false, SoundEvents.HONEY_BLOCK_BREAK);
        HONEY_CAULDRON_INTERACTIONS.put(Items.AIR, new FirstCauldronInteraction(honeyToBlock, honeyToSugar));
        HONEY_CAULDRON_INTERACTIONS.put(Items.HONEY_BLOCK, honeyToBlock);
        HONEY_CAULDRON_INTERACTIONS.put(Items.SUGAR, honeyToSugar);
        CauldronInteractions.EMPTY.put(Items.HONEY_BLOCK, new FillCauldronInteraction(honeyCauldron, 4, Items.AIR, SoundEvents.HONEY_BLOCK_PLACE));
      }

      // soup
      if (Config.enableCauldronSoups.getAsBoolean()) {
        // mushroom
        CauldronInteraction fillMushroomStew = new FillCauldronInteraction(mushroomStewCauldron);
        addToAll.accept(mushroomStewBucket, fillMushroomStew);
        CauldronRegistry.register(ALL_CAULDRONS, fluidTag(InspirationsTags.Common.MUSHROOM_STEW), fillMushroomStew);
        // potato
        addToAll.accept(potatoSoupBucket, new FillCauldronInteraction(potatoSoupCauldron));
        // rabbit
        CauldronInteraction fillRabbitStew = new FillCauldronInteraction(rabbitStewCauldron);
        addToAll.accept(rabbitStewBucket, fillRabbitStew);
        CauldronRegistry.register(ALL_CAULDRONS, fluidTag(InspirationsTags.Common.RABBIT_STEW), fillRabbitStew);
        // beetroot
        CauldronInteraction fillBeetrootSoup = new FillCauldronInteraction(beetrootSoupCauldron);
        addToAll.accept(beetrootSoupBucket, fillBeetrootSoup);
        CauldronRegistry.register(ALL_CAULDRONS, fluidTag(InspirationsTags.Common.BEETROOT_SOUP), fillBeetrootSoup);
        // empty buckets
        MUSHROOM_STEW_CAULDRON_INTERACTIONS.put(Items.BUCKET, new EmptyCauldronInteraction(mushroomStewBucket, SoundEvents.BUCKET_FILL));
        POTATO_SOUP_CAULDRON_INTERACTIONS.put(Items.BUCKET, new EmptyCauldronInteraction(potatoSoupBucket,   SoundEvents.BUCKET_FILL));
        RABBIT_STEW_CAULDRON_INTERACTIONS.put(Items.BUCKET, new EmptyCauldronInteraction(rabbitStewBucket,   SoundEvents.BUCKET_FILL));
        BEETROOT_SOUP_CAULDRON_INTERACTIONS.put(Items.BUCKET, new EmptyCauldronInteraction(beetrootSoupBucket, SoundEvents.BUCKET_FILL));

        // empty bowls
        CauldronInteractions.EMPTY.put(Items.MUSHROOM_STEW, new FillCauldronInteraction(mushroomStewCauldron, 1, Items.BOWL));
        CauldronInteractions.EMPTY.put(potatoSoupItem,      new FillCauldronInteraction(potatoSoupCauldron,   1, Items.BOWL));
        CauldronInteractions.EMPTY.put(Items.RABBIT_STEW,   new FillCauldronInteraction(rabbitStewCauldron,   1, Items.BOWL));
        CauldronInteractions.EMPTY.put(Items.BEETROOT_SOUP, new FillCauldronInteraction(beetrootSoupCauldron, 1, Items.BOWL));
        IncreaseLayerCauldronInteraction increaseIntoBowl = IncreaseLayerCauldronInteraction.fourLevel(Items.BOWL);
        MUSHROOM_STEW_CAULDRON_INTERACTIONS.put(Items.MUSHROOM_STEW, increaseIntoBowl);
        POTATO_SOUP_CAULDRON_INTERACTIONS.put(potatoSoupItem,      increaseIntoBowl);
        RABBIT_STEW_CAULDRON_INTERACTIONS.put(Items.RABBIT_STEW,   increaseIntoBowl);
        BEETROOT_SOUP_CAULDRON_INTERACTIONS.put(Items.BEETROOT_SOUP, increaseIntoBowl);

        // fill bowls
        MUSHROOM_STEW_CAULDRON_INTERACTIONS.put(Items.BOWL, new DecreaseLayerCauldronInteraction(Items.MUSHROOM_STEW, FourLayerCauldronBlock.LEVEL));
        POTATO_SOUP_CAULDRON_INTERACTIONS.put(Items.BOWL, new DecreaseLayerCauldronInteraction(potatoSoupItem,      FourLayerCauldronBlock.LEVEL));
        RABBIT_STEW_CAULDRON_INTERACTIONS.put(Items.BOWL, new DecreaseLayerCauldronInteraction(Items.RABBIT_STEW,   FourLayerCauldronBlock.LEVEL));
        BEETROOT_SOUP_CAULDRON_INTERACTIONS.put(Items.BOWL, new DecreaseLayerCauldronInteraction(Items.BEETROOT_SOUP, FourLayerCauldronBlock.LEVEL));

        // making the soup
        // mushroom: slight discount (6 mushrooms for 4 bowls) and can use either mushroom (but not both)
        // use water if milk is disabled
        if (Config.enableCauldronMilk.getAsBoolean()) {
          CauldronInteraction mushroomTransform = new TransformCauldronInteraction(true, 2, FourLayerCauldronBlock.LEVEL, mushroomStewCauldron);
          MILK_CAULDRON_INTERACTIONS.put(Items.BROWN_MUSHROOM, mushroomTransform);
          MILK_CAULDRON_INTERACTIONS.put(Items.RED_MUSHROOM, mushroomTransform);
          CauldronRegistry.register(exactBlock(milkCauldron), itemTag(Tags.Items.MUSHROOMS), mushroomTransform);
        } else {
          CauldronInteraction mushroomTransform = new TransformCauldronInteraction(true, 2, LayeredCauldronBlock.LEVEL, mushroomStewCauldron);
          CauldronInteractions.WATER.put(Items.BROWN_MUSHROOM, mushroomTransform);
          CauldronInteractions.WATER.put(Items.RED_MUSHROOM, mushroomTransform);
          CauldronRegistry.register(exactBlock(Blocks.WATER_CAULDRON), itemTag(Tags.Items.MUSHROOMS), mushroomTransform);
        }
        // potato: slight discount, 4 bowls only costs 6 potatoes instead of 8. Uses 2 more mushrooms
        MUSHROOM_STEW_CAULDRON_INTERACTIONS.put(Items.BAKED_POTATO, new TransformCauldronInteraction(true, 2, FourLayerCauldronBlock.LEVEL, potatoSoupCauldron));
        // rabbit: slight discount, 4 bowls only costs 3 rabbit instead of 4 and does not need carrot. Uses 2 more mushroom
        POTATO_SOUP_CAULDRON_INTERACTIONS.put(Items.COOKED_RABBIT, new TransformCauldronInteraction(true, 1, FourLayerCauldronBlock.LEVEL, rabbitStewCauldron));
        // rabbit: slight discount, 4 bowls only costs 18 beetroot instead of 24
        CauldronInteractions.WATER.put(Items.BEETROOT, new TransformCauldronInteraction(true, 6, LayeredCauldronBlock.LEVEL, beetrootSoupCauldron));

        // suspicious stew
        CauldronInteractions.EMPTY.put(Items.SUSPICIOUS_STEW, SuspiciousStewIntoEmptyCauldronInteraction.INSTANCE);
        SUSPICIOUS_STEW_CAULDRON_INTERACTIONS.put(Items.BOWL, DecreaseSuspiciousStewCauldronInteraction.INSTANCE);
        SUSPICIOUS_STEW_CAULDRON_INTERACTIONS.put(Items.SUSPICIOUS_STEW, MixSuspiciousStewCauldronInteraction.INSTANCE);
        CauldronRegistry.register(exactBlock(mushroomStewCauldron), itemTag(BlockItemTags.SMALL_FLOWERS.item()), SuspiciousStewingCauldronInteraction.INSTANCE);
      }

      // dye cauldrons
      if (Config.enableCauldronDyeing.getAsBoolean()) {
        for (DyeColor dye : DyeColor.values()) {
          Item dyeItem = switch (dye) {
            case WHITE -> Items.DYE.pick(DyeColor.WHITE);
            case ORANGE -> Items.DYE.pick(DyeColor.ORANGE);
            case MAGENTA -> Items.DYE.pick(DyeColor.MAGENTA);
            case LIGHT_BLUE -> Items.DYE.pick(DyeColor.LIGHT_BLUE);
            case YELLOW -> Items.DYE.pick(DyeColor.YELLOW);
            case LIME -> Items.DYE.pick(DyeColor.LIME);
            case PINK -> Items.DYE.pick(DyeColor.PINK);
            case GRAY -> Items.DYE.pick(DyeColor.GRAY);
            case LIGHT_GRAY -> Items.DYE.pick(DyeColor.LIGHT_GRAY);
            case CYAN -> Items.DYE.pick(DyeColor.CYAN);
            case PURPLE -> Items.DYE.pick(DyeColor.PURPLE);
            case BLUE -> Items.DYE.pick(DyeColor.BLUE);
            case BROWN -> Items.DYE.pick(DyeColor.BROWN);
            case GREEN -> Items.DYE.pick(DyeColor.GREEN);
            case RED -> Items.DYE.pick(DyeColor.RED);
            case BLACK -> Items.DYE.pick(DyeColor.BLACK);
          };

          // dyes into cauldrons
          CauldronInteractions.WATER.put(dyeItem, new DyeWaterCauldronInteraction(dye));
          DYE_CAULDRON_INTERACTIONS.put(dyeItem, new MixDyeCauldronInteraction(dye));

          // dyed bottles into cauldrons
          int color = MiscUtil.getColor(dye);
          CauldronInteractions.EMPTY.put(simpleDyedWaterBottle.get(dye), new DyedBottleIntoEmptyCauldronInteraction(color));
          CauldronInteractions.WATER.put(simpleDyedWaterBottle.get(dye), new DyedBottleIntoWaterCauldronInteraction(color));
          DYE_CAULDRON_INTERACTIONS.put(simpleDyedWaterBottle.get(dye), new DyedBottleIntoDyeCauldronInteraction(color));
        }

        // mixed bottle into cauldrons
        CauldronInteractions.EMPTY.put(mixedDyedWaterBottle, new DyedBottleIntoEmptyCauldronInteraction(null));
        CauldronInteractions.WATER.put(mixedDyedWaterBottle, new DyedBottleIntoWaterCauldronInteraction(null));
        DYE_CAULDRON_INTERACTIONS.put(mixedDyedWaterBottle, new DyedBottleIntoDyeCauldronInteraction(null));

        // water into dyed cauldrons
        DYE_CAULDRON_INTERACTIONS.put(Items.POTION, new WaterBottleIntoDyeCauldronInteraction(Items.GLASS_BOTTLE, 0x808080));
        DYE_CAULDRON_INTERACTIONS.put(Items.SPLASH_POTION, new WaterBottleIntoDyeCauldronInteraction(splashBottle, 0x808080));
        DYE_CAULDRON_INTERACTIONS.put(Items.LINGERING_POTION, new WaterBottleIntoDyeCauldronInteraction(lingeringBottle, 0x808080));

        // fill the bottle
        DYE_CAULDRON_INTERACTIONS.put(Items.GLASS_BOTTLE, FillDyedBottleCauldronInteraction.INSTANCE);

        // dye the wool
        addToList(DYE_CAULDRON_INTERACTIONS, new DyeItemCauldronInteraction(color -> switch (color) {
                    case WHITE      -> Items.WOOL.pick(DyeColor.WHITE);
                    case ORANGE     -> Items.WOOL.pick(DyeColor.ORANGE);
                    case MAGENTA    -> Items.WOOL.pick(DyeColor.MAGENTA);
                    case LIGHT_BLUE -> Items.WOOL.pick(DyeColor.LIGHT_BLUE);
                    case YELLOW     -> Items.WOOL.pick(DyeColor.YELLOW);
                    case LIME       -> Items.WOOL.pick(DyeColor.LIME);
                    case PINK       -> Items.WOOL.pick(DyeColor.PINK);
                    case GRAY       -> Items.WOOL.pick(DyeColor.GRAY);
                    case LIGHT_GRAY -> Items.WOOL.pick(DyeColor.LIGHT_GRAY);
                    case CYAN       -> Items.WOOL.pick(DyeColor.CYAN);
                    case PURPLE     -> Items.WOOL.pick(DyeColor.PURPLE);
                    case BLUE       -> Items.WOOL.pick(DyeColor.BLUE);
                    case BROWN      -> Items.WOOL.pick(DyeColor.BROWN);
                    case GREEN      -> Items.WOOL.pick(DyeColor.GREEN);
                    case RED        -> Items.WOOL.pick(DyeColor.RED);
                    case BLACK      -> Items.WOOL.pick(DyeColor.BLACK);
                  }), Items.WOOL.pick(DyeColor.WHITE), Items.WOOL.pick(DyeColor.ORANGE), Items.WOOL.pick(DyeColor.MAGENTA), Items.WOOL.pick(DyeColor.LIGHT_BLUE), Items.WOOL.pick(DyeColor.YELLOW), Items.WOOL.pick(DyeColor.LIME), Items.WOOL.pick(DyeColor.PINK), Items.WOOL.pick(DyeColor.GRAY),
                  Items.WOOL.pick(DyeColor.LIGHT_GRAY), Items.WOOL.pick(DyeColor.CYAN), Items.WOOL.pick(DyeColor.PURPLE), Items.WOOL.pick(DyeColor.BLUE), Items.WOOL.pick(DyeColor.BROWN), Items.WOOL.pick(DyeColor.GREEN), Items.WOOL.pick(DyeColor.RED), Items.WOOL.pick(DyeColor.BLACK));
        // dye the bed
        addToList(DYE_CAULDRON_INTERACTIONS, new DyeItemCauldronInteraction(color -> switch (color) {
                    case WHITE      -> Items.BED.pick(DyeColor.WHITE);
                    case ORANGE     -> Items.BED.pick(DyeColor.ORANGE);
                    case MAGENTA    -> Items.BED.pick(DyeColor.MAGENTA);
                    case LIGHT_BLUE -> Items.BED.pick(DyeColor.LIGHT_BLUE);
                    case YELLOW     -> Items.BED.pick(DyeColor.YELLOW);
                    case LIME       -> Items.BED.pick(DyeColor.LIME);
                    case PINK       -> Items.BED.pick(DyeColor.PINK);
                    case GRAY       -> Items.BED.pick(DyeColor.GRAY);
                    case LIGHT_GRAY -> Items.BED.pick(DyeColor.LIGHT_GRAY);
                    case CYAN       -> Items.BED.pick(DyeColor.CYAN);
                    case PURPLE     -> Items.BED.pick(DyeColor.PURPLE);
                    case BLUE       -> Items.BED.pick(DyeColor.BLUE);
                    case BROWN      -> Items.BED.pick(DyeColor.BROWN);
                    case GREEN      -> Items.BED.pick(DyeColor.GREEN);
                    case RED        -> Items.BED.pick(DyeColor.RED);
                    case BLACK      -> Items.BED.pick(DyeColor.BLACK);
                  }), Items.BED.pick(DyeColor.WHITE), Items.BED.pick(DyeColor.ORANGE), Items.BED.pick(DyeColor.MAGENTA), Items.BED.pick(DyeColor.LIGHT_BLUE), Items.BED.pick(DyeColor.YELLOW), Items.BED.pick(DyeColor.LIME), Items.BED.pick(DyeColor.PINK), Items.BED.pick(DyeColor.GRAY),
                  Items.BED.pick(DyeColor.LIGHT_GRAY), Items.BED.pick(DyeColor.CYAN), Items.BED.pick(DyeColor.PURPLE), Items.BED.pick(DyeColor.BLUE), Items.BED.pick(DyeColor.BROWN), Items.BED.pick(DyeColor.GREEN), Items.BED.pick(DyeColor.RED), Items.BED.pick(DyeColor.BLACK));
        // dye the carpet
        addToList(DYE_CAULDRON_INTERACTIONS, new DyeItemCauldronInteraction(color -> switch (color) {
                    case WHITE      -> Items.CARPET.pick(DyeColor.WHITE);
                    case ORANGE     -> Items.CARPET.pick(DyeColor.ORANGE);
                    case MAGENTA    -> Items.CARPET.pick(DyeColor.MAGENTA);
                    case LIGHT_BLUE -> Items.CARPET.pick(DyeColor.LIGHT_BLUE);
                    case YELLOW     -> Items.CARPET.pick(DyeColor.YELLOW);
                    case LIME       -> Items.CARPET.pick(DyeColor.LIME);
                    case PINK       -> Items.CARPET.pick(DyeColor.PINK);
                    case GRAY       -> Items.CARPET.pick(DyeColor.GRAY);
                    case LIGHT_GRAY -> Items.CARPET.pick(DyeColor.LIGHT_GRAY);
                    case CYAN       -> Items.CARPET.pick(DyeColor.CYAN);
                    case PURPLE     -> Items.CARPET.pick(DyeColor.PURPLE);
                    case BLUE       -> Items.CARPET.pick(DyeColor.BLUE);
                    case BROWN      -> Items.CARPET.pick(DyeColor.BROWN);
                    case GREEN      -> Items.CARPET.pick(DyeColor.GREEN);
                    case RED        -> Items.CARPET.pick(DyeColor.RED);
                    case BLACK      -> Items.CARPET.pick(DyeColor.BLACK);
                  }), Items.CARPET.pick(DyeColor.WHITE), Items.CARPET.pick(DyeColor.ORANGE), Items.CARPET.pick(DyeColor.MAGENTA), Items.CARPET.pick(DyeColor.LIGHT_BLUE), Items.CARPET.pick(DyeColor.YELLOW), Items.CARPET.pick(DyeColor.LIME), Items.CARPET.pick(DyeColor.PINK), Items.CARPET.pick(DyeColor.GRAY),
                  Items.CARPET.pick(DyeColor.LIGHT_GRAY), Items.CARPET.pick(DyeColor.CYAN), Items.CARPET.pick(DyeColor.PURPLE), Items.CARPET.pick(DyeColor.BLUE), Items.CARPET.pick(DyeColor.BROWN), Items.CARPET.pick(DyeColor.GREEN), Items.CARPET.pick(DyeColor.RED), Items.CARPET.pick(DyeColor.BLACK));
        // dye the carpeted trapdoor
        addToList(DYE_CAULDRON_INTERACTIONS, new DyeItemCauldronInteraction(dye -> InspirationsUtility.carpetedTrapdoors.get(dye)), InspirationsUtility.carpetedTrapdoors.values().stream().map(ItemLike::asItem).toArray(Item[]::new));
        // dye the shulker box
        addToList(DYE_CAULDRON_INTERACTIONS, new DyeItemCauldronInteraction(color -> switch (color) {
                    case WHITE      -> Items.DYED_SHULKER_BOX.pick(DyeColor.WHITE);
                    case ORANGE     -> Items.DYED_SHULKER_BOX.pick(DyeColor.ORANGE);
                    case MAGENTA    -> Items.DYED_SHULKER_BOX.pick(DyeColor.MAGENTA);
                    case LIGHT_BLUE -> Items.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_BLUE);
                    case YELLOW     -> Items.DYED_SHULKER_BOX.pick(DyeColor.YELLOW);
                    case LIME       -> Items.DYED_SHULKER_BOX.pick(DyeColor.LIME);
                    case PINK       -> Items.DYED_SHULKER_BOX.pick(DyeColor.PINK);
                    case GRAY       -> Items.DYED_SHULKER_BOX.pick(DyeColor.GRAY);
                    case LIGHT_GRAY -> Items.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_GRAY);
                    case CYAN       -> Items.DYED_SHULKER_BOX.pick(DyeColor.CYAN);
                    case PURPLE     -> Items.DYED_SHULKER_BOX.pick(DyeColor.PURPLE);
                    case BLUE       -> Items.DYED_SHULKER_BOX.pick(DyeColor.BLUE);
                    case BROWN      -> Items.DYED_SHULKER_BOX.pick(DyeColor.BROWN);
                    case GREEN      -> Items.DYED_SHULKER_BOX.pick(DyeColor.GREEN);
                    case RED        -> Items.DYED_SHULKER_BOX.pick(DyeColor.RED);
                    case BLACK      -> Items.DYED_SHULKER_BOX.pick(DyeColor.BLACK);
                  }, true), Items.DYED_SHULKER_BOX.pick(DyeColor.WHITE), Items.DYED_SHULKER_BOX.pick(DyeColor.ORANGE), Items.DYED_SHULKER_BOX.pick(DyeColor.MAGENTA), Items.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_BLUE), Items.DYED_SHULKER_BOX.pick(DyeColor.YELLOW), Items.DYED_SHULKER_BOX.pick(DyeColor.LIME), Items.DYED_SHULKER_BOX.pick(DyeColor.PINK), Items.DYED_SHULKER_BOX.pick(DyeColor.GRAY),
                  Items.DYED_SHULKER_BOX.pick(DyeColor.LIGHT_GRAY), Items.DYED_SHULKER_BOX.pick(DyeColor.CYAN), Items.DYED_SHULKER_BOX.pick(DyeColor.PURPLE), Items.DYED_SHULKER_BOX.pick(DyeColor.BLUE), Items.DYED_SHULKER_BOX.pick(DyeColor.BROWN), Items.DYED_SHULKER_BOX.pick(DyeColor.GREEN), Items.DYED_SHULKER_BOX.pick(DyeColor.RED), Items.DYED_SHULKER_BOX.pick(DyeColor.BLACK));
        // dye the leather, any item whose dye a cauldron removes (the dyeable tag before 26.1; 1.20 had a dyeable item
        // interface; tags are not loaded yet here)
        CauldronRegistry.register(exactBlock(dyeCauldron), itemTag(ItemTags.CAULDRON_CAN_REMOVE_DYE), DyeLeatherItemCauldronInteraction.INSTANCE);
      }

      // potion cauldrons
      if (Config.enableCauldronPotions.getAsBoolean()) {
        // brewing bottles is registered in registerBrewing, 1.21 builds the brewing recipes in an event

        // fill the potion
        POTION_CAULDRON_INTERACTIONS.put(Items.GLASS_BOTTLE, new FillPotionCauldronInteraction(Items.POTION));
        // splash
        CauldronInteraction fillSplashPotion = new FillPotionCauldronInteraction(Items.SPLASH_POTION);
        POTION_CAULDRON_INTERACTIONS.put(splashBottle, fillSplashPotion);
        CauldronRegistry.register(exactBlock(potionCauldron), itemTag(InspirationsTags.Common.SPLASH_BOTTLE), fillSplashPotion);
        // lingering
        CauldronInteraction fillLingeringBottle = new FillPotionCauldronInteraction(Items.LINGERING_POTION);
        POTION_CAULDRON_INTERACTIONS.put(lingeringBottle, fillLingeringBottle);
        CauldronRegistry.register(exactBlock(potionCauldron), itemTag(InspirationsTags.Common.LINGERING_BOTTLE), fillLingeringBottle);

        // drain the potion
        POTION_CAULDRON_INTERACTIONS.put(Items.POTION, new PotionIntoPotionCauldron(Items.GLASS_BOTTLE));
        POTION_CAULDRON_INTERACTIONS.put(Items.SPLASH_POTION, new PotionIntoPotionCauldron(splashBottle));
        POTION_CAULDRON_INTERACTIONS.put(Items.LINGERING_POTION, new PotionIntoPotionCauldron(lingeringBottle));
        // drain the potion into empty, note this replaces the vanilla one, but it's okay as we still make water cauldrons
        CauldronInteractions.EMPTY.put(Items.POTION, new PotionIntoEmptyInteraction(Items.GLASS_BOTTLE));
        CauldronInteractions.EMPTY.put(Items.SPLASH_POTION, new PotionIntoEmptyInteraction(splashBottle));
        CauldronInteractions.EMPTY.put(Items.LINGERING_POTION, new PotionIntoEmptyInteraction(lingeringBottle));

        // water cauldrons work with potions, but not splash or lingering
        CauldronInteractions.WATER.put(Items.SPLASH_POTION, new WaterBottleIntoWaterInteraction(splashBottle));
        CauldronInteractions.WATER.put(Items.LINGERING_POTION, new WaterBottleIntoWaterInteraction(lingeringBottle));
        CauldronInteractions.WATER.put(splashBottle, new DecreaseLayerCauldronInteraction(() -> PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WATER), LayeredCauldronBlock.LEVEL, true, SoundEvents.BOTTLE_FILL));
        CauldronInteractions.WATER.put(lingeringBottle, new DecreaseLayerCauldronInteraction(() -> PotionContents.createItemStack(Items.LINGERING_POTION, Potions.WATER), LayeredCauldronBlock.LEVEL, true, SoundEvents.BOTTLE_FILL));

        // make the tipped arrows, no water arrows tho
        if (Config.cauldronTipArrows.getAsBoolean()) {
          POTION_CAULDRON_INTERACTIONS.put(Items.ARROW, TipArrowCauldronInteraction.INSTANCE);
        }

        // potion brewing
        if (Config.cauldronBrewing.getAsBoolean()) {
          CauldronRegistry.register(exactBlock(Blocks.WATER_CAULDRON), CauldronRegistry.ALL_ITEMS, new BrewingCauldronInteraction(Potions.WATER));
          CauldronRegistry.register(exactBlock(potionCauldron), CauldronRegistry.ALL_ITEMS, new BrewingCauldronInteraction(null));
        }
      }

      Map<BlockState, Holder<PoiType>> map = GameData.getBlockStatePointOfInterestTypeMap();
      register(map, PoiTypes.LEATHERWORKER,
               honeyCauldron, mushroomStewCauldron, potatoSoupCauldron, beetrootSoupCauldron, rabbitStewCauldron,
               dyeCauldron, potionCauldron, suspiciousStewCauldron);
    });
  }


  /* Helpers */

  private static void register(Map<BlockState, Holder<PoiType>> map, ResourceKey<PoiType> key, Block... blocks) {
    Holder<PoiType> poi = BuiltInRegistries.POINT_OF_INTEREST_TYPE.get(key).orElse(null);
    if (poi != null) {
      for (Block block : blocks) {
        if (block != null) {
          for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            map.put(state, poi);
          }
        }
      }
    }
  }

  /** Creates a fluid builder */
  private static FluidType.Properties fluidBuilder(String name) {
    return FluidType.Properties.create()
                               .descriptionId(Util.makeDescriptionId("fluid", Inspirations.getResource(name)))
                               .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                               .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                               .motionScale(0.0023333333333333335D)
                               .canExtinguish(true);
  }

  /** Gets concrete powder for the given color */
  public static Block getConcretePowder(DyeColor color) {
    return switch(color) {
      case WHITE      -> Blocks.CONCRETE_POWDER.pick(DyeColor.WHITE);
      case ORANGE     -> Blocks.CONCRETE_POWDER.pick(DyeColor.ORANGE);
      case MAGENTA    -> Blocks.CONCRETE_POWDER.pick(DyeColor.MAGENTA);
      case LIGHT_BLUE -> Blocks.CONCRETE_POWDER.pick(DyeColor.LIGHT_BLUE);
      case YELLOW     -> Blocks.CONCRETE_POWDER.pick(DyeColor.YELLOW);
      case LIME       -> Blocks.CONCRETE_POWDER.pick(DyeColor.LIME);
      case PINK       -> Blocks.CONCRETE_POWDER.pick(DyeColor.PINK);
      case GRAY       -> Blocks.CONCRETE_POWDER.pick(DyeColor.GRAY);
      case LIGHT_GRAY -> Blocks.CONCRETE_POWDER.pick(DyeColor.LIGHT_GRAY);
      case CYAN       -> Blocks.CONCRETE_POWDER.pick(DyeColor.CYAN);
      case PURPLE     -> Blocks.CONCRETE_POWDER.pick(DyeColor.PURPLE);
      case BLUE       -> Blocks.CONCRETE_POWDER.pick(DyeColor.BLUE);
      case BROWN      -> Blocks.CONCRETE_POWDER.pick(DyeColor.BROWN);
      case GREEN      -> Blocks.CONCRETE_POWDER.pick(DyeColor.GREEN);
      case RED        -> Blocks.CONCRETE_POWDER.pick(DyeColor.RED);
      case BLACK      -> Blocks.CONCRETE_POWDER.pick(DyeColor.BLACK);
    };
  }

  /** Gets concrete for the given color */
  public static Block getConcrete(DyeColor color) {
    return switch(color) {
      case WHITE      -> Blocks.CONCRETE.pick(DyeColor.WHITE);
      case ORANGE     -> Blocks.CONCRETE.pick(DyeColor.ORANGE);
      case MAGENTA    -> Blocks.CONCRETE.pick(DyeColor.MAGENTA);
      case LIGHT_BLUE -> Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE);
      case YELLOW     -> Blocks.CONCRETE.pick(DyeColor.YELLOW);
      case LIME       -> Blocks.CONCRETE.pick(DyeColor.LIME);
      case PINK       -> Blocks.CONCRETE.pick(DyeColor.PINK);
      case GRAY       -> Blocks.CONCRETE.pick(DyeColor.GRAY);
      case LIGHT_GRAY -> Blocks.CONCRETE.pick(DyeColor.LIGHT_GRAY);
      case CYAN       -> Blocks.CONCRETE.pick(DyeColor.CYAN);
      case PURPLE     -> Blocks.CONCRETE.pick(DyeColor.PURPLE);
      case BLUE       -> Blocks.CONCRETE.pick(DyeColor.BLUE);
      case BROWN      -> Blocks.CONCRETE.pick(DyeColor.BROWN);
      case GREEN      -> Blocks.CONCRETE.pick(DyeColor.GREEN);
      case RED        -> Blocks.CONCRETE.pick(DyeColor.RED);
      case BLACK      -> Blocks.CONCRETE.pick(DyeColor.BLACK);
    };
  }

  /** Adds an interaction for both empty and the passed map */
  private static void addToMapAndCauldron(CauldronInteraction.Dispatcher map, Item item, CauldronInteraction interaction) {
    map.put(item, interaction);
    CauldronInteractions.EMPTY.put(item, interaction);
  }

  /** Adds an interaction for each item in the list */
  private static void addToList(CauldronInteraction.Dispatcher map, CauldronInteraction interaction, Item... items) {
    for (Item item : items) {
      map.put(item, interaction);
    }
  }
}
