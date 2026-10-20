package knightminer.inspirations.tools;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.AbstractClientEvents;
import knightminer.inspirations.library.MiscUtil;
import knightminer.inspirations.tools.client.BarometerPropertyGetter;
import knightminer.inspirations.tools.client.DimensionCompassPropertyGetter;
import knightminer.inspirations.tools.client.NorthCompassPropertyGetter;
import knightminer.inspirations.tools.client.PhotometerPropertyGetter;
import knightminer.inspirations.tools.client.RedstoneArrowRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
public class ToolsClientEvents extends AbstractClientEvents {

  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    // item model properties
    event.enqueueWork(() -> {
      registerModelProperty(InspirationsTools.northCompass, "angle", new NorthCompassPropertyGetter());
      registerModelProperty(InspirationsTools.barometer, "height", new BarometerPropertyGetter());
      registerModelProperty(InspirationsTools.photometer, "light", new PhotometerPropertyGetter());
      registerModelProperty(InspirationsTools.dimensionCompass, "angle", new DimensionCompassPropertyGetter());
    });
  }

  @SubscribeEvent
  static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(InspirationsTools.entRSArrow, RedstoneArrowRenderer::new);
  }

  @SubscribeEvent
  static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources event) {

    // coloring of books for normal bookshelf
    registerItemColors((stack, tintIndex) -> {
      if (tintIndex == 1 && MiscUtil.hasColor(stack)) {
        return ARGB.opaque(MiscUtil.getColor(stack));
      }
      return tintIndex == 0 ? -1 : ARGB.opaque(0x2CCDB1);
    }, InspirationsTools.dimensionCompass);
  }
}
