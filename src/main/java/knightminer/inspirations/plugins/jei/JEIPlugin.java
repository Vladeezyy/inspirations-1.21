package knightminer.inspirations.plugins.jei;

import dev.vladeezyy.kilnwork.retexture.RetexturedHelper;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.library.MiscUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return Inspirations.getResource("jei");
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registry) {
    IIngredientSubtypeInterpreter<ItemStack> texture = (ingredient, context) -> RetexturedHelper.getTextureName(RetexturedHelper.getTexture(ingredient));
    Consumer<ItemLike> setTextureSubtype = item -> registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item.asItem(), texture);

    // building
    InspirationsBuilding.shelf.values().forEach(setTextureSubtype);
    InspirationsBuilding.enlightenedBush.values().forEach(setTextureSubtype);
    registry.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, InspirationsBuilding.coloredBook, (stack, context) -> context == UidContext.Ingredient ? Integer.toHexString(MiscUtil.getColor(stack)) : "");
  }
}
