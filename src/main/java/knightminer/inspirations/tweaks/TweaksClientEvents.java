package knightminer.inspirations.tweaks;

import net.minecraft.world.level.block.entity.BlockEntityTypes;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.AbstractClientEvents;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.tweaks.client.PortalColorHandler;
import knightminer.inspirations.tweaks.client.TintedLecternRenderer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
public class TweaksClientEvents extends AbstractClientEvents {
  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    NeoForge.EVENT_BUS.addListener(TweaksClientEvents::fixShieldTooltip);
  }

  @SubscribeEvent
  static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    if (Config.tintLecternBook.getAsBoolean()) {
      // replaces the vanilla lectern renderer
      event.registerBlockEntityRenderer(BlockEntityTypes.LECTERN, TintedLecternRenderer::new);
    }
  }


  @SubscribeEvent
  static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {

    // coloring on sugar cane crop to match reeds
    registerBlockColors(event, (state, world, pos, index) -> {
      if (world == null || pos == null) {
        return -1;
      }
      return BiomeColors.getAverageGrassColor(world, pos);
    }, InspirationsTweaks.sugarCane);

    // portal tinting
    registerBlockColors(event, PortalColorHandler.INSTANCE, Blocks.NETHER_PORTAL);
  }

  @SubscribeEvent
  static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources event) {

    // colored ribbons on enchanted books
    registerItemColors((stack, tintIndex) -> {
      if (tintIndex == 0 && Config.get(Config.coloredEnchantedRibbons)) {
        // find the rarest enchantment we have; 1.21 has weights instead of rarities: 10 common, 5 uncommon, 2 rare, 1 very rare
        int weight = Integer.MAX_VALUE;
        for (Holder<Enchantment> enchantment : stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).keySet()) {
          weight = Math.min(weight, enchantment.value().getWeight());
        }

        // color by that rarity
        int color;
        if (weight >= 10) {
          color = 0xFF2151;
        } else if (weight >= 5) {
          color = 0xE2882D;
        } else if (weight >= 2) {
          color = 0x00FF21;
        } else {
          color = 0x9F7FFF;
        }
        return ARGB.opaque(color);
      }
      return -1;
    }, Items.ENCHANTED_BOOK);

    registerItemColors((stack, tintIndex) -> {
      if (!Config.get(Config.coloredFireworkItems)) {
        return -1;
      }
      Fireworks fireworks = stack.get(DataComponents.FIREWORKS);
      // string is darker with more gunpowder
      if (tintIndex == 2) {
        int color = 0xA0A0A0;
        if (fireworks != null) {
          int flight = fireworks.flightDuration();
          if (flight == 1) {
            color = 0x808080;
          } else if (flight == 2) {
            color = 0x606060;
          } else if (flight == 3) {
            color = 0x303030;
          } else if (flight > 3) {
            color = 0x000000;
          }
        }
        return ARGB.opaque(color);
      }
      // color the stripes and the top
      if (tintIndex == 0 || tintIndex == 1) {
        // no stars?
        int missing = ARGB.opaque(tintIndex == 1 ? 0xCCA190 : 0xC0C0C0);
        if (fireworks == null || tintIndex >= fireworks.explosions().size()) {
          return missing;
        }

        // grab the proper star's first color
        IntList colors = fireworks.explosions().get(tintIndex).colors();
        return colors.isEmpty() ? missing : ARGB.opaque(colors.getInt(0));
      }

      return -1;
    }, Items.FIREWORK_ROCKET);
  }

  // registered with Forge bus
  private static void fixShieldTooltip(ItemTooltipEvent event) {
    if (!Config.fixShieldTooltip.getAsBoolean()) return;
    ItemStack stack = event.getItemStack();
    if (stack.getItem() != Items.SHIELD) return;

    // only need to run if it has patterns and is enchanted
    if (!stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().isEmpty() && stack.isEnchanted()) {
      // find the last banner pattern line in the tooltip
      List<Component> text = event.getToolTip();
      int i = text.size() - 1;
      for (; i >= 0; i--) {
        Component component = text.get(i);
        if (component.getContents() instanceof TranslatableContents translatable && translatable.getKey().contains("banner")) {
          text.add(i + 1, Component.empty());
          break;
        }
      }
    }
  }
}
