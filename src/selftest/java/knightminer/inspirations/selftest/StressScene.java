package knightminer.inspirations.selftest;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.mojang.datafixers.util.Pair;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import knightminer.inspirations.building.block.type.ShelfType;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.cauldrons.block.entity.DyeCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.library.MiscUtil;
import knightminer.inspirations.tweaks.network.RequestLecternBookPacket;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Stress scene ({@code tools/scenetest.sh -Pscene=stress}), inert unless {@code inspirations.scene} is {@code stress}:
 * a broken common config from before the start (tools/scenetest.sh writes it), 1024 shelves full of colored books and
 * 96 cauldrons, frame rate before and with them, shelves and cauldrons changing every tick (chunk sections rebuild on
 * many threads), 2000 lectern book requests for random positions, extreme config values, leaving and rejoining the
 * world. Logs {@code [scene] CHECK/PERF}, then quits.
 */
@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
public final class StressScene {
  private static final boolean ENABLED = System.getProperty("inspirations.scene", "").equals("stress");
  private static final String WORLD = "insp_stress";
  private static final int SIDE = 32;
  private static final Block[] SLABS = {Blocks.OAK_SLAB, Blocks.BIRCH_SLAB, Blocks.SPRUCE_SLAB, Blocks.CRIMSON_SLAB, Blocks.WARPED_SLAB,
    Blocks.CHERRY_SLAB, Blocks.MANGROVE_SLAB, Blocks.STONE_SLAB, Blocks.QUARTZ_SLAB};
  private static final List<Holder<Potion>> POTIONS = List.of(Potions.SWIFTNESS, Potions.HEALING, Potions.POISON, Potions.NIGHT_VISION, Potions.REGENERATION);
  private static final List<Pair<Integer, Consumer<Minecraft>>> STEPS = new ArrayList<>();
  /** Steps after rejoining, counted from the new join */
  private static final List<Pair<Integer, Consumer<Minecraft>>> REJOIN_STEPS = new ArrayList<>();
  private static final Random RANDOM = new Random(20261016L);
  private static boolean opened, rejoining, rejoined;
  private static int tick;
  private static int failures, checks;
  private static BlockPos stage;
  /** Frame counting */
  private static int frames;
  private static long measureStart;
  /** Shelf contents and a dye color before leaving */
  private static final List<String> savedShelves = new ArrayList<>();
  private static int savedDye;

  static void at(int t, Consumer<Minecraft> action) {
    STEPS.add(Pair.of(t, action));
  }

  static void afterRejoin(int t, Consumer<Minecraft> action) {
    REJOIN_STEPS.add(Pair.of(t, action));
  }

  static void check(String name, boolean ok, Object detail) {
    checks++;
    if (!ok) {
      failures++;
    }
    Inspirations.log.info("[scene] CHECK " + (ok ? "OK " : "FAIL ") + name + (ok ? "" : " (" + detail + ")"));
  }

  static void server(Minecraft mc, Consumer<ServerPlayer> action) {
    MinecraftServer server = mc.getSingleplayerServer();
    server.execute(() -> {
      try {
        action.accept(server.getPlayerList().getPlayer(mc.player.getUUID()));
      } catch (RuntimeException | LinkageError e) {
        check("server step ran", false, e);
        Inspirations.log.error("[scene] server step failed", e);
      }
    });
  }

  static BlockPos at(int x, int y, int z) {
    return stage.offset(x, y, z);
  }

  private static void startMeasure() {
    frames = 0;
    measureStart = System.nanoTime();
  }

  private static double fps(String name) {
    double fps = frames / ((System.nanoTime() - measureStart) / 1e9);
    var server = Minecraft.getInstance().getSingleplayerServer();
    double mspt = server == null ? 0 : server.getAverageTickTimeNanos() / 1e6;
    Inspirations.log.info(String.format("[scene] PERF %s: %.1f FPS, %.2f MSPT", name, fps, mspt));
    return fps;
  }

  private static double fpsEmpty;

  static {
    int t = 20;
    at(t, mc -> {
      // tools/scenetest.sh wrote building = "maybe", shelf = 42 and bookshelf = "yes" before the start
      check("broken common config: unknown values back to the defaults, shelf registered", Config.enableBookshelf.getAsBoolean() && Config.enableColoredBooks.getAsBoolean()
        && InspirationsBuilding.shelf.get(ShelfType.NORMAL) != null, Config.enableBookshelf.getAsBoolean() + " " + Config.enableColoredBooks.getAsBoolean());
      server(mc, StressScene::floor);
    });
    at(t += 40, mc -> startMeasure());
    at(t += 100, mc -> {
      fpsEmpty = fps("empty floor");
      server(mc, StressScene::vanilla);
    });
    // vanilla baseline: the same places with full chiseled bookshelves (a block entity, a block model per book)
    at(t += 100, mc -> startMeasure());
    at(t += 100, mc -> {
      fps("vanilla baseline, 1024 full chiseled bookshelves");
      server(mc, StressScene::build);
    });
    // let 1024 shelves and their books bake
    at(t += 100, mc -> {
      check("shelves reached the client", mc.level.getBlockEntity(at(SIDE - 1, 1, SIDE - 1)) instanceof ShelfBlockEntity shelf && !shelf.getInventory().getStackInSlot(0).isEmpty(), "no shelf");
      startMeasure();
    });
    at(t += 100, mc -> {
      double fps = fps("1024 shelves full of books");
      check("frame rate with 1024 shelves stays over 30 FPS", fps > 30, fps);
      server(mc, StressScene::campfires);
    });
    // the campfires alone are vanilla (smoke particles), the cauldrons above them are the mod's
    at(t += 60, mc -> startMeasure());
    at(t += 100, mc -> {
      fps("plus 32 campfires (vanilla)");
      server(mc, StressScene::cauldrons);
    });
    at(t += 60, mc -> startMeasure());
    at(t += 100, mc -> {
      fps("plus 96 cauldrons (32 boiling over campfires)");
      startMeasure();
    });
    // churn: shelves, cauldrons, broken and placed shelves every tick
    int churnStart = t + 1;
    int churnEnd = t + 200;
    for (int i = churnStart; i <= churnEnd; i++) {
      at(i, mc -> server(mc, StressScene::churn));
    }
    at(t = churnEnd + 1, mc -> {
      double fps = fps("200 ticks of 64 shelves and 32 cauldrons changing per tick");
      check("frame rate while shelves change stays over 20 FPS", fps > 20, fps);
    });
    // packet spam: the client asks for lectern books anywhere
    at(t += 5, mc -> {
      for (int i = 0; i < 2000; i++) {
        BlockPos pos = new BlockPos(RANDOM.nextInt(60_000_000) - 30_000_000, RANDOM.nextInt(400) - 64, RANDOM.nextInt(60_000_000) - 30_000_000);
        ClientPacketDistributor.sendToServer(new RequestLecternBookPacket(i % 100 == 0 ? at(RANDOM.nextInt(SIDE), 1, RANDOM.nextInt(SIDE)) : pos));
      }
      // and once for the real lectern, its book was cleared on the client
      if (mc.level.getBlockEntity(at(-4, 1, -4)) instanceof LecternBlockEntity lectern) {
        lectern.clearContent();
      }
      ClientPacketDistributor.sendToServer(new RequestLecternBookPacket(at(-4, 1, -4)));
    });
    at(t += 20, mc -> {
      check("2000 lectern requests for random positions: the server runs, the real lectern answers",
        mc.getSingleplayerServer().isRunning() && mc.level.getBlockEntity(at(-4, 1, -4)) instanceof LecternBlockEntity lectern && lectern.getBook().is(InspirationsBuilding.coloredBook), "no book");
      server(mc, StressScene::extremeConfig);
    });
    at(t += 10, mc -> server(mc, StressScene::saveState));
    at(t += 10, mc -> {
      rejoining = true;
      mc.level.disconnect(net.minecraft.network.chat.Component.empty());
      mc.disconnect(new TitleScreen(), false);
    });

    afterRejoin(60, mc -> server(mc, p -> {
      List<String> now = shelfState(p.level());
      check("shelf contents and textures survive saving and loading", now.equals(savedShelves), now.size() + " vs " + savedShelves.size());
    }));
    afterRejoin(65, mc -> check("dye cauldron color reached the client after loading", mc.level.getBlockEntity(at(0, 1, -2)) instanceof DyeCauldronBlockEntity dye && dye.getColor() == savedDye,
      mc.level.getBlockEntity(at(0, 1, -2)) instanceof DyeCauldronBlockEntity dye ? Integer.toHexString(dye.getColor()) : "none"));
    afterRejoin(75, mc -> {
      Inspirations.log.info("[scene] DONE " + (checks - failures) + "/" + checks + (failures == 0 ? " passed" : " (" + failures + " failed)"));
      mc.level.disconnect(net.minecraft.network.chat.Component.empty());
      mc.disconnect(new TitleScreen(), false);
      mc.stop();
    });
  }

  @SubscribeEvent
  public static void onFrame(RenderFrameEvent.Post event) {
    if (ENABLED) {
      frames++;
    }
  }

  @SubscribeEvent
  public static void onTick(ClientTickEvent.Post event) {
    if (!ENABLED) {
      return;
    }
    Minecraft mc = Minecraft.getInstance();
    if (!opened && mc.gui.screen() instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
      mc.options.onboardAccessibility = false;
      mc.options.save();
      mc.gui.setScreen(new TitleScreen());
      return;
    }
    if (mc.gui.overlay() != null) {
      return;
    }
    if (!opened && mc.gui.screen() instanceof TitleScreen) {
      opened = true;
      GLFW.glfwSetWindowSize(mc.getWindow().handle(), 1600, 900);
      mc.options.guiScale().set(3);
      mc.options.cloudStatus().set(CloudStatus.OFF);
      mc.options.framerateLimit().set(260);
      mc.options.enableVsync().set(false);
      mc.options.renderDistance().set(8);
      mc.options.pauseOnLostFocus = false;
      mc.resizeGui();
      LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT);
      mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(20261016L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
      return;
    }
    if (rejoining && !rejoined && mc.level == null && mc.gui.screen() instanceof TitleScreen) {
      rejoined = true;
      tick = 0;
      mc.createWorldOpenFlows().openWorld(WORLD, () -> check("world opens again", false, "failed"));
      return;
    }
    if (mc.player == null || mc.getSingleplayerServer() == null) {
      return;
    }
    mc.options.pauseOnLostFocus = false;
    mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
    mc.gui.toastManager().clear();
    if (mc.gui.screen() instanceof PauseScreen) {
      mc.gui.setScreen(null);
    }
    tick++;
    for (Pair<Integer, Consumer<Minecraft>> step : rejoined ? REJOIN_STEPS : STEPS) {
      if (step.getFirst() == tick) {
        try {
          step.getSecond().accept(mc);
        } catch (RuntimeException | LinkageError e) {
          check("step at tick " + tick + " ran", false, e);
          Inspirations.log.error("[scene] step failed", e);
        }
      }
    }
  }

  /* World */

  /** Stone floor in the sky, the player above its middle looking down at it */
  private static void floor(ServerPlayer player) {
    ServerLevel level = player.level();
    BlockPos spawn = level.getRespawnData().pos();
    stage = new BlockPos(spawn.getX() - SIDE / 2, 150, spawn.getZ() - SIDE / 2);
    ClientScene.command(level, "time set 6000");
    level.getGameRules().set(GameRules.ADVANCE_TIME, false, level.getServer());
    level.getGameRules().set(GameRules.ADVANCE_WEATHER, false, level.getServer());
    for (BlockPos pos : BlockPos.betweenClosed(at(-6, 0, -6), at(SIDE + 2, 0, SIDE + 2))) {
      level.setBlock(pos, Blocks.SMOOTH_STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
    player.getAbilities().flying = true;
    player.onUpdateAbilities();
    player.teleportTo(level, stage.getX() + SIDE / 2.0, stage.getY() + 14, stage.getZ() - 6, java.util.Set.of(), 0, 45, true);
  }

  private static ItemStack randomBook() {
    return switch (RANDOM.nextInt(6)) {
      case 0 -> new ItemStack(Items.BOOK);
      case 1 -> new ItemStack(Items.ENCHANTED_BOOK);
      case 2 -> ItemStack.EMPTY;
      default -> MiscUtil.setColor(new ItemStack(InspirationsBuilding.coloredBook), RANDOM.nextInt(0x1000000));
    };
  }

  private static void placeShelf(ServerLevel level, BlockPos pos) {
    ShelfType type = ShelfType.values()[RANDOM.nextInt(ShelfType.values().length)];
    level.setBlock(pos, InspirationsBuilding.shelf.get(type).defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
    if (level.getBlockEntity(pos) instanceof ShelfBlockEntity shelf) {
      shelf.updateTexture(SLABS[RANDOM.nextInt(SLABS.length)]);
      for (int slot = 0; slot < 16; slot++) {
        shelf.getInventory().setStackInSlot(slot, slot == 0 ? new ItemStack(InspirationsBuilding.coloredBook) : randomBook());
      }
    }
  }

  /** The baseline: 1024 full chiseled bookshelves where the shelves go */
  private static void vanilla(ServerPlayer player) {
    ServerLevel level = player.level();
    BlockState full = Blocks.CHISELED_BOOKSHELF.defaultBlockState();
    for (var slot : net.minecraft.world.level.block.ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES) {
      full = full.setValue(slot, true);
    }
    for (int x = 0; x < SIDE; x++) {
      for (int z = 0; z < SIDE; z++) {
        level.setBlock(at(x, 1, z), full, Block.UPDATE_ALL);
      }
    }
  }

  /** 1024 shelves full of books */
  private static void build(ServerPlayer player) {
    ServerLevel level = player.level();
    for (int x = 0; x < SIDE; x++) {
      for (int z = 0; z < SIDE; z++) {
        placeShelf(level, at(x, 1, z));
      }
    }
  }

  /** 32 campfires, the fire for the boiling cauldrons */
  private static void campfires(ServerPlayer player) {
    ServerLevel level = player.level();
    for (int x = 0; x < SIDE; x++) {
      level.setBlock(at(x, 1, -4), Blocks.CAMPFIRE.defaultBlockState(), Block.UPDATE_ALL);
    }
  }

  /** 32 boiling cauldrons over the campfires, 32 dye and 32 potion cauldrons, a lectern with a book */
  private static void cauldrons(ServerPlayer player) {
    ServerLevel level = player.level();
    for (int x = 0; x < SIDE; x++) {
      level.setBlock(at(x, 2, -4), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), Block.UPDATE_ALL);
      level.setBlock(at(x, 1, -2), InspirationsCaudrons.dyeCauldron.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), Block.UPDATE_ALL);
      if (level.getBlockEntity(at(x, 1, -2)) instanceof DyeCauldronBlockEntity dye) {
        dye.setColor(MiscUtil.getColor(DyeColor.values()[x % 16]));
      }
      level.setBlock(at(x, 1, -3), InspirationsCaudrons.potionCauldron.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), Block.UPDATE_ALL);
      if (level.getBlockEntity(at(x, 1, -3)) instanceof PotionCauldronBlockEntity potion) {
        potion.setPotion(POTIONS.get(x % POTIONS.size()));
      }
    }
    level.setBlock(at(-4, 1, -4), Blocks.LECTERN.defaultBlockState(), Block.UPDATE_ALL);
    LecternBlock.tryPlaceBook(player, level, at(-4, 1, -4), level.getBlockState(at(-4, 1, -4)), MiscUtil.setColor(new ItemStack(InspirationsBuilding.coloredBook), MiscUtil.getColor(DyeColor.LIME)));
  }

  /** One tick: 64 shelves change a book, 4 shelves are replaced, 16 dye and 16 potion cauldrons change */
  private static void churn(ServerPlayer player) {
    ServerLevel level = player.level();
    for (int i = 0; i < 64; i++) {
      if (level.getBlockEntity(at(RANDOM.nextInt(SIDE), 1, RANDOM.nextInt(SIDE))) instanceof ShelfBlockEntity shelf) {
        shelf.getInventory().setStackInSlot(1 + RANDOM.nextInt(15), randomBook());
      }
    }
    for (int i = 0; i < 4; i++) {
      BlockPos pos = at(RANDOM.nextInt(SIDE), 1, RANDOM.nextInt(SIDE));
      level.removeBlock(pos, false);
      placeShelf(level, pos);
    }
    for (int i = 0; i < 16; i++) {
      if (level.getBlockEntity(at(RANDOM.nextInt(SIDE), 1, -2)) instanceof DyeCauldronBlockEntity dye) {
        dye.setColor(RANDOM.nextInt(0x1000000));
      }
      if (level.getBlockEntity(at(RANDOM.nextInt(SIDE), 1, -3)) instanceof PotionCauldronBlockEntity potion) {
        potion.setPotion(POTIONS.get(RANDOM.nextInt(POTIONS.size())));
      }
    }
  }

  /** Enchanting power at the config's limits, the milk cooldown at its maximum */
  private static void extremeConfig(ServerPlayer player) {
    ServerLevel level = player.level();
    // an enchanting table inside the shelves: shelves on the ring two blocks away
    BlockPos table = at(5, 1, 5);
    for (BlockPos pos : BlockPos.betweenClosed(table.offset(-1, 0, -1), table.offset(1, 1, 1))) {
      level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
    level.setBlock(table, Blocks.ENCHANTING_TABLE.defaultBlockState(), Block.UPDATE_ALL);
    Config.defaultEnchantingPower.set(15.0);
    knightminer.inspirations.library.InspirationsRegistry.clearBookCache();
    int[] costs = enchantCosts(player, table);
    check("enchanting power 15 per book: costs stay within vanilla's 30", costs[2] > 0 && costs[2] <= 30, java.util.Arrays.toString(costs));
    Config.defaultEnchantingPower.set(0.0);
    knightminer.inspirations.library.InspirationsRegistry.clearBookCache();
    costs = enchantCosts(player, table);
    check("enchanting power 0 per book: no crash, low costs", costs[2] <= 8, java.util.Arrays.toString(costs));
    Config.defaultEnchantingPower.set(1.5);
    knightminer.inspirations.library.InspirationsRegistry.clearBookCache();
    Config.milkCooldownTime.set((int) Short.MAX_VALUE);
    check("milk cooldown at its maximum is accepted", Config.milkCooldownTime.get() == Short.MAX_VALUE, Config.milkCooldownTime.get());
    Config.milkCooldownTime.set(600);
  }

  private static int[] enchantCosts(ServerPlayer player, BlockPos table) {
    // shelf power is cached per shelf, the shelves are placed again to drop it
    for (BlockPos pos : BlockPos.betweenClosed(table.offset(-2, 0, -2), table.offset(2, 1, 2))) {
      if (player.level().getBlockEntity(pos) instanceof ShelfBlockEntity shelf) {
        BlockState state = player.level().getBlockState(pos);
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < 16; slot++) {
          items.add(shelf.getInventory().getStackInSlot(slot).copy());
        }
        Block texture = shelf.getTexture();
        player.level().removeBlock(pos, false);
        player.level().setBlock(pos, state, Block.UPDATE_ALL);
        if (player.level().getBlockEntity(pos) instanceof ShelfBlockEntity fresh) {
          fresh.updateTexture(texture);
          for (int slot = 0; slot < 16; slot++) {
            fresh.getInventory().setStackInSlot(slot, items.get(slot));
          }
        }
      }
    }
    EnchantmentMenu menu = new EnchantmentMenu(0, player.getInventory(), ContainerLevelAccess.create(player.level(), table));
    menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
    menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
    menu.slotsChanged(menu.getSlot(0).container);
    int[] costs = menu.costs.clone();
    menu.getSlot(0).set(ItemStack.EMPTY);
    menu.getSlot(1).set(ItemStack.EMPTY);
    return costs;
  }

  private static List<String> shelfState(ServerLevel level) {
    List<String> state = new ArrayList<>();
    for (int x = 0; x < SIDE; x += 7) {
      for (int z = 0; z < SIDE; z += 7) {
        if (level.getBlockEntity(at(x, 1, z)) instanceof ShelfBlockEntity shelf) {
          StringBuilder line = new StringBuilder(shelf.getTextureName());
          for (int slot = 0; slot < 16; slot++) {
            ItemStack stack = shelf.getInventory().getStackInSlot(slot);
            line.append(' ').append(stack.isEmpty() ? "-" : stack.getItem() + "#" + Integer.toHexString(MiscUtil.getColor(stack)));
          }
          state.add(line.toString());
        }
      }
    }
    return state;
  }

  private static void saveState(ServerPlayer player) {
    savedShelves.clear();
    savedShelves.addAll(shelfState(player.level()));
    if (player.level().getBlockEntity(at(0, 1, -2)) instanceof DyeCauldronBlockEntity dye) {
      dye.setColor(0x123456);
      savedDye = dye.getColor();
    }
  }
}
