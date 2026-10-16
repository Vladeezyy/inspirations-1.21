package knightminer.inspirations.cauldrons;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.cauldrons.block.entity.DyeCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.cauldrons.client.BoilingParticle;
import knightminer.inspirations.cauldrons.item.MixedDyedBottleItem;
import knightminer.inspirations.common.AbstractClientEvents;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.util.FastColor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import knightminer.inspirations.Inspirations;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT, bus = Bus.MOD)
public class CauldronsClientEvents extends AbstractClientEvents {
  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    ItemBlockRenderTypes.setRenderLayer(InspirationsCaudrons.honey, RenderType.translucent());
    ItemBlockRenderTypes.setRenderLayer(InspirationsCaudrons.honey.getFlowing(), RenderType.translucent());
  }

  @SubscribeEvent
  static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
    BlockColors colors = event.getBlockColors();
    colors.register((state, level, pos, index) -> {
      if (index == 0 && level != null && pos != null) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be.getType() == InspirationsCaudrons.dyeCauldronEntity) {
          return FastColor.ARGB32.opaque(((DyeCauldronBlockEntity) be).getColor());
        }
      }
      return -1;
    }, InspirationsCaudrons.dyeCauldron);
    colors.register((state, level, pos, index) -> {
      if (index == 0 && level != null && pos != null) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be.getType() == InspirationsCaudrons.potionCauldronEntity) {
          PotionCauldronBlockEntity cauldron = (PotionCauldronBlockEntity) be;
          return cauldron.getPotion() == null ? -1 : FastColor.ARGB32.opaque(cauldron.getPotionContents().getColor());
        }
      }
      return -1;
    }, InspirationsCaudrons.potionCauldron);
  }

  @SubscribeEvent
  static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(InspirationsCaudrons.boilingParticle, BoilingParticle.Factory::new);
  }

  @SubscribeEvent
  static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    ItemColors itemColors = event.getItemColors();

    // dyed water bottles
    InspirationsCaudrons.simpleDyedWaterBottle.forEach((color, bottle) -> itemColors.register((stack, index) -> index == 0 ? FastColor.ARGB32.opaque(MiscUtil.getColor(color)) : -1, bottle));
    registerItemColors(itemColors, (stack, index) -> index == 0 ? FastColor.ARGB32.opaque(MixedDyedBottleItem.dyeFromBottle(stack)) : -1, InspirationsCaudrons.mixedDyedWaterBottle);
    event.register(fluidColor(InspirationsCaudrons.mushroomStewType), InspirationsCaudrons.mushroomStewBucket);
    event.register(fluidColor(InspirationsCaudrons.potatoSoupType), InspirationsCaudrons.potatoSoupBucket);
    event.register(fluidColor(InspirationsCaudrons.rabbitStewType), InspirationsCaudrons.rabbitStewBucket);
    event.register(fluidColor(InspirationsCaudrons.beetrootSoupType), InspirationsCaudrons.beetrootSoupBucket);
    event.register(fluidColor(InspirationsCaudrons.honeyType), InspirationsCaudrons.honeyBucket);
  }

  /** Gets the fluid color for a bucket */
  private static ItemColor fluidColor(FluidType type) {
    IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(type);
    return (stack, index) -> index == 1 ? extensions.getTintColor() : -1;
  }

  /** Still and flowing textures and tint of a fluid, from the fluid texture JSON Mantle read in 1.20 */
  private static IClientFluidTypeExtensions fluidTexture(String texture, int color) {
    ResourceLocation still = Inspirations.getResource("fluid/" + texture + "_still");
    ResourceLocation flowing = Inspirations.getResource("fluid/" + texture + "_flowing");
    return new IClientFluidTypeExtensions() {
      @Override
      public ResourceLocation getStillTexture() {
        return still;
      }

      @Override
      public ResourceLocation getFlowingTexture() {
        return flowing;
      }

      @Override
      public int getTintColor() {
        return color;
      }
    };
  }

  @SubscribeEvent
  static void registerClientExtensions(RegisterClientExtensionsEvent event) {
    event.registerFluidType(fluidTexture("colorless", 0xFFCD8C6F), InspirationsCaudrons.mushroomStewType);
    event.registerFluidType(fluidTexture("colorless", 0xFF84160D), InspirationsCaudrons.beetrootSoupType);
    event.registerFluidType(fluidTexture("colorless", 0xFF984A2C), InspirationsCaudrons.rabbitStewType);
    event.registerFluidType(fluidTexture("colorless", 0xFFF2DA9F), InspirationsCaudrons.potatoSoupType);
    event.registerFluidType(fluidTexture("transparent", 0xFFFF9116), InspirationsCaudrons.honeyType);
  }
}
