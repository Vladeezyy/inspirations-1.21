package knightminer.inspirations.utility;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.AbstractClientEvents;
import knightminer.inspirations.common.client.BackgroundContainerScreen;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
public class UtilityClientEvents extends AbstractClientEvents {
  @SubscribeEvent
  static void registerScreens(RegisterMenuScreensEvent event) {
    // Register GUIs.
    registerScreenFactory(event, InspirationsUtility.contCollector, new BackgroundContainerScreen.Factory<>(166, Identifier.withDefaultNamespace("textures/gui/container/dispenser.png")));
    registerScreenFactory(event, InspirationsUtility.contPipe, new BackgroundContainerScreen.Factory<>(133, "pipe"));
  }
}
