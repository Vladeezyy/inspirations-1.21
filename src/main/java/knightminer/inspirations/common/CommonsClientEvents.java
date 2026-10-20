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
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import knightminer.inspirations.common.client.ItemColorSources;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
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
      // the tints of the two models (TweaksClientEvents colors) come from their item definitions
      configPack.addItemDefinitionReplacement(Config.coloredEnchantedRibbons, Items.ENCHANTED_BOOK, "enchanted_book");
      configPack.addItemDefinitionReplacement(Config.coloredFireworkItems, Items.FIREWORK_ROCKET, "fireworks");
      configPack.addItemModelReplacement(Config.betterCauldronItem, Items.CAULDRON, "cauldron");
    }
  }

  @SubscribeEvent
  static void reloadListeners(AddClientReloadListenersEvent event) {
    event.addListener(Inspirations.getResource("client_util"), ClientUtil.RELOAD_LISTENER);
  }

  @SubscribeEvent
  static void tintSources(RegisterColorHandlersEvent.ItemTintSources event) {
    event.register(ItemColorSources.TINT_ID, ItemColorSources.Tint.MAP_CODEC);
  }

  @SubscribeEvent
  static void itemProperties(RegisterRangeSelectItemModelPropertyEvent event) {
    event.register(ItemColorSources.PROPERTY_ID, ItemColorSources.Property.MAP_CODEC);
  }
}
