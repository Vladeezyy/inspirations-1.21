package knightminer.inspirations.building;

import knightminer.inspirations.common.client.ItemColorSources;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import knightminer.inspirations.building.block.type.BushType;
import knightminer.inspirations.building.block.type.ShelfType;
import knightminer.inspirations.building.client.ShelfBlockEntityRenderer;
import knightminer.inspirations.building.client.ShelfContainerScreen;
import knightminer.inspirations.common.AbstractClientEvents;
import knightminer.inspirations.library.MiscUtil;
import knightminer.inspirations.library.client.ClientUtil;
import knightminer.inspirations.library.client.model.ShelfModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import dev.vladeezyy.kilnwork.client.KilnworkClient;
import knightminer.inspirations.building.block.ShelfBlock;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import dev.vladeezyy.kilnwork.retexture.IRetexturedBlockEntity;
import dev.vladeezyy.kilnwork.util.BlockEntityHelper;
import dev.vladeezyy.kilnwork.retexture.RetexturedHelper;

import java.util.Optional;

@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
public class BuildingClientEvents extends AbstractClientEvents {
  private static final Minecraft mc = Minecraft.getInstance();

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(InspirationsBuilding.shelfTileEntity, ShelfBlockEntityRenderer::new);
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
    event.register(Inspirations.getResource("shelf"), ShelfModel.LOADER);
  }

  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    // the shelf model keeps the books of the filled slots and takes the texture of the block entity
    KilnworkClient.wrapBlockModels(block -> block instanceof ShelfBlock, ShelfModel.Block::new);
  }

  @SubscribeEvent
  static void registerScreens(RegisterMenuScreensEvent event) {
    // Register GUIs.
    registerScreenFactory(event, InspirationsBuilding.shelfContainer, ShelfContainerScreen::new);
  }

  @SubscribeEvent
  static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {

    // coloring of books for normal bookshelf, tint index 1 to 16 for the slots
    registerBlockColors(event, 17, (state, world, pos, tintIndex) -> {
      if (tintIndex > 0 && tintIndex <= 16 && world != null && pos != null) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ShelfBlockEntity) {
          ItemStack stack = ((ShelfBlockEntity)te).getInventory().getStackInSlot(tintIndex - 1);
          if (!stack.isEmpty()) {
            int color = ClientUtil.getItemColor(stack.getItem());
            int itemColors = ItemColorSources.getColor(stack, 0);
            if (itemColors > -1) {
              // combine twice to make sure the item colors result is dominant
              color = MiscUtil.combineColors(color, itemColors, 3);
            }
            return color;
          }
        }
      }

      return -1;
    }, InspirationsBuilding.shelf.getOrNull(ShelfType.NORMAL));

    // rope vine coloring
    registerBlockColors(event, (state, world, pos, tintIndex) -> {
      if (world != null && pos != null) {
        return BiomeColors.getAverageFoliageColor(world, pos);
      }
      return FoliageColor.FOLIAGE_DEFAULT;
    }, InspirationsBuilding.vine);

    // bush block coloring
    // First the three which never change tint.
    InspirationsBuilding.enlightenedBush.forEach((type, bush) -> {
      if (type != BushType.WHITE) {
        int color = ARGB.opaque(type.getColor()); // Make closure capture just the int.
        registerBlockColors(event, (state, world, pos, tintIndex) -> color, bush);
      }
    });

    // white copies the default leaf colors
    registerBlockColors(event, (state, world, pos, tintIndex) -> {
      if (tintIndex != 0 || world == null || pos == null) {
        return -1;
      }
      if (world.getBlockEntity(pos) instanceof IRetexturedBlockEntity te) {
        Block block = te.getTexture();
        if (block != Blocks.AIR) {
          return ClientUtil.getStackBlockColorsSafe(new ItemStack(block), world, pos, 0);
        }
      }
      return FoliageColor.FOLIAGE_DEFAULT;
    }, InspirationsBuilding.enlightenedBush.getOrNull(BushType.WHITE));
  }

  @SubscribeEvent
  static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources event) {

    // coloring of books for normal bookshelf
    registerItemColors((stack, tintIndex) -> {
      if (tintIndex > 0 && tintIndex <= 16) {
        return ARGB.opaque(0x654B17);
      }
      return -1;
    }, InspirationsBuilding.shelf.getOrNull(ShelfType.NORMAL));

    // book covers, too lazy to make 16 cover textures
    // 1.21 item colors are ARGB; an uncolored book keeps the leather default color it had in 1.20
    registerItemColors((stack, index) -> index == 0 ? ARGB.opaque(DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR)) : -1, InspirationsBuilding.coloredBook);

    // bush block colors
    // First the three blocks which never change tint.
    InspirationsBuilding.enlightenedBush.forEach((type, bush) -> {
      if (type != BushType.WHITE) {
        int color = ARGB.opaque(type.getColor());
        registerItemColors((stack, tintIndex) -> tintIndex == 0 ? color : -1, bush);
      }
    });

    // The main one uses the tint of the textured stack
    registerItemColors((stack, tintIndex) -> {
      if (tintIndex != 0) {
        return -1;
      }
      // redirect to block for colors
      Block block = RetexturedHelper.getTexture(stack);
      if (block != Blocks.AIR) {
        // the default color of the texture block (its block colors without a world)
        return ClientUtil.getStackBlockColorsSafe(new ItemStack(block), null, null, 0);
      } else {
        return FoliageColor.FOLIAGE_DEFAULT;
      }
    }, InspirationsBuilding.enlightenedBush.getOrNull(BushType.WHITE));

    // We can't get the world position of the item, so use the default tint.
    registerItemColors((stack, tintIndex) -> FoliageColor.FOLIAGE_DEFAULT, InspirationsBuilding.vine);
  }
}
