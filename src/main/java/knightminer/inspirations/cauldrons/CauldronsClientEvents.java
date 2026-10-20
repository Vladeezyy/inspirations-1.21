package knightminer.inspirations.cauldrons;

import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.client.fluid.FluidTintSources;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.FluidModel;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.cauldrons.block.entity.DyeCauldronBlockEntity;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.cauldrons.client.BoilingParticle;
import knightminer.inspirations.cauldrons.item.MixedDyedBottleItem;
import knightminer.inspirations.common.AbstractClientEvents;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.util.ARGB;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Inspirations.modID, value = Dist.CLIENT)
public class CauldronsClientEvents extends AbstractClientEvents {
  @SubscribeEvent
  static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
    registerBlockColors(event, (state, level, pos, index) -> {
      if (index == 0 && level != null && pos != null) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be.getType() == InspirationsCaudrons.dyeCauldronEntity) {
          return ARGB.opaque(((DyeCauldronBlockEntity) be).getColor());
        }
      }
      return -1;
    }, InspirationsCaudrons.dyeCauldron);
    registerBlockColors(event, (state, level, pos, index) -> {
      if (index == 0 && level != null && pos != null) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be.getType() == InspirationsCaudrons.potionCauldronEntity) {
          PotionCauldronBlockEntity cauldron = (PotionCauldronBlockEntity) be;
          return cauldron.getPotion() == null ? -1 : ARGB.opaque(cauldron.getPotionContents().getColor());
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
  static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources event) {

    // dyed water bottles
    InspirationsCaudrons.simpleDyedWaterBottle.forEach((color, bottle) -> registerItemColors((stack, index) -> index == 0 ? ARGB.opaque(MiscUtil.getColor(color)) : -1, bottle));
    registerItemColors((stack, index) -> index == 0 ? ARGB.opaque(MixedDyedBottleItem.dyeFromBottle(stack)) : -1, InspirationsCaudrons.mixedDyedWaterBottle);
  }


  /** Fluid model with the still and flowing textures and the tint of the fluid texture JSON Mantle read in 1.20 */
  private static void fluidModel(RegisterFluidModelsEvent event, String texture, int color, BaseFlowingFluid fluid) {
    Material still = new Material(Inspirations.getResource("fluid/" + texture + "_still"));
    Material flowing = new Material(Inspirations.getResource("fluid/" + texture + "_flowing"));
    // the layer (translucent honey) follows the texture since 26.1
    event.register(new FluidModel.Unbaked(still, flowing, null, FluidTintSources.constant(color)), fluid.getSource(), fluid.getFlowing());
  }

  @SubscribeEvent
  static void registerFluidModels(RegisterFluidModelsEvent event) {
    fluidModel(event, "colorless", 0xFFCD8C6F, InspirationsCaudrons.mushroomStew);
    fluidModel(event, "colorless", 0xFF84160D, InspirationsCaudrons.beetrootSoup);
    fluidModel(event, "colorless", 0xFF984A2C, InspirationsCaudrons.rabbitStew);
    fluidModel(event, "colorless", 0xFFF2DA9F, InspirationsCaudrons.potatoSoup);
    fluidModel(event, "transparent", 0xFFFF9116, InspirationsCaudrons.honey);
  }
}
