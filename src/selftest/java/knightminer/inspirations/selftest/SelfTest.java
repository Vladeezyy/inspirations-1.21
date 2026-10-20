package knightminer.inspirations.selftest;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import knightminer.inspirations.tools.enchantment.InspirationsEnchanting;
import net.minecraft.world.entity.EntitySpawnReason;
import dev.vladeezyy.kilnwork.KilnworkRegistries;
import dev.vladeezyy.kilnwork.retexture.RetexturedHelper;
import dev.vladeezyy.kilnwork.util.LegacyItemStacks;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.building.block.RopeBlock;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import knightminer.inspirations.building.block.type.ShelfType;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.cauldrons.block.FourLayerCauldronBlock;
import knightminer.inspirations.cauldrons.block.entity.DyeCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.SuspiciousStewCauldronBlockEntity;
import knightminer.inspirations.library.MiscUtil;
import knightminer.inspirations.tools.InspirationsTools;
import knightminer.inspirations.tools.capability.DimensionCompass;
import knightminer.inspirations.tweaks.InspirationsTweaks;
import knightminer.inspirations.tweaks.block.FittedCarpetBlock;
import knightminer.inspirations.utility.InspirationsUtility;
import knightminer.inspirations.utility.block.entity.CollectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Functional checks on a dev server, only in dev runs with -Dinspirations.selftest=true (the source set is not in the
 * jar). Prints "[Inspirations selftest] PASS n/n" or the failed checks, then stops the server.
 */
@EventBusSubscriber(modid = Inspirations.modID)
public class SelfTest {
  private static final String PREFIX = "[Inspirations selftest] ";
  private static final boolean ENABLED = Boolean.getBoolean("inspirations.selftest");
  private static final List<String> failures = new ArrayList<>();
  private static int checks = 0;
  /** Tick counter after server start, -1 when not running */
  private static int tick = -1;
  private static BlockPos origin;
  private static FakePlayer player;

  static void check(String name, boolean result) {
    checks++;
    if (result) {
      Inspirations.log.info(PREFIX + "ok   {}", name);
    } else {
      failures.add(name);
      Inspirations.log.error(PREFIX + "FAIL {}", name);
    }
  }

  static void check(String name, BooleanSupplier test) {
    boolean result;
    try {
      result = test.getAsBoolean();
    } catch (RuntimeException e) {
      Inspirations.log.error(PREFIX + "exception in {}", name, e);
      result = false;
    }
    check(name, result);
  }

  /** Position in the test area */
  private static BlockPos at(int x, int y, int z) {
    return origin.offset(x, y, z);
  }

  /** Right clicks the top of the block with the stack like a player (events, block, item) */
  private static InteractionResult use(ServerLevel level, BlockPos pos, ItemStack stack, Direction side) {
    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).relative(side, 0.5), side, pos, false);
    return player.gameMode.useItemOn(player, level, stack, InteractionHand.MAIN_HAND, hit);
  }

  private static InteractionResult use(ServerLevel level, BlockPos pos, ItemStack stack) {
    return use(level, pos, stack, Direction.UP);
  }

  /** Sneaking, isCrouching() reads the pose */
  private static void crouch(boolean crouching) {
    player.setShiftKeyDown(crouching);
    player.setPose(crouching ? net.minecraft.world.entity.Pose.CROUCHING : net.minecraft.world.entity.Pose.STANDING);
  }

  private static Holder<Enchantment> enchantment(ServerLevel level, ResourceKey<Enchantment> key) {
    return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
  }

  @SubscribeEvent
  static void serverStarted(ServerStartedEvent event) {
    if (!ENABLED) {
      return;
    }
    ServerLevel level = event.getServer().overworld();
    BlockPos spawn = level.getRespawnData().pos();
    origin = new BlockPos(spawn.getX(), 150, spawn.getZ());
    for (int cx = -1; cx <= 2; cx++) {
      for (int cz = -1; cz <= 2; cz++) {
        level.setChunkForced((origin.getX() >> 4) + cx, (origin.getZ() >> 4) + cz, true);
      }
    }
    for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -2, -2), origin.offset(30, 8, 12))) {
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
    player = FakePlayerFactory.getMinecraft(level);
    player.setPos(origin.getX(), origin.getY(), origin.getZ() - 2);
    try {
      instantChecks(event.getServer(), level);
      worldSetup(level);
    } catch (RuntimeException e) {
      Inspirations.log.error(PREFIX + "setup crashed", e);
      check("setup", false);
    }
    tick = 0;
  }

  @SubscribeEvent
  static void serverTick(ServerTickEvent.Post event) {
    if (tick < 0) {
      return;
    }
    tick++;
    ServerLevel level = event.getServer().overworld();
    if (tick == 100) {
      try {
        worldChecks(level);
      } catch (RuntimeException e) {
        Inspirations.log.error(PREFIX + "world checks crashed", e);
        check("world checks", false);
      }
      int passed = checks - failures.size();
      if (failures.isEmpty()) {
        Inspirations.log.info(PREFIX + "PASS {}/{}", passed, checks);
      } else {
        Inspirations.log.error(PREFIX + "FAILED {}/{}: {}", passed, checks, failures);
      }
      tick = -1;
      event.getServer().halt(false);
    }
  }

  /* Checks not needing ticks */

  /** Result of the brewing recipe for the input and reagent, empty if none */
  private static ItemStack brew(MinecraftServer server, ServerLevel level, ItemStack input, ItemStack reagent) {
    var brewing = new net.minecraft.world.item.crafting.BrewingInput(input, reagent);
    return server.getRecipeManager().getRecipeFor(RecipeType.BREWING, brewing, level).map(r -> r.value().assemble(brewing)).orElse(ItemStack.EMPTY);
  }

  private static void instantChecks(MinecraftServer server, ServerLevel level) {
    long blocks = BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals(Inspirations.modID)).count();
    long items = BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals(Inspirations.modID)).count();
    check("registries: " + blocks + " blocks, " + items + " items", blocks > 100 && items > 80);

    long recipes = server.getRecipeManager().getRecipes().stream().filter(r -> r.id().identifier().getNamespace().equals(Inspirations.modID)).count();
    check("recipes loaded with the config condition: " + recipes, recipes >= 200);

    // shelves take the texture of the slabs, and creative shelves are the same items (issue #247)
    check("shelf recipe gives a shelf textured with the slab", () -> {
      ItemStack slab = new ItemStack(Items.OAK_SLAB);
      CraftingInput input = CraftingInput.of(3, 3, List.of(slab, slab, slab, ItemStack.EMPTY, slab, ItemStack.EMPTY, slab, slab, slab));
      return server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
        .map(r -> r.value().assemble(input))
        .map(result -> result.is(InspirationsBuilding.shelf.get(ShelfType.NORMAL).asItem()) && result.getCount() == 2 && RetexturedHelper.getTexture(result) == Blocks.OAK_SLAB)
        .orElse(false);
    });
    check("creative tab shelves have slab textures, like crafted ones (#247)", () -> {
      CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS).value();
      tab.buildContents(new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), true, level.registryAccess()));
      return tab.getDisplayItems().stream().anyMatch(s -> s.is(InspirationsBuilding.shelf.get(ShelfType.NORMAL).asItem()) && RetexturedHelper.getTexture(s) == Blocks.OAK_SLAB);
    });
    check("every creative tab builds", () -> {
      for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), true, level.registryAccess()));
      }
      return true;
    });
    check("colored book recipe dyes the book", () -> {
      CraftingInput input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.BOOK), new ItemStack(Items.DYE.pick(DyeColor.RED))));
      return server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
        .map(r -> r.value().assemble(input))
        .map(result -> result.is(InspirationsBuilding.coloredBook) && MiscUtil.getColor(result) == MiscUtil.getColor(DyeColor.RED)).orElse(false);
    });
    // 26.1 dropped the dyeable tag: one crafting_dye recipe per item, washing through cauldron_can_remove_dye
    check("colored book takes another dye in the crafting grid (crafting_dye)", () -> {
      CraftingInput input = CraftingInput.of(2, 1, List.of(MiscUtil.setColor(new ItemStack(InspirationsBuilding.coloredBook), MiscUtil.getColor(DyeColor.RED)), new ItemStack(Items.DYE.pick(DyeColor.BLUE))));
      return server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
        .map(r -> r.value().assemble(input))
        .map(result -> result.is(InspirationsBuilding.coloredBook) && MiscUtil.getColor(result) != MiscUtil.getColor(DyeColor.RED)).orElse(false);
    });
    check("water cauldrons wash the dye off a dimension compass", () -> {
      ItemStack compass = MiscUtil.setColor(new ItemStack(InspirationsTools.dimensionCompass), 0x123456);
      return compass.is(net.minecraft.tags.ItemTags.CAULDRON_CAN_REMOVE_DYE)
        && net.minecraft.core.cauldron.CauldronInteractions.WATER.get(compass) != net.minecraft.core.cauldron.CauldronInteraction.DEFAULT;
    });
    check("mixing dyed bottles keeps no empty bottles", () -> {
      ItemStack red = new ItemStack(InspirationsCaudrons.simpleDyedWaterBottle.get(DyeColor.RED));
      ItemStack yellow = new ItemStack(InspirationsCaudrons.simpleDyedWaterBottle.get(DyeColor.YELLOW));
      CraftingInput input = CraftingInput.of(2, 1, List.of(red, yellow));
      return server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
        .map(r -> r.value().assemble(input).is(InspirationsCaudrons.simpleDyedWaterBottle.get(DyeColor.ORANGE).asItem())
          && r.value().getRemainingItems(input).stream().allMatch(ItemStack::isEmpty)).orElse(false);
    });

    // enchantments are data in 1.21, the shield and axe rules come from mixins
    check("shield takes protection, thorns, knockback and fire aspect", () -> {
      ItemStack shield = new ItemStack(Items.SHIELD);
      return shield.supportsEnchantment(enchantment(level, Enchantments.PROTECTION)) && shield.supportsEnchantment(enchantment(level, Enchantments.THORNS))
        && shield.supportsEnchantment(enchantment(level, Enchantments.KNOCKBACK)) && shield.supportsEnchantment(enchantment(level, Enchantments.FIRE_ASPECT))
        && !shield.supportsEnchantment(enchantment(level, Enchantments.SHARPNESS));
    });
    check("shield is enchantable at the table (12, banner 16)", () -> {
      ItemStack shield = new ItemStack(Items.SHIELD);
      ItemStack banner = new ItemStack(Items.SHIELD);
      banner.set(DataComponents.BASE_COLOR, DyeColor.RED);
      // since 1.21.2 the table needs the enchantable component, the mixins give it to shields
      int cost = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentCost(level.getRandom(), 0, 15, shield);
      Inspirations.log.info(PREFIX + "shield enchantability {} / {}, table {}, cost {}, primary {}", InspirationsEnchanting.shieldEnchantability(shield), InspirationsEnchanting.shieldEnchantability(banner),
        shield.isEnchantable(), cost, shield.isPrimaryItemFor(enchantment(level, Enchantments.PROTECTION)));
      return InspirationsEnchanting.shieldEnchantability(shield) == 12 && InspirationsEnchanting.shieldEnchantability(banner) == 16 && shield.isEnchantable() && cost > 0
        && shield.isPrimaryItemFor(enchantment(level, Enchantments.PROTECTION));
    });
    check("axe takes looting, knockback and fire aspect, sharpness at the table", () -> {
      ItemStack axe = new ItemStack(Items.IRON_AXE);
      return axe.supportsEnchantment(enchantment(level, Enchantments.LOOTING)) && axe.supportsEnchantment(enchantment(level, Enchantments.KNOCKBACK))
        && axe.supportsEnchantment(enchantment(level, Enchantments.FIRE_ASPECT)) && axe.isPrimaryItemFor(enchantment(level, Enchantments.SHARPNESS))
        && !axe.supportsEnchantment(enchantment(level, Enchantments.SWEEPING_EDGE));
    });
    check("fire aspect and thorns, looting and fortune are incompatible", () ->
      !Enchantment.areCompatible(enchantment(level, Enchantments.FIRE_ASPECT), enchantment(level, Enchantments.THORNS))
        && !Enchantment.areCompatible(enchantment(level, Enchantments.LOOTING), enchantment(level, Enchantments.FORTUNE))
        && Enchantment.areCompatible(enchantment(level, Enchantments.SHARPNESS), enchantment(level, Enchantments.LOOTING)));

    // loot tables injected on load: in 1.20 the listener was never registered
    check("skeleton loot has the skull pool", () -> {
      LootTable table = server.reloadableRegistries().getLootTable(EntityTypes.SKELETON.getDefaultLootTable().orElseThrow());
      return table.getPool(Inspirations.getResource("entities/skeleton").toString()) != null;
    });
    check("cave spider loot has the cobweb pool", () -> {
      LootTable table = server.reloadableRegistries().getLootTable(EntityTypes.CAVE_SPIDER.getDefaultLootTable().orElseThrow());
      return table.getPool(Inspirations.getResource("entities/cave_spider").toString()) != null;
    });
    check("rose bush loot drops a rose with shears", () -> {
      LootTable table = server.reloadableRegistries().getLootTable(Blocks.ROSE_BUSH.getLootTable().orElseThrow());
      return table.getPool(Inspirations.getResource("blocks/inject/rose").toString()) != null;
    });
    check("heartbeets drop from beetroots (global loot modifier)", () -> {
      BlockPos pos = at(28, 0, 0);
      BlockState beets = Blocks.BEETROOTS.defaultBlockState().setValue(net.minecraft.world.level.block.BeetrootBlock.AGE, 3);
      int heartbeets = 0;
      for (int i = 0; i < 400; i++) {
        for (ItemStack drop : Block.getDrops(beets, level, pos, null)) {
          if (drop.is(InspirationsTweaks.heartbeet)) {
            heartbeets++;
          }
        }
      }
      return heartbeets > 0;
    });

    // brewing (data recipes since 26.3)
    check("heartbeet brews awkward into regeneration", () ->
      brew(server, level, PotionContents.createItemStack(Items.POTION, Potions.AWKWARD), new ItemStack(InspirationsTweaks.heartbeet))
        .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.REGENERATION));
    check("glass bottles brew into splash bottles with gunpowder", () ->
      brew(server, level, new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.GUNPOWDER)).is(InspirationsCaudrons.splashBottle));
    check("splash bottles brew into lingering bottles with dragon's breath", () ->
      brew(server, level, new ItemStack(InspirationsCaudrons.splashBottle), new ItemStack(Items.DRAGON_BREATH)).is(InspirationsCaudrons.lingeringBottle));

    // misc registration (compost chances are components since 26.3)
    check("compostables", () -> new ItemStack(InspirationsTweaks.heartbeet).has(DataComponents.COMPOSTABLE) && new ItemStack(InspirationsBuilding.vine).has(DataComponents.COMPOSTABLE)
      && new ItemStack(InspirationsBuilding.shelf.get(ShelfType.NORMAL)).has(DataComponents.COOKING_FUEL));
    check("wet hopper keeps the hopper block entity", () -> net.minecraft.world.level.block.entity.BlockEntityTypes.HOPPER.isValid(InspirationsTweaks.wetHopper.defaultBlockState()));

    // data of 1.20 worlds
    check("1.20 texture in custom data is read", () -> {
      ItemStack shelf = new ItemStack(InspirationsBuilding.shelf.get(ShelfType.NORMAL));
      CompoundTag tag = new CompoundTag();
      tag.putString("texture", "minecraft:spruce_planks");
      shelf.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      return RetexturedHelper.getTexture(shelf) == Blocks.SPRUCE_PLANKS;
    });
    check("1.20 item stack in a block entity is read", () -> {
      CompoundTag tag = new CompoundTag();
      tag.putString("id", "minecraft:enchanted_book");
      tag.putByte("Count", (byte) 1);
      CompoundTag nbt = new CompoundTag();
      ListTag stored = new ListTag();
      CompoundTag sharpness = new CompoundTag();
      sharpness.putString("id", "minecraft:sharpness");
      sharpness.putShort("lvl", (short) 3);
      stored.add(sharpness);
      nbt.put("StoredEnchantments", stored);
      tag.put("tag", nbt);
      ItemStack stack = LegacyItemStacks.parse(level.registryAccess(), tag);
      return stack.is(Items.ENCHANTED_BOOK) && stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY)
        .getLevel(enchantment(level, Enchantments.SHARPNESS)) == 3;
    });
  }

  /* Blocks placed before ticking */

  private static void worldSetup(ServerLevel level) {
    // collector facing a chest with items, pulsed with redstone after a few ticks
    BlockPos chest = at(0, 0, 0);
    level.setBlockAndUpdate(chest, Blocks.CHEST.defaultBlockState());
    if (level.getBlockEntity(chest) instanceof ChestBlockEntity be) {
      be.setItem(0, new ItemStack(Items.DIAMOND, 3));
    }
    level.setBlockAndUpdate(at(0, 0, 1), InspirationsUtility.collector.defaultBlockState().setValue(net.minecraft.world.level.block.DirectionalBlock.FACING, Direction.NORTH));

    // two collectors grabbing the same item entity (issue #227)
    level.setBlockAndUpdate(at(4, 0, 0), InspirationsUtility.collector.defaultBlockState().setValue(net.minecraft.world.level.block.DirectionalBlock.FACING, Direction.SOUTH));
    level.setBlockAndUpdate(at(4, 0, 2), InspirationsUtility.collector.defaultBlockState().setValue(net.minecraft.world.level.block.DirectionalBlock.FACING, Direction.NORTH));
    level.setBlockAndUpdate(at(4, -1, 1), Blocks.GLASS.defaultBlockState());

    // pipe into a chest
    BlockPos pipeChest = at(8, 0, 0);
    level.setBlockAndUpdate(pipeChest, Blocks.CHEST.defaultBlockState());
    level.setBlockAndUpdate(at(8, 0, 1), InspirationsUtility.pipe.defaultBlockState().setValue(net.minecraft.world.level.block.DirectionalBlock.FACING, Direction.NORTH));
    var pipe = level.getCapability(Capabilities.Item.BLOCK, at(8, 0, 1), null);
    if (pipe != null) {
      try (var transaction = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
        pipe.insert(0, net.neoforged.neoforge.transfer.item.ItemResource.of(Items.EMERALD), 2, transaction);
        transaction.commit();
      }
    }

    // boiling water cauldron over a campfire
    level.setBlockAndUpdate(at(12, -1, 0), Blocks.CAMPFIRE.defaultBlockState());
    level.setBlockAndUpdate(at(12, 0, 0), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
  }

  /* Checks after ticking */

  private static void worldChecks(ServerLevel level) {
    // collectors
    check("collector pulls an item from the chest it faces", () -> {
      level.setBlockAndUpdate(at(1, 0, 1), Blocks.REDSTONE_BLOCK.defaultBlockState());
      ((CollectorBlockEntity) level.getBlockEntity(at(0, 0, 1))).collect();
      return ((CollectorBlockEntity) level.getBlockEntity(at(0, 0, 1))).getItem(0).is(Items.DIAMOND)
        && ((ChestBlockEntity) level.getBlockEntity(at(0, 0, 0))).getItem(0).getCount() == 2;
    });
    check("two collectors cannot duplicate one dropped item (#227)", () -> {
      ItemEntity item = new ItemEntity(level, at(4, 0, 1).getX() + 0.5, at(4, 0, 1).getY() + 0.1, at(4, 0, 1).getZ() + 0.5, new ItemStack(Items.GOLD_INGOT, 5));
      item.setNoGravity(true);
      level.addFreshEntity(item);
      CollectorBlockEntity first = (CollectorBlockEntity) level.getBlockEntity(at(4, 0, 0));
      CollectorBlockEntity second = (CollectorBlockEntity) level.getBlockEntity(at(4, 0, 2));
      first.collect();
      second.collect();
      int total = 0;
      for (CollectorBlockEntity collector : List.of(first, second)) {
        for (int i = 0; i < collector.getContainerSize(); i++) {
          total += collector.getItem(i).is(Items.GOLD_INGOT) ? collector.getItem(i).getCount() : 0;
        }
      }
      // the stack in the collector must not be the one of the removed entity either
      first.getItem(0).grow(0);
      return total == 5 && !item.isAlive();
    });

    // pipe
    check("pipe moved its items into the chest", () -> {
      ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(at(8, 0, 0));
      return chest.getItem(0).is(Items.EMERALD) && chest.getItem(0).getCount() == 2;
    });

    // boiling water cauldron, a mixin on the vanilla block
    check("water cauldron over fire boils entities", () -> {
      Pig pig = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
      pig.snapTo(Vec3.atBottomCenterOf(at(12, 0, 0)).add(0, 0.3, 0));
      level.addFreshEntity(pig);
      float health = pig.getHealth();
      BlockState cauldron = level.getBlockState(at(12, 0, 0));
      cauldron.entityInside(level, at(12, 0, 0), pig, net.minecraft.world.entity.InsideBlockEffectApplier.NOOP, true);
      boolean hurt = pig.getHealth() < health;
      pig.discard();
      return hurt;
    });

    // cauldron interactions, through the full right click
    check("red dye in a water cauldron makes a dye cauldron of that color", () -> {
      BlockPos pos = at(14, 0, 0);
      level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
      use(level, pos, new ItemStack(Items.DYE.pick(DyeColor.RED)));
      return level.getBlockState(pos).is(InspirationsCaudrons.dyeCauldron) && level.getBlockEntity(pos) instanceof DyeCauldronBlockEntity be && be.getDye() == DyeColor.RED;
    });
    check("glass bottle takes a red dyed bottle from the dye cauldron", () -> {
      ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
      use(level, at(14, 0, 0), bottle);
      return player.getMainHandItem().is(InspirationsCaudrons.simpleDyedWaterBottle.get(DyeColor.RED).asItem()) || player.getInventory().contains(new ItemStack(InspirationsCaudrons.simpleDyedWaterBottle.get(DyeColor.RED)));
    });
    check("leather in a dye cauldron is dyed (cauldron_can_remove_dye tag)", () -> {
      BlockPos pos = at(14, 0, 2);
      level.setBlockAndUpdate(pos, InspirationsCaudrons.dyeCauldron.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
      ((DyeCauldronBlockEntity) level.getBlockEntity(pos)).setColor(0x123456);
      player.getInventory().clearContent();
      use(level, pos, new ItemStack(Items.LEATHER_HELMET));
      ItemStack held = player.getMainHandItem();
      return held.is(Items.LEATHER_HELMET) && MiscUtil.getColor(held) == 0x123456;
    });
    check("potion into an empty cauldron makes a potion cauldron", () -> {
      BlockPos pos = at(16, 0, 0);
      level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
      use(level, pos, PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
      return level.getBlockState(pos).is(InspirationsCaudrons.potionCauldron) && level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity be
        && be.getPotion() != null && be.getPotion().is(Potions.SWIFTNESS);
    });
    check("arrows dipped in the potion cauldron become tipped arrows", () -> {
      player.getInventory().clearContent();
      use(level, at(16, 0, 0), new ItemStack(Items.ARROW, 4));
      ItemStack held = player.getMainHandItem();
      return held.is(Items.TIPPED_ARROW) && held.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.SWIFTNESS);
    });
    check("water bottle into a dye cauldron of 1 level adds water", () -> {
      BlockPos pos = at(14, 0, 4);
      level.setBlockAndUpdate(pos, InspirationsCaudrons.dyeCauldron.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
      use(level, pos, PotionContents.createItemStack(Items.POTION, Potions.WATER));
      return level.getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL) == 2;
    });
    check("honey bottle into an empty cauldron makes a honey cauldron", () -> {
      BlockPos pos = at(18, 0, 0);
      level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
      use(level, pos, new ItemStack(Items.HONEY_BOTTLE));
      return level.getBlockState(pos).is(InspirationsCaudrons.honeyCauldron) && level.getBlockState(pos).getValue(FourLayerCauldronBlock.LEVEL) == 1;
    });
    check("milk bucket fills a cauldron with milk", () -> {
      BlockPos pos = at(18, 0, 2);
      level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
      use(level, pos, new ItemStack(Items.MILK_BUCKET));
      return level.getBlockState(pos).is(InspirationsCaudrons.milkCauldron) && level.getBlockState(pos).getValue(FourLayerCauldronBlock.LEVEL) == 4;
    });
    check("boiling milk with mushrooms makes mushroom stew (mushroom tag override)", () -> {
      BlockPos pos = at(20, 0, 0);
      level.setBlockAndUpdate(pos.below(), Blocks.CAMPFIRE.defaultBlockState());
      level.setBlockAndUpdate(pos, InspirationsCaudrons.milkCauldron.defaultBlockState().setValue(FourLayerCauldronBlock.LEVEL, 4));
      use(level, pos, new ItemStack(Items.RED_MUSHROOM, 16));
      return level.getBlockState(pos).is(InspirationsCaudrons.mushroomStewCauldron);
    });
    check("suspicious stew goes in and out of a cauldron with its effects", () -> {
      BlockPos pos = at(20, 0, 2);
      level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
      ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
      stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(MobEffects.NIGHT_VISION, 100))));
      use(level, pos, stew);
      boolean in = level.getBlockState(pos).is(InspirationsCaudrons.suspiciousStewCauldron);
      player.getInventory().clearContent();
      use(level, pos, new ItemStack(Items.BOWL));
      ItemStack out = player.getMainHandItem();
      return in && out.is(Items.SUSPICIOUS_STEW) && out.getOrDefault(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY).effects().stream()
        .anyMatch(e -> e.effect().is(MobEffects.NIGHT_VISION) && e.duration() == 100);
    });
    check("1.20 suspicious stew data in a cauldron is read", () -> {
      BlockPos pos = at(20, 0, 4);
      level.setBlockAndUpdate(pos, InspirationsCaudrons.suspiciousStewCauldron.defaultBlockState());
      CompoundTag tag = new CompoundTag();
      ListTag effects = new ListTag();
      CompoundTag effect = new CompoundTag();
      effect.putByte("EffectId", (byte) 16);
      effect.putString("forge:effect_id", "minecraft:night_vision");
      effect.putInt("EffectDuration", 160);
      effects.add(effect);
      tag.put("Effects", effects);
      BlockEntity be = level.getBlockEntity(pos);
      be.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), tag));
      return be instanceof SuspiciousStewCauldronBlockEntity stewCauldron && stewCauldron.getEffects().effects().size() == 1
        && stewCauldron.getEffects().effects().get(0).effect().is(MobEffects.NIGHT_VISION);
    });

    // shelves
    check("shelf takes a book and gives enchanting power", () -> {
      BlockPos pos = at(22, 0, 0);
      level.setBlockAndUpdate(pos, InspirationsBuilding.shelf.get(ShelfType.NORMAL).defaultBlockState());
      ShelfBlockEntity shelf = (ShelfBlockEntity) level.getBlockEntity(pos);
      for (int i = 0; i < 16; i++) {
        shelf.getInventory().setStackInSlot(i, new ItemStack(Items.BOOK));
      }
      return shelf.getInventory().getStackInSlot(0).is(Items.BOOK) && level.getBlockState(pos).getEnchantPowerBonus(level, pos) > 1;
    });
    check("redstone book gives comparator output", () -> {
      BlockPos pos = at(22, 0, 2);
      level.setBlockAndUpdate(pos, InspirationsBuilding.shelf.get(ShelfType.NORMAL).defaultBlockState());
      ShelfBlockEntity shelf = (ShelfBlockEntity) level.getBlockEntity(pos);
      shelf.getInventory().setStackInSlot(5, new ItemStack(InspirationsBuilding.redstoneBook));
      return level.getBlockState(pos).getAnalogOutputSignal(level, pos, Direction.NORTH) == 5;
    });
    check("broken shelf drops with its texture", () -> {
      BlockPos pos = at(22, 0, 4);
      level.setBlockAndUpdate(pos, InspirationsBuilding.shelf.get(ShelfType.NORMAL).defaultBlockState());
      ((ShelfBlockEntity) level.getBlockEntity(pos)).updateTexture(Blocks.BIRCH_SLAB);
      List<ItemStack> drops = Block.getDrops(level.getBlockState(pos), level, pos, level.getBlockEntity(pos));
      return drops.size() == 1 && RetexturedHelper.getTexture(drops.get(0)) == Blocks.BIRCH_SLAB;
    });
    check("placed textured shelf keeps the texture", () -> {
      BlockPos pos = at(22, 0, 6);
      level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
      ItemStack shelf = RetexturedHelper.setTexture(new ItemStack(InspirationsBuilding.shelf.get(ShelfType.NORMAL)), Blocks.ACACIA_SLAB);
      player.setYRot(0);
      use(level, pos.below(), shelf);
      return level.getBlockEntity(pos) instanceof ShelfBlockEntity be && be.getTexture() == Blocks.ACACIA_SLAB;
    });

    // ropes
    check("rope placed under a block extends with more rope", () -> {
      BlockPos pos = at(24, 2, 0);
      level.setBlockAndUpdate(pos.above(), Blocks.OAK_PLANKS.defaultBlockState());
      level.setBlockAndUpdate(pos, InspirationsBuilding.rope.defaultBlockState());
      use(level, pos, new ItemStack(InspirationsBuilding.rope), Direction.NORTH);
      return level.getBlockState(pos.below()).is(InspirationsBuilding.rope);
    });
    check("sticks make rope ladder rungs", () -> {
      BlockPos pos = at(24, 2, 0);
      use(level, pos, new ItemStack(Items.STICK, 4), Direction.NORTH);
      return level.getBlockState(pos).getValue(RopeBlock.RUNGS) != RopeBlock.Rungs.NONE;
    });
    check("iron bars above rope are climbable, glass panes are not changed", () -> {
      BlockPos bars = at(24, 3, 4);
      level.setBlockAndUpdate(bars.above(), Blocks.OAK_PLANKS.defaultBlockState());
      level.setBlockAndUpdate(bars, Blocks.IRON_BARS.defaultBlockState());
      level.setBlockAndUpdate(bars.below(), InspirationsBuilding.rope.defaultBlockState());
      BlockPos pane = at(26, 3, 4);
      level.setBlockAndUpdate(pane, Blocks.GLASS_PANE.defaultBlockState());
      level.setBlockAndUpdate(pane.below(), InspirationsBuilding.rope.defaultBlockState());
      return level.getBlockState(bars).isLadder(level, bars, player) && !level.getBlockState(pane).isLadder(level, pane, player);
    });

    // carpets fit to stairs, a mixin on the vanilla carpets
    check("carpet placed on stairs becomes a fitted carpet", () -> {
      BlockPos stairs = at(26, 0, 0);
      level.setBlockAndUpdate(stairs, Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
      use(level, stairs, new ItemStack(Items.CARPET.pick(DyeColor.RED)));
      return level.getBlockState(stairs.above()).getBlock() instanceof FittedCarpetBlock;
    });
    check("fitted carpet becomes flat when the stairs go", () -> {
      BlockPos stairs = at(26, 0, 0);
      level.setBlockAndUpdate(stairs, Blocks.STONE.defaultBlockState());
      return level.getBlockState(stairs.above()).is(Blocks.CARPET.pick(DyeColor.RED));
    });

    // hoppers take water, a mixin on the vanilla hopper
    check("water placed in a hopper makes a wet hopper that keeps its items", () -> {
      BlockPos pos = at(28, 2, 2);
      level.setBlockAndUpdate(pos, Blocks.HOPPER.defaultBlockState());
      ((HopperBlockEntity) level.getBlockEntity(pos)).setItem(0, new ItemStack(Items.COAL, 7));
      boolean placed = ((net.minecraft.world.level.block.LiquidBlockContainer) Blocks.HOPPER).placeLiquid(level, pos, level.getBlockState(pos), Fluids.WATER.getSource(false));
      return placed && level.getBlockState(pos).is(InspirationsTweaks.wetHopper) && level.getBlockEntity(pos) instanceof HopperBlockEntity hopper
        && hopper.getItem(0).getCount() == 7 && level.getFluidState(pos).is(Fluids.WATER);
    });
    check("taking the water out dries the hopper", () -> {
      BlockPos pos = at(28, 2, 2);
      ItemStack bucket = ((net.minecraft.world.level.block.BucketPickup) InspirationsTweaks.wetHopper).pickupBlock(player, level, pos, level.getBlockState(pos));
      return bucket.is(Items.WATER_BUCKET) && level.getBlockState(pos).is(Blocks.HOPPER) && ((HopperBlockEntity) level.getBlockEntity(pos)).getItem(0).getCount() == 7;
    });

    // torch levers
    check("torch lever powers when used", () -> {
      BlockPos pos = at(2, 0, 6);
      level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(pos, InspirationsUtility.torchLeverFloor.defaultBlockState());
      use(level, pos, ItemStack.EMPTY);
      return level.getBlockState(pos).getSignal(level, pos, Direction.UP) == 15;
    });
    // carpeted pressure plate
    check("carpet on a stone pressure plate hides it", () -> {
      BlockPos pos = at(4, 0, 6);
      level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
      level.setBlockAndUpdate(pos, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
      use(level, pos, new ItemStack(Items.CARPET.pick(DyeColor.BLUE)));
      return level.getBlockState(pos).is(InspirationsUtility.carpetedPressurePlates.get(DyeColor.BLUE));
    });
    // redstone charger
    check("redstone charger places a charge that powers", () -> {
      BlockPos pos = at(6, 0, 6);
      level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
      player.getInventory().clearContent();
      use(level, pos.below(), new ItemStack(InspirationsTools.redstoneCharger));
      return level.getBlockState(pos).is(InspirationsTools.redstoneCharge) && level.hasNeighborSignal(pos.below());
    });
    // locks
    check("named lock locks a chest, matching key unlocks it", () -> {
      BlockPos pos = at(8, 0, 6);
      level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
      ItemStack lock = new ItemStack(InspirationsTools.lock);
      lock.set(DataComponents.CUSTOM_NAME, Component.literal("selftest"));
      use(level, pos, lock, Direction.NORTH);
      boolean locked = ((ChestBlockEntity) level.getBlockEntity(pos)).isLocked();
      Inspirations.log.info(PREFIX + "chest locked: {}", locked);
      ItemStack key = new ItemStack(InspirationsTools.key);
      key.set(DataComponents.CUSTOM_NAME, Component.literal("selftest"));
      crouch(true);
      use(level, pos, key, Direction.NORTH);
      crouch(false);
      return locked && !((ChestBlockEntity) level.getBlockEntity(pos)).isLocked();
    });
    // pigs
    check("sneaking with an empty hand takes the saddle off a pig", () -> {
      Pig pig = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
      pig.snapTo(Vec3.atCenterOf(at(10, 0, 6)));
      level.addFreshEntity(pig);
      pig.setItemSlot(net.minecraft.world.entity.EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
      player.getInventory().clearContent();
      player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
      crouch(true);
      player.interactOn(pig, InteractionHand.MAIN_HAND, pig.getBoundingBox().getCenter());
      crouch(false);
      boolean result = !pig.isSaddled() && player.getInventory().contains(new ItemStack(Items.SADDLE));
      pig.discard();
      return result;
    });
    // milk
    check("milk bottle cures one effect", () -> {
      player.removeAllEffects();
      player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
      ItemStack bottle = new ItemStack(InspirationsCaudrons.milkBottle);
      bottle.finishUsingItem(level, player);
      return !player.hasEffect(MobEffects.POISON);
    });
    // compass
    check("dimension compass data is attached to players", () -> DimensionCompass.get(player).getEnteredPosition() == null);
    // smooth growth
    check("grown cactus puts a cactus seed on top", () -> {
      BlockPos sand = at(12, 0, 6);
      level.setBlockAndUpdate(sand, Blocks.SAND.defaultBlockState());
      level.setBlockAndUpdate(sand.above(), Blocks.CACTUS.defaultBlockState().setValue(net.minecraft.world.level.block.CactusBlock.AGE, 15));
      level.getBlockState(sand.above()).randomTick(level, sand.above(), level.getRandom());
      return level.getBlockState(sand.above(2)).is(InspirationsTweaks.cactus);
    });
  }
}
