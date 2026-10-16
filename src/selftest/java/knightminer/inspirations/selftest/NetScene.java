package knightminer.inspirations.selftest;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.DyeCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.library.MiscUtil;
import knightminer.inspirations.tools.capability.DimensionCompass;
import knightminer.inspirations.tweaks.TweaksEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Multiplayer check ({@code tools/nettest.sh}), inert unless {@code inspirations.scene} is {@code net}: the dev client
 * joins a dev dedicated server as an operator, builds with commands and uses blocks and entities like a player, then
 * checks what the server synced: a book put on a shelf, the color of a dyed cauldron, a potion poured in and taken
 * back, the book of a lectern, the collector screen, the milk cooldown of a cow, the dimension compass position after a
 * trip to the nether. Logs {@code [scene] CHECK/DONE}, then quits.
 */
@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
public final class NetScene {
  private static final boolean ENABLED = System.getProperty("inspirations.scene", "").equals("net");
  private static final int Y = 200;
  private static final BlockPos SHELF = new BlockPos(2, Y, 3), WATER = new BlockPos(3, Y, 3), EMPTY = new BlockPos(4, Y, 3),
    LECTERN = new BlockPos(5, Y, 3), COLLECTOR = new BlockPos(6, Y, 3);
  private static int tick, checks, failures;

  static void check(String name, boolean ok, Object detail) {
    checks++;
    if (!ok) {
      failures++;
    }
    Inspirations.log.info("[scene] CHECK " + (ok ? "OK " : "FAIL ") + name + (ok ? "" : " (" + detail + ")"));
  }

  private static void command(Minecraft mc, String command) {
    mc.player.connection.sendCommand(command);
  }

  private static void hold(Minecraft mc, String item) {
    command(mc, "item replace entity @s weapon.mainhand with " + item);
  }

  /** Right click on the north face of the block, as a player does */
  private static void use(Minecraft mc, BlockPos pos, double x, double y) {
    Vec3 hit = new Vec3(pos.getX() + x, pos.getY() + y, pos.getZ());
    mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, Direction.NORTH, pos, false));
  }

  @SubscribeEvent
  public static void onTick(ClientTickEvent.Post event) {
    if (!ENABLED) {
      return;
    }
    Minecraft mc = Minecraft.getInstance();
    if (mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
      mc.options.onboardAccessibility = false;
      mc.options.save();
      mc.setScreen(new TitleScreen());
      return;
    }
    if (mc.player == null || mc.getConnection() == null) {
      return;
    }
    mc.options.pauseOnLostFocus = false;
    mc.getToasts().clear();
    tick++;
    try {
      step(mc);
    } catch (RuntimeException e) {
      check("net step at tick " + tick + " ran", false, e);
      Inspirations.log.error("[scene] net step failed", e);
    }
  }

  private static void step(Minecraft mc) {
    switch (tick) {
      case 40 -> {
        check("joined the dedicated server with the mod", mc.getConnection().getConnection().isConnected() && mc.getSingleplayerServer() == null, "no connection");
        command(mc, "gamemode survival");
        command(mc, "time set day");
        command(mc, "fill -3 " + (Y - 1) + " -3 10 " + (Y - 1) + " 10 minecraft:stone");
        command(mc, "fill -3 " + Y + " -3 10 " + (Y + 3) + " 10 minecraft:air");
        command(mc, "setblock " + pos(SHELF) + " inspirations:shelf[facing=north]");
        command(mc, "setblock " + pos(WATER) + " minecraft:water_cauldron[level=3]");
        command(mc, "setblock " + pos(EMPTY) + " minecraft:cauldron");
        command(mc, "setblock " + pos(LECTERN) + " minecraft:lectern[facing=north,has_book=true]{Book:{id:\"inspirations:colored_book\",count:1,components:{\"minecraft:dyed_color\":{rgb:65280}}}}");
        command(mc, "setblock " + pos(COLLECTOR) + " inspirations:collector");
        command(mc, "summon minecraft:cow 4.5 " + Y + " 6.5 {NoAI:1b,Tags:[\"insp_cow\"]}");
        command(mc, "tp @s 4.5 " + Y + " 0.5 0 20");
      }
      // a red book on the shelf
      case 80 -> hold(mc, "inspirations:colored_book[minecraft:dyed_color={rgb:16711680}]");
      case 90 -> use(mc, SHELF, 0.2, 0.8);
      case 105 -> check("book put on a shelf reached the client", mc.level.getBlockEntity(SHELF) instanceof ShelfBlockEntity shelf && countBooks(shelf, 0xFF0000) == 1 && mc.player.getMainHandItem().isEmpty(),
        mc.level.getBlockEntity(SHELF) instanceof ShelfBlockEntity shelf ? countBooks(shelf, 0xFF0000) + " " + mc.player.getMainHandItem() : "no shelf");
      // red dye in water
      case 110 -> hold(mc, "minecraft:red_dye");
      case 120 -> use(mc, WATER, 0.5, 0.9);
      case 135 -> check("dyed cauldron color reached the client", mc.level.getBlockEntity(WATER) instanceof DyeCauldronBlockEntity dye && dye.getColor() == MiscUtil.getColor(DyeColor.RED),
        mc.level.getBlockEntity(WATER) instanceof DyeCauldronBlockEntity dye ? Integer.toHexString(dye.getColor()) : mc.level.getBlockState(WATER));
      // a potion in an empty cauldron, then back into a bottle
      case 140 -> hold(mc, "minecraft:potion[minecraft:potion_contents={potion:\"minecraft:swiftness\"}]");
      case 150 -> use(mc, EMPTY, 0.5, 0.9);
      case 165 -> {
        check("potion cauldron reached the client", mc.level.getBlockEntity(EMPTY) instanceof PotionCauldronBlockEntity potion && potion.getPotion() == Potions.SWIFTNESS && mc.player.getMainHandItem().is(Items.GLASS_BOTTLE),
          mc.level.getBlockState(EMPTY) + " " + mc.player.getMainHandItem());
        use(mc, EMPTY, 0.5, 0.9);
      }
      case 180 -> check("bottle filled back with the potion", mc.player.getMainHandItem().is(Items.POTION)
        && mc.player.getMainHandItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.SWIFTNESS), mc.player.getMainHandItem());
      // the lectern asked for its book when rendered
      case 185 -> check("lectern book reached the client", mc.level.getBlockEntity(LECTERN) instanceof LecternBlockEntity lectern && lectern.getBook().is(InspirationsBuilding.coloredBook),
        mc.level.getBlockEntity(LECTERN) instanceof LecternBlockEntity lectern ? lectern.getBook() : "no lectern");
      // collector screen
      case 190 -> hold(mc, "minecraft:air");
      case 200 -> use(mc, COLLECTOR, 0.5, 0.5);
      case 215 -> {
        check("collector screen opens over the network with 9 slots", mc.player.containerMenu.slots.size() == 9 + 36, mc.player.containerMenu.slots.size());
        mc.player.closeContainer();
      }
      // milk cooldown
      case 220 -> hold(mc, "minecraft:bucket");
      case 230 -> mc.gameMode.interact(mc.player, cow(mc), InteractionHand.MAIN_HAND);
      case 245 -> {
        check("cow milked into a bucket", mc.player.getMainHandItem().is(Items.MILK_BUCKET), mc.player.getMainHandItem());
        check("milk cooldown reached the client", cow(mc).getPersistentData().getShort(TweaksEvents.TAG_MILKCOOLDOWN) > 0, cow(mc).getPersistentData());
        hold(mc, "minecraft:bucket");
      }
      case 255 -> mc.gameMode.interact(mc.player, cow(mc), InteractionHand.MAIN_HAND);
      case 270 -> check("cow on cooldown gives no milk", mc.player.getMainHandItem().is(Items.BUCKET), mc.player.getMainHandItem());
      // dimension compass: a trip to the nether and back
      case 275 -> command(mc, "execute in minecraft:the_nether run tp @s 8.5 100 8.5");
      case 335 -> command(mc, "execute in minecraft:overworld run tp @s 4.5 " + Y + " 0.5");
      case 400 -> {
        BlockPos entered = DimensionCompass.get(mc.player).getEnteredPosition();
        check("dimension compass position reached the client after changing dimension", entered != null && entered.closerThan(new BlockPos(4, Y, 0), 3), entered);
      }
      case 410 -> {
        Inspirations.log.info("[scene] DONE " + (checks - failures) + "/" + checks + (failures == 0 ? " passed" : " (" + failures + " failed)"));
        mc.level.disconnect();
        mc.disconnect(new TitleScreen());
        mc.stop();
      }
      default -> { }
    }
  }

  private static String pos(BlockPos pos) {
    return pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  private static Cow cow(Minecraft mc) {
    for (Entity entity : mc.level.entitiesForRendering()) {
      if (entity instanceof Cow cow) {
        return cow;
      }
    }
    throw new IllegalStateException("no cow on the client");
  }

  private static int countBooks(ShelfBlockEntity shelf, int color) {
    int count = 0;
    for (int slot = 0; slot < 16; slot++) {
      if (shelf.getInventory().getStackInSlot(slot).is(InspirationsBuilding.coloredBook) && (MiscUtil.getColor(shelf.getInventory().getStackInSlot(slot)) & 0xFFFFFF) == color) {
        count++;
      }
    }
    return count;
  }
}
