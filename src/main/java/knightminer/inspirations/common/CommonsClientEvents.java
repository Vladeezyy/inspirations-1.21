package knightminer.inspirations.common;

import com.google.common.collect.ImmutableSet;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.library.client.ClientUtil;
import knightminer.inspirations.library.client.ConfigurableResourcePack;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT, bus = Bus.MOD)
public class CommonsClientEvents extends AbstractClientEvents {
  public static ConfigurableResourcePack configPack;

  /**
   * Called during mod constructor to run early events
   */
  public static void onConstruct(IEventBus modBus) {
    if (!DatagenModLoader.isRunningDataGen()) {
      configPack = new ConfigurableResourcePack(
        Inspirations.class,
        Inspirations.getResource("config_resources"),
        Component.translatable("pack.inspirations.config.name"),
        Component.translatable("pack.inspirations.config.description"),
        ImmutableSet.of("minecraft"));
      modBus.addListener((AddPackFindersEvent event) -> {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
          event.addRepositorySource(configPack);
        }
      });

      // add model replacements to the config pack
      configPack.addBlockstateReplacement(Config.customPortalColor, Blocks.NETHER_PORTAL, "nether_portal");
      configPack.addItemModelReplacement(Config.coloredEnchantedRibbons, Items.ENCHANTED_BOOK, "enchanted_book");
      configPack.addItemModelReplacement(Config.coloredFireworkItems, Items.FIREWORK_ROCKET, "fireworks");
      configPack.addItemModelReplacement(Config.betterCauldronItem, Items.CAULDRON, "cauldron");
    }
  }

  @SubscribeEvent
  static void reloadListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(ClientUtil.RELOAD_LISTENER);
  }
}
