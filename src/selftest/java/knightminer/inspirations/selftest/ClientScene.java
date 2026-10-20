package knightminer.inspirations.selftest;

import knightminer.inspirations.common.client.ItemColorSources;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import dev.vladeezyy.kilnwork.client.render.RenderItem;
import dev.vladeezyy.kilnwork.retexture.RetexturedHelper;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.building.block.RopeBlock;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import knightminer.inspirations.building.block.type.BushType;
import knightminer.inspirations.building.block.type.FlowerType;
import knightminer.inspirations.building.block.type.MulchType;
import knightminer.inspirations.building.block.type.PathType;
import knightminer.inspirations.building.block.type.ShelfType;
import knightminer.inspirations.building.client.ShelfContainerScreen;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.cauldrons.block.FourLayerCauldronBlock;
import knightminer.inspirations.cauldrons.block.entity.DyeCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.cauldrons.client.BoilingParticle;
import knightminer.inspirations.library.MiscUtil;
import knightminer.inspirations.tools.InspirationsTools;
import knightminer.inspirations.tweaks.InspirationsTweaks;
import knightminer.inspirations.utility.InspirationsUtility;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.model.data.ModelData;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Client scene, inert unless {@code -Dinspirations.scene=main} ({@code tools/scenetest.sh}): builds a showcase of every
 * block, checks the models (textures from other blocks, books on shelves, no missing sprites), tint colors, fluid
 * textures, the lectern book sync, the screens, JEI (issue #247), the config resource pack and the translations, and
 * saves screenshots to {@code run/screenshots/scene_*.png}. Logs {@code [scene] CHECK OK/FAIL}, then quits.
 */
@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
public final class ClientScene {
  private static final String MODE = System.getProperty("inspirations.scene", "");
  private static final boolean ENABLED = MODE.equals("main");
  private static final String LANG = System.getProperty("inspirations.scene.lang", "en_us");
  private static final String WORLD = "insp_scene";
  private static final List<Pair<Integer, Consumer<Minecraft>>> STEPS = new ArrayList<>();
  private static boolean opened;
  private static int tick;
  private static int failures, checks;
  /** Corner of the showcase, set on the server */
  private static BlockPos stage;

  static void at(int t, Consumer<Minecraft> action) {
    STEPS.add(Pair.of(t, action));
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

  static void shot(Minecraft mc, String name) {
    Screenshot.grab(mc.gameDirectory, "scene_" + name + ".png", mc.getMainRenderTarget(), 1, c -> Inspirations.log.info("[scene] " + c.getString()));
  }

  static BlockPos at(int x, int y, int z) {
    return stage.offset(x, y, z);
  }

  /** Puts the player at the stage position, looking at the target */
  static void lookAt(ServerPlayer player, double x, double y, double z, double tx, double ty, double tz) {
    double dx = tx - x, dy = ty - (y + player.getEyeHeight()), dz = tz - z;
    float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
    float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
    player.teleportTo(player.level(), stage.getX() + x, stage.getY() + y, stage.getZ() + z, java.util.Set.of(), yaw, pitch, true);
  }

  static {
    int t = 40;
    at(t, mc -> server(mc, ClientScene::build));
    // let the chunk sections rebuild and the lectern ask for its book
    at(t += 60, mc -> {
      checkModels(mc);
      checkColors(mc);
      checkClientData(mc);
    });
    at(t += 5, mc -> server(mc, p -> lookAt(p, 9.5, 1, -7, 9.5, 1, 3)));
    at(t += 30, mc -> {
      shot(mc, "overview");
      checkNoMissingTexture(mc, "overview");
    });
    at(t += 5, mc -> server(mc, p -> lookAt(p, 2.5, 1, -1.2, 2.5, 0.8, 2)));
    at(t += 25, mc -> shot(mc, "shelves"));
    at(t += 5, mc -> server(mc, p -> lookAt(p, 8.5, 1.2, -1.5, 8.5, 0.3, 4)));
    at(t += 25, mc -> shot(mc, "building"));
    at(t += 5, mc -> server(mc, p -> lookAt(p, 4.5, 1.5, 3.5, 4.5, 0.4, 8)));
    at(t += 25, mc -> shot(mc, "cauldrons"));
    at(t += 5, mc -> server(mc, p -> lookAt(p, 14.5, 1.5, 3.5, 14.5, 0.4, 8)));
    at(t += 25, mc -> shot(mc, "utility"));
    at(t += 5, mc -> server(mc, p -> lookAt(p, 3.5, 1.2, 9.2, 3.5, 0.4, 12)));
    at(t += 25, mc -> shot(mc, "carpets"));
    at(t += 5, mc -> server(mc, p -> lookAt(p, 16.5, 1.5, 9.5, 16.5, 2.0, 13)));
    at(t += 25, mc -> {
      shot(mc, "portal_lectern");
      checkNoMissingTexture(mc, "portal and lectern");
    });
    // screens
    at(t += 5, mc -> server(mc, p -> {
      lookAt(p, 2.5, 1, -1.2, 2.5, 0.8, 2);
      p.openMenu((MenuProvider) p.level().getBlockEntity(at(0, 1, 2)), at(0, 1, 2));
    }));
    at(t += 20, mc -> {
      check("shelf screen opens with 16 shelf slots", mc.screen instanceof ShelfContainerScreen screen && screen.getMenu().slots.size() == 16 + 36, mc.screen);
      shot(mc, "gui_shelf");
      checkNoMissingTexture(mc, "shelf screen");
      mc.player.closeContainer();
    });
    // menus close when the player is over 8 blocks away
    at(t += 5, mc -> server(mc, p -> {
      lookAt(p, 12.5, 1, 4.2, 12.5, 1.5, 6.5);
      p.openMenu((MenuProvider) p.level().getBlockEntity(at(12, 1, 6)), at(12, 1, 6));
    }));
    at(t += 20, mc -> {
      check("collector screen opens with 9 slots", mc.screen instanceof AbstractContainerScreen<?> screen && screen.getMenu().slots.size() == 9 + 36, mc.screen);
      shot(mc, "gui_collector");
      mc.player.closeContainer();
    });
    at(t += 5, mc -> server(mc, p -> {
      lookAt(p, 14.5, 1, 4.2, 14.5, 1.5, 6.5);
      p.openMenu((MenuProvider) p.level().getBlockEntity(at(14, 1, 6)), at(14, 1, 6));
    }));
    at(t += 20, mc -> {
      check("pipe screen opens with 1 slot", mc.screen instanceof AbstractContainerScreen<?> screen && screen.getMenu().slots.size() == 1 + 36, mc.screen);
      shot(mc, "gui_pipe");
      mc.player.closeContainer();
    });
    // items in the hand and the hotbar
    at(t += 5, mc -> server(mc, p -> {
      lookAt(p, 9.5, 1, -7, 9.5, 3, 3);
      Inventory inventory = p.getInventory();
      inventory.clearContent();
      List<ItemStack> hotbar = hotbarItems(p.level());
      for (int i = 0; i < hotbar.size(); i++) {
        inventory.setItem(i, hotbar.get(i));
      }
      inventory.setSelectedSlot(0);
    }));
    at(t += 25, mc -> {
      shot(mc, "hotbar");
      checkNoMissingTexture(mc, "hotbar");
    });
    // -Pno_jei runs (old NeoForge) have no JEI classes at all
    at(t += 5, mc -> {
      if (net.neoforged.fml.ModList.get().isLoaded("jei")) {
        SceneJeiPlugin.checkJei(mc);
      } else {
        Inspirations.log.info("[scene] JEI is not loaded, its checks are skipped");
      }
    });
    at(t += 5, ClientScene::checkTranslations);
    at(t += 10, mc -> {
      Inspirations.log.info("[scene] DONE " + (checks - failures) + "/" + checks + (failures == 0 ? " passed" : " (" + failures + " failed)"));
      mc.level.disconnect(net.minecraft.network.chat.Component.empty());
      mc.disconnect(new TitleScreen(), false);
      mc.stop();
    });
  }

  @SubscribeEvent
  public static void onTick(ClientTickEvent.Post event) {
    if (!ENABLED) {
      return;
    }
    Minecraft mc = Minecraft.getInstance();
    if (!opened && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
      mc.options.onboardAccessibility = false;
      mc.options.save();
      mc.setScreen(new TitleScreen());
      return;
    }
    if (!opened && mc.getOverlay() != null) {
      return;
    }
    if (!opened && mc.screen instanceof TitleScreen) {
      if (!mc.options.languageCode.equals(LANG)) {
        mc.options.languageCode = LANG;
        mc.getLanguageManager().setSelected(LANG);
        mc.options.save();
        mc.reloadResourcePacks();
        return;
      }
      opened = true;
      GLFW.glfwSetWindowSize(mc.getWindow().handle(), 1600, 900);
      mc.options.guiScale().set(3);
      mc.options.cloudStatus().set(CloudStatus.OFF);
      mc.options.pauseOnLostFocus = false;
      mc.resizeDisplay();
      LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(net.minecraft.world.level.WorldDataConfiguration.DEFAULT.enabledFeatures()), net.minecraft.world.level.WorldDataConfiguration.DEFAULT);
      mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(20261013L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
      return;
    }
    if (mc.player == null || mc.getSingleplayerServer() == null) {
      return;
    }
    mc.options.pauseOnLostFocus = false;
    mc.options.hideGui = false;
    mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
    mc.getToastManager().clear();
    if (mc.screen instanceof PauseScreen) {
      mc.setScreen(null);
    }
    tick++;
    for (Pair<Integer, Consumer<Minecraft>> step : STEPS) {
      if (step.getFirst() == tick) {
        Inspirations.log.info("[scene] tick " + tick);
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

  private static void set(ServerLevel level, BlockPos pos, BlockState state) {
    level.setBlock(pos, state, Block.UPDATE_ALL);
  }

  /** The showcase: rows of blocks on a stone floor, in the sky above spawn */
  private static void build(ServerPlayer player) {
    ServerLevel level = player.level();
    BlockPos spawn = level.getRespawnData().pos();
    stage = new BlockPos(spawn.getX(), 140, spawn.getZ());
    level.setDayTime(6000);
    level.getGameRules().set(GameRules.ADVANCE_TIME, false, level.getServer());
    level.getGameRules().set(GameRules.ADVANCE_WEATHER, false, level.getServer());
    level.setWeatherParameters(6000, 0, false, false);
    for (BlockPos pos : BlockPos.betweenClosed(at(-3, 0, -9), at(22, 8, 16))) {
      level.setBlock(pos, pos.getY() == stage.getY() ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
    lookAt(player, 9.5, 1, -7, 9.5, 1, 3);
    player.getAbilities().flying = true;
    player.onUpdateAbilities();

    // row 1, z=2: shelves with books and an apple, textured with different slabs
    Block[] slabs = {Blocks.OAK_SLAB, Blocks.BIRCH_SLAB, Blocks.SPRUCE_SLAB, Blocks.DARK_OAK_SLAB, Blocks.CHERRY_SLAB};
    ShelfType[] types = {ShelfType.NORMAL, ShelfType.RAINBOW, ShelfType.TOMES, ShelfType.ANCIENT, ShelfType.NORMAL};
    for (int i = 0; i < types.length; i++) {
      BlockPos pos = at(i, 1, 2);
      set(level, pos, InspirationsBuilding.shelf.get(types[i]).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
      ShelfBlockEntity shelf = (ShelfBlockEntity) level.getBlockEntity(pos);
      shelf.updateTexture(slabs[i]);
      int slot = 0;
      for (DyeColor color : List.of(DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN, DyeColor.YELLOW, DyeColor.PURPLE)) {
        shelf.getInventory().setStackInSlot(slot++, MiscUtil.setColor(new ItemStack(InspirationsBuilding.coloredBook), MiscUtil.getColor(color)));
      }
      shelf.getInventory().setStackInSlot(slot++, new ItemStack(Items.ENCHANTED_BOOK));
      shelf.getInventory().setStackInSlot(slot, new ItemStack(InspirationsBuilding.redstoneBook));
      shelf.getInventory().setStackInSlot(10, new ItemStack(Items.APPLE));
      for (int s = 12; s < 16; s++) {
        shelf.getInventory().setStackInSlot(s, new ItemStack(Items.BOOK));
      }
    }
    // row 1 continued: bushes, ropes, glass doors
    int x = 6;
    for (BushType type : BushType.values()) {
      BlockPos pos = at(x++, 1, 2);
      set(level, pos, InspirationsBuilding.enlightenedBush.get(type).defaultBlockState());
      if (level.getBlockEntity(pos) instanceof dev.vladeezyy.kilnwork.retexture.IRetexturedBlockEntity be) {
        be.updateTexture(type == BushType.WHITE ? Blocks.BIRCH_LEAVES : Blocks.OAK_LEAVES);
      }
    }
    set(level, at(11, 4, 2), Blocks.OAK_PLANKS.defaultBlockState());
    for (int y = 3; y >= 1; y--) {
      set(level, at(11, y, 2), InspirationsBuilding.rope.defaultBlockState().setValue(RopeBlock.RUNGS, y == 2 ? RopeBlock.Rungs.X : RopeBlock.Rungs.NONE).setValue(RopeBlock.BOTTOM, y == 1));
    }
    set(level, at(12, 4, 2), Blocks.OAK_PLANKS.defaultBlockState());
    for (int y = 3; y >= 1; y--) {
      set(level, at(12, y, 2), InspirationsBuilding.vine.defaultBlockState().setValue(RopeBlock.BOTTOM, y == 1));
    }
    set(level, at(13, 1, 2), InspirationsBuilding.glassDoor.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH));
    set(level, at(13, 2, 2), InspirationsBuilding.glassDoor.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
    set(level, at(14, 1, 2), InspirationsBuilding.glassTrapdoor.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.NORTH).setValue(TrapDoorBlock.OPEN, true));
    // iron bars climbable over a rope
    set(level, at(15, 3, 2), Blocks.IRON_BARS.defaultBlockState());
    set(level, at(15, 2, 2), InspirationsBuilding.rope.defaultBlockState());
    set(level, at(15, 4, 2), Blocks.OAK_PLANKS.defaultBlockState());
    // paths on stone, mulch with flowers
    x = 16;
    for (PathType type : PathType.values()) {
      set(level, at(x++, 1, 2), InspirationsBuilding.path.get(type).defaultBlockState());
    }
    x = 6;
    for (MulchType type : MulchType.values()) {
      set(level, at(x, 1, 0), InspirationsBuilding.mulch.get(type).defaultBlockState());
      x++;
    }
    x = 6;
    for (FlowerType type : FlowerType.values()) {
      set(level, at(x, 2, 0), InspirationsBuilding.flower.get(type).defaultBlockState());
      set(level, at(x + 6, 1, 0), InspirationsBuilding.flowerPot.get(type).defaultBlockState());
      x++;
    }

    // row 2, z=6: cauldrons
    set(level, at(0, 1, 6), Blocks.CAMPFIRE.defaultBlockState());
    set(level, at(0, 2, 6), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
    set(level, at(1, 1, 6), InspirationsCaudrons.dyeCauldron.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
    ((DyeCauldronBlockEntity) level.getBlockEntity(at(1, 1, 6))).setColor(MiscUtil.getColor(DyeColor.RED));
    set(level, at(2, 1, 6), InspirationsCaudrons.potionCauldron.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
    ((PotionCauldronBlockEntity) level.getBlockEntity(at(2, 1, 6))).setPotion(Potions.SWIFTNESS);
    Block[] fourLayer = {InspirationsCaudrons.honeyCauldron, InspirationsCaudrons.milkCauldron, InspirationsCaudrons.mushroomStewCauldron, InspirationsCaudrons.beetrootSoupCauldron,
      InspirationsCaudrons.rabbitStewCauldron, InspirationsCaudrons.potatoSoupCauldron, InspirationsCaudrons.suspiciousStewCauldron};
    for (int i = 0; i < fourLayer.length; i++) {
      set(level, at(3 + i, 1, 6), fourLayer[i].defaultBlockState().setValue(FourLayerCauldronBlock.LEVEL, 1 + i % 4));
    }
    // row 2: utility
    set(level, at(12, 1, 6), InspirationsUtility.collector.defaultBlockState());
    set(level, at(13, 1, 6), Blocks.HOPPER.defaultBlockState());
    set(level, at(14, 1, 6), InspirationsUtility.pipe.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.WEST));
    set(level, at(15, 1, 6), InspirationsTweaks.wetHopper.defaultBlockState());
    set(level, at(16, 1, 6), InspirationsUtility.torchLeverFloor.defaultBlockState());
    set(level, at(17, 2, 7), Blocks.STONE.defaultBlockState());
    set(level, at(17, 2, 6), InspirationsUtility.torchLeverWall.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    set(level, at(18, 1, 6), InspirationsUtility.carpetedTrapdoors.get(DyeColor.LIME).defaultBlockState());
    set(level, at(19, 1, 6), InspirationsUtility.carpetedTrapdoors.get(DyeColor.MAGENTA).defaultBlockState().setValue(TrapDoorBlock.OPEN, true).setValue(TrapDoorBlock.FACING, Direction.NORTH));
    set(level, at(20, 1, 6), InspirationsUtility.carpetedPressurePlates.get(DyeColor.ORANGE).defaultBlockState());
    set(level, at(21, 1, 6), InspirationsTools.redstoneCharge.defaultBlockState());
    set(level, at(16, 1, 8), InspirationsTweaks.cactus.defaultBlockState().setValue(knightminer.inspirations.tweaks.block.BlockCropBlock.LARGE_AGE, 8));
    set(level, at(17, 1, 8), Blocks.SAND.defaultBlockState());
    set(level, at(18, 1, 8), Blocks.WATER.defaultBlockState());
    set(level, at(17, 2, 8), InspirationsTweaks.sugarCane.defaultBlockState().setValue(knightminer.inspirations.tweaks.block.BlockCropBlock.LARGE_AGE, 10));
    set(level, at(16, 0, 8), Blocks.SAND.defaultBlockState());

    // row 3, z=12: fitted carpets on every stair shape
    StairsShape[] shapes = StairsShape.values();
    for (int i = 0; i < shapes.length; i++) {
      BlockPos pos = at(i * 2, 1, 12);
      set(level, pos, Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.SHAPE, shapes[i]));
      set(level, pos.above(), Blocks.RED_CARPET.defaultBlockState());
      level.getBlockState(pos.above()).getBlock().setPlacedBy(level, pos.above(), level.getBlockState(pos.above()), player, new ItemStack(Items.RED_CARPET));
    }

    // tinted portal over blue glass, lectern with a colored book
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 5; j++) {
        boolean frame = i == 0 || i == 3 || j == 0 || j == 4;
        set(level, at(15 + i, 1 + j, 13), frame ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState());
      }
      set(level, at(15 + i, 0, 13), Blocks.BLUE_STAINED_GLASS.defaultBlockState());
    }
    for (int i = 1; i <= 2; i++) {
      for (int j = 1; j <= 3; j++) {
        level.setBlock(at(15 + i, 1 + j, 13), Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, Direction.Axis.X), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
      }
    }
    set(level, at(13, 1, 12), Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));
    LecternBlock.tryPlaceBook(player, level, at(13, 1, 12), level.getBlockState(at(13, 1, 12)), MiscUtil.setColor(new ItemStack(InspirationsBuilding.coloredBook), MiscUtil.getColor(DyeColor.LIME)));
  }

  static List<ItemStack> hotbarItems(ServerLevel level) {
    List<ItemStack> items = new ArrayList<>();
    items.add(MiscUtil.setColor(new ItemStack(InspirationsBuilding.coloredBook), MiscUtil.getColor(DyeColor.CYAN)));
    items.add(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new EnchantmentInstance(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 1)));
    items.add(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(new EnchantmentInstance(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING), 1)));
    ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
    rocket.set(DataComponents.FIREWORKS, new net.minecraft.world.item.component.Fireworks(2, List.of(
      new net.minecraft.world.item.component.FireworkExplosion(net.minecraft.world.item.component.FireworkExplosion.Shape.STAR, it.unimi.dsi.fastutil.ints.IntList.of(0xFF2020), it.unimi.dsi.fastutil.ints.IntList.of(), false, false),
      new net.minecraft.world.item.component.FireworkExplosion(net.minecraft.world.item.component.FireworkExplosion.Shape.BURST, it.unimi.dsi.fastutil.ints.IntList.of(0x2020FF), it.unimi.dsi.fastutil.ints.IntList.of(), false, false))));
    items.add(rocket);
    items.add(new ItemStack(InspirationsTools.dimensionCompass));
    items.add(new ItemStack(InspirationsTools.barometer));
    items.add(new ItemStack(InspirationsTools.photometer));
    items.add(new ItemStack(InspirationsCaudrons.simpleDyedWaterBottle.get(DyeColor.MAGENTA)));
    items.add(new ItemStack(Items.CAULDRON));
    return items;
  }

  /* Checks */

  /** The client level with fixed model data at every position, for the model checks */
  private static BlockAndTintGetter withData(Minecraft mc, ModelData data) {
    BlockAndTintGetter level = mc.level;
    return new BlockAndTintGetter() {
      @Override
      public float getShade(Direction side, boolean shade) {
        return level.getShade(side, shade);
      }

      @Override
      public net.minecraft.world.level.lighting.LevelLightEngine getLightEngine() {
        return level.getLightEngine();
      }

      @Override
      public int getBlockTint(BlockPos pos, net.minecraft.world.level.ColorResolver resolver) {
        return level.getBlockTint(pos, resolver);
      }

      @Override
      public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(BlockPos pos) {
        return null;
      }

      @Override
      public BlockState getBlockState(BlockPos pos) {
        return level.getBlockState(pos);
      }

      @Override
      public net.minecraft.world.level.material.FluidState getFluidState(BlockPos pos) {
        return level.getFluidState(pos);
      }

      @Override
      public int getHeight() {
        return level.getHeight();
      }

      @Override
      public int getMinY() {
        return level.getMinY();
      }

      @Override
      public ModelData getModelData(BlockPos pos) {
        return data;
      }
    };
  }

  /** Quads of the block model with the model data, all sides */
  private static List<BakedQuad> quads(BlockStateModel model, BlockState state, BlockAndTintGetter level) {
    List<BakedQuad> quads = new ArrayList<>();
    for (BlockModelPart part : model.collectParts(level, BlockPos.ZERO, state, RandomSource.create(42))) {
      for (Direction side : new Direction[] {null, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
        quads.addAll(part.getQuads(side));
      }
    }
    return quads;
  }

  private static Set<Identifier> sprites(List<BakedQuad> quads) {
    Set<Identifier> sprites = new HashSet<>();
    for (BakedQuad quad : quads) {
      sprites.add(quad.sprite().contents().name());
    }
    return sprites;
  }

  /** How the item model resolver sets up a stack in the GUI: quads, tints and lighting of its layers (private fields) */
  private record ItemLook(List<BakedQuad> quads, List<int[]> tints, boolean blockLight, @org.jetbrains.annotations.Nullable net.minecraft.client.renderer.texture.TextureAtlasSprite particle) {}

  private static ItemLook look(Minecraft mc, ItemStack stack) {
    ItemStackRenderState state = new ItemStackRenderState();
    mc.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GUI, mc.level, mc.player, 0);
    List<BakedQuad> quads = new ArrayList<>();
    List<int[]> tints = new ArrayList<>();
    try {
      var count = ItemStackRenderState.class.getDeclaredField("activeLayerCount");
      var layers = ItemStackRenderState.class.getDeclaredField("layers");
      var layerQuads = ItemStackRenderState.LayerRenderState.class.getDeclaredField("quads");
      var layerTints = ItemStackRenderState.LayerRenderState.class.getDeclaredField("tintLayers");
      for (var field : List.of(count, layers, layerQuads, layerTints)) {
        field.setAccessible(true);
      }
      Object[] array = (Object[]) layers.get(state);
      for (int i = 0; i < count.getInt(state); i++) {
        @SuppressWarnings("unchecked")
        List<BakedQuad> list = (List<BakedQuad>) layerQuads.get(array[i]);
        quads.addAll(list);
        tints.add((int[]) layerTints.get(array[i]));
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
    return new ItemLook(quads, tints, state.usesBlockLight(), state.pickParticleIcon(RandomSource.create(42)));
  }

  /** Tint of the stack's first layer with that tint index, -1 for none */
  private static int itemTint(Minecraft mc, ItemStack stack, int index) {
    for (int[] tints : look(mc, stack).tints()) {
      if (index < tints.length) {
        return tints[index];
      }
    }
    return -1;
  }

  private static void checkModels(Minecraft mc) {
    var shaper = mc.getModelManager().getBlockModelShaper();
    Identifier missing = MissingTextureAtlasSprite.getLocation();
    BlockAndTintGetter noData = withData(mc, ModelData.EMPTY);
    // every block state and item model of the mod has real sprites
    List<String> bad = new ArrayList<>();
    for (Block block : BuiltInRegistries.BLOCK) {
      if (!BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Inspirations.modID)) {
        continue;
      }
      for (BlockState state : block.getStateDefinition().getPossibleStates()) {
        BlockStateModel model = shaper.getBlockModel(state);
        if (model == mc.getModelManager().getMissingBlockStateModel() || sprites(quads(model, state, noData)).contains(missing) || model.particleIcon(noData, BlockPos.ZERO, state).contents().name().equals(missing)) {
          bad.add(state.toString());
          break;
        }
      }
    }
    check("every block state has a model with real textures", bad.isEmpty(), bad);
    List<String> badItems = new ArrayList<>();
    var missingItem = mc.getModelManager().getItemModel(Inspirations.getResource("selftest_missing"));
    for (Item item : BuiltInRegistries.ITEM) {
      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      if (!id.getNamespace().equals(Inspirations.modID)) {
        continue;
      }
      ItemLook look = look(mc, new ItemStack(item));
      if (mc.getModelManager().getItemModel(id) == missingItem || sprites(look.quads()).contains(missing) || (look.particle() != null && look.particle().contents().name().equals(missing))) {
        badItems.add(id.toString());
      }
    }
    check("every item has a model with real textures", badItems.isEmpty(), badItems);

    // retextured models
    BlockState shelf = InspirationsBuilding.shelf.get(ShelfType.NORMAL).defaultBlockState();
    BlockStateModel shelfModel = shaper.getBlockModel(shelf);
    Set<Identifier> birch = sprites(quads(shelfModel, shelf, withData(mc, ModelData.builder().with(RetexturedHelper.BLOCK_PROPERTY, Blocks.BIRCH_SLAB).with(ShelfBlockEntity.BOOKS, 0).build())));
    check("shelf model takes the texture of a birch slab", birch.contains(Identifier.withDefaultNamespace("block/birch_planks")), birch);
    int empty = quads(shelfModel, shelf, withData(mc, ModelData.builder().with(RetexturedHelper.BLOCK_PROPERTY, Blocks.OAK_SLAB).with(ShelfBlockEntity.BOOKS, 0).build())).size();
    int full = quads(shelfModel, shelf, withData(mc, ModelData.builder().with(RetexturedHelper.BLOCK_PROPERTY, Blocks.OAK_SLAB).with(ShelfBlockEntity.BOOKS, 0xFFFF).build())).size();
    check("shelf model adds books from the model data (" + empty + " -> " + full + " quads)", full > empty, empty + " -> " + full);
    ItemStack shelfItem = RetexturedHelper.setTexture(new ItemStack(InspirationsBuilding.shelf.get(ShelfType.NORMAL)), Blocks.CRIMSON_SLAB);
    Set<Identifier> crimson = sprites(look(mc, shelfItem).quads());
    check("shelf item model takes the texture from the component", crimson.contains(Identifier.withDefaultNamespace("block/crimson_planks")), crimson);
    BlockState bush = InspirationsBuilding.enlightenedBush.get(BushType.WHITE).defaultBlockState();
    Set<Identifier> leaves = sprites(quads(shaper.getBlockModel(bush), bush, withData(mc, RetexturedHelper.getModelData(Blocks.BIRCH_LEAVES))));
    check("enlightened bush model takes the texture of birch leaves", leaves.contains(Identifier.withDefaultNamespace("block/birch_leaves")), leaves);
    var bushLayer = shaper.getBlockModel(bush).collectParts(withData(mc, RetexturedHelper.getModelData(Blocks.BIRCH_LEAVES)), BlockPos.ZERO, bush, RandomSource.create(42)).get(0).getRenderType(bush);
    check("retextured enlightened bush keeps its cutout render type", bushLayer != net.minecraft.client.renderer.chunk.ChunkSectionLayer.SOLID, bushLayer);
    check("shelf item positions loaded (Kilnwork item lists)", RenderItem.ITEM_LISTS.get(shelf, List.of()).size() == 16, RenderItem.ITEM_LISTS.get(shelf, List.of()).size());
    // config resource pack: the 3D cauldron item model
    check("config resource pack is on", mc.getResourcePackRepository().getSelectedIds().contains(Inspirations.getResource("config_resources").toString()), mc.getResourcePackRepository().getSelectedIds());
    check("cauldron item uses the block model (config pack)", look(mc, new ItemStack(Items.CAULDRON)).blockLight(), "flat");
  }

  private static void checkColors(Minecraft mc) {
    var blockColors = mc.getBlockColors();
    BlockState dye = mc.level.getBlockState(at(1, 1, 6));
    int dyeColor = blockColors.getColor(dye, mc.level, at(1, 1, 6), 0);
    check("dye cauldron tint is the red dye", dyeColor == (0xFF000000 | MiscUtil.getColor(DyeColor.RED)), Integer.toHexString(dyeColor));
    BlockState potion = mc.level.getBlockState(at(2, 1, 6));
    int potionColor = blockColors.getColor(potion, mc.level, at(2, 1, 6), 0);
    check("potion cauldron tint is the swiftness color", potionColor == new PotionContents(Potions.SWIFTNESS).getColor(), Integer.toHexString(potionColor));
    int portal = blockColors.getColor(mc.level.getBlockState(at(16, 2, 13)), mc.level, at(16, 2, 13), 0);
    check("portal over blue glass is tinted blue", portal == (0xFF000000 | MiscUtil.getColor(DyeColor.BLUE)), Integer.toHexString(portal));
    int book = itemTint(mc, MiscUtil.setColor(new ItemStack(InspirationsBuilding.coloredBook), 0x3366CC), 0);
    check("colored book item color is opaque ARGB", book == 0xFF3366CC, Integer.toHexString(book));
    int bottle = itemTint(mc, new ItemStack(InspirationsCaudrons.simpleDyedWaterBottle.get(DyeColor.LIME)), 0);
    check("dyed bottle item color is the lime dye", bottle == (0xFF000000 | MiscUtil.getColor(DyeColor.LIME)), Integer.toHexString(bottle));
    List<ItemStack> hotbar = hotbarItems(mc.getSingleplayerServer().overworld());
    int sharpness = itemTint(mc, hotbar.get(1), 0);
    int mending = itemTint(mc, hotbar.get(2), 0);
    check("enchanted book ribbons: common red, rare green", sharpness == 0xFFFF2151 && mending == 0xFF00FF21, Integer.toHexString(sharpness) + " " + Integer.toHexString(mending));
    int rocketTop = itemTint(mc, hotbar.get(3), 1);
    check("firework item shows the second star's color", rocketTop == 0xFF2020FF, Integer.toHexString(rocketTop));
    int vine = blockColors.getColor(InspirationsBuilding.vine.defaultBlockState(), mc.level, at(12, 2, 2), 0);
    check("vine rope is tinted like foliage", vine != -1 && (vine & 0xFFFFFF) != 0xFFFFFF, Integer.toHexString(vine));
  }

  private static void checkClientData(Minecraft mc) {
    // fluid textures from client extensions
    var atlas = mc.getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS);
    Identifier honey = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(InspirationsCaudrons.honeyType).getStillTexture();
    check("honey fluid texture is in the block atlas", !atlas.getSprite(honey).contents().name().equals(MissingTextureAtlasSprite.getLocation()), honey);
    for (String sprite : List.of("fluid/colorless_still", "fluid/potion", "fluid/suspicious_stew", "entity/lectern_book")) {
      Identifier id = Inspirations.getResource(sprite);
      check("sprite " + sprite + " is in the block atlas", !atlas.getSprite(id).contents().name().equals(MissingTextureAtlasSprite.getLocation()), id);
    }
    // the client asked the server for the lectern book, the tinted renderer needs it
    check("lectern book reached the client", mc.level.getBlockEntity(at(13, 1, 12)) instanceof LecternBlockEntity lectern && lectern.getBook().is(InspirationsBuilding.coloredBook),
      mc.level.getBlockEntity(at(13, 1, 12)) instanceof LecternBlockEntity lectern ? lectern.getBook() : null);
    // client copies of block entity data
    check("shelf contents reached the client", mc.level.getBlockEntity(at(0, 1, 2)) instanceof ShelfBlockEntity be && be.getInventory().getStackInSlot(10).is(Items.APPLE) && be.getTexture() == Blocks.OAK_SLAB, "");
    check("dye and potion cauldron data reached the client", mc.level.getBlockEntity(at(2, 1, 6)) instanceof PotionCauldronBlockEntity be && be.getPotion() != null && be.getPotion().is(Potions.SWIFTNESS), "");
    // boiling particles
    Particle particle = mc.particleEngine.createParticle(InspirationsCaudrons.boilingParticle, stage.getX(), stage.getY() + 2.5, stage.getZ() + 6, 0, 0, 0);
    check("boiling particle is created", particle instanceof BoilingParticle, particle);
    // item model properties
    float barometer = new ItemColorSources.Property("height").get(new ItemStack(InspirationsTools.barometer), mc.level, mc.player, 0);
    float photometer = new ItemColorSources.Property("light").get(new ItemStack(InspirationsTools.photometer), mc.level, mc.player, 0);
    check("barometer and photometer properties", barometer >= 0 && barometer <= 1 && photometer >= 0 && photometer <= 15, barometer + " " + photometer);
    // tooltips
    List<Component> tooltip = RetexturedHelper.setTexture(new ItemStack(InspirationsBuilding.shelf.get(ShelfType.NORMAL)), Blocks.BIRCH_SLAB)
      .getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.Default.NORMAL);
    check("shelf tooltip names the texture block", tooltip.stream().anyMatch(c -> c.getString().equals(Blocks.BIRCH_SLAB.getName().getString())), tooltip);
  }

  /** Keys the mod shows: names of its registry entries and the texts of its code */
  private static Set<String> neededKeys() {
    Set<String> keys = new TreeSet<>();
    for (Item item : BuiltInRegistries.ITEM) {
      if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Inspirations.modID)) {
        keys.add(item.getDescriptionId());
      }
    }
    for (Block block : BuiltInRegistries.BLOCK) {
      if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Inspirations.modID)) {
        keys.add(block.getDescriptionId());
      }
    }
    for (FluidType type : NeoForgeRegistries.FLUID_TYPES) {
      Identifier id = NeoForgeRegistries.FLUID_TYPES.getKey(type);
      if (id != null && id.getNamespace().equals(Inspirations.modID)) {
        keys.add(type.getDescriptionId());
      }
    }
    keys.addAll(List.of("gui.inspirations.shelf.name", "gui.inspirations.collector", "gui.inspirations.pipe", "pack.inspirations.config.name", "pack.inspirations.config.description",
      "inspirations.lock.success", "inspirations.lock.fail.locked", "inspirations.lock.fail.blank", "inspirations.unlock.success", "inspirations.unlock.fail.no_match",
      "inspirations.unlock.fail.unlocked", "item.inspirations.lodestone_dimension_compass", "death.attack.inspirations.boiling"));
    return keys;
  }

  /** Every name of the mod is in the current language, and the mod's language file has no keys nothing shows */
  private static void checkTranslations(Minecraft mc) {
    Language language = Language.getInstance();
    Set<String> needed = neededKeys();
    List<String> missing = new ArrayList<>();
    for (String key : needed) {
      if (!language.has(key)) {
        missing.add(key);
      }
    }
    if (!language.has("block.kilnwork.retextured.id")) {
      missing.add("block.kilnwork.retextured.id");
    }
    check("translations: every name in " + LANG, missing.isEmpty(), missing);
    List<String> unused = new ArrayList<>();
    Identifier file = Inspirations.getResource("lang/" + LANG + ".json");
    try (var reader = mc.getResourceManager().openAsReader(file)) {
      for (String key : com.google.gson.JsonParser.parseReader(reader).getAsJsonObject().keySet()) {
        if (!needed.contains(key)) {
          unused.add(key);
        }
      }
    } catch (Exception e) {
      unused.add(file + ": " + e);
    }
    check("translations: no unused keys in " + LANG, unused.isEmpty(), unused);
  }

  /**
   * Counts the missing texture's magenta pixels of the frame: a model, item or screen without its texture shows the
   * magenta and black checkers
   */
  private static void checkNoMissingTexture(Minecraft mc, String name) {
    // the frame is read back from the GPU later since 1.21.5, the check runs in the callback
    Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> {
      int magenta = 0;
      try (image) {
        for (int pixel : image.getPixelsABGR()) {
          // ABGR: red low byte
          int r = pixel & 0xFF, g = pixel >> 8 & 0xFF, b = pixel >> 16 & 0xFF;
          if (r > 200 && b > 200 && g < 40) {
            magenta++;
          }
        }
      }
      check(name + ": no missing texture on screen", magenta < 50, magenta + " magenta pixels");
    });
  }
}
