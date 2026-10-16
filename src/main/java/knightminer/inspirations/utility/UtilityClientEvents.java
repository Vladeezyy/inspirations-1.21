package knightminer.inspirations.utility;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.AbstractClientEvents;
import knightminer.inspirations.common.client.BackgroundContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT, bus = Bus.MOD)
public class UtilityClientEvents extends AbstractClientEvents {
  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    // Register GUIs.
    registerScreenFactory(InspirationsUtility.contCollector, new BackgroundContainerScreen.Factory<>(166, ResourceLocation.parse("textures/gui/container/dispenser.png")));
    registerScreenFactory(InspirationsUtility.contPipe, new BackgroundContainerScreen.Factory<>(133, "pipe"));
  }
}
