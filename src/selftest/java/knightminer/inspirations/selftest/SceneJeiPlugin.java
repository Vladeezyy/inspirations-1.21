package knightminer.inspirations.selftest;

import knightminer.inspirations.Inspirations;
import dev.vladeezyy.kilnwork.retexture.RetexturedHelper;
import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.building.block.type.BushType;
import knightminer.inspirations.building.block.type.ShelfType;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

/** Keeps the JEI runtime for the client scene, dev runs only */
@JeiPlugin
public class SceneJeiPlugin implements IModPlugin {
  @Nullable
  static IJeiRuntime runtime;

  @Override
  public Identifier getPluginUid() {
    return Inspirations.getResource("scene");
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    runtime = jeiRuntime;
  }

  /** The recipe of a creative shelf can be looked up (original issue #247) */
  static void checkJei(Minecraft mc) {
    if (runtime == null) {
      ClientScene.check("JEI runtime available", false, "no JEI");
      return;
    }
    var focus = runtime.getJeiHelpers().getFocusFactory();
    ItemStack creativeShelf = RetexturedHelper.setTexture(new ItemStack(InspirationsBuilding.shelf.get(ShelfType.NORMAL)), Blocks.OAK_SLAB);
    long shelves = runtime.getRecipeManager().createRecipeLookup(RecipeTypes.CRAFTING)
      .limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, creativeShelf))).get().count();
    ClientScene.check("JEI finds the recipe of a creative shelf (#247)", shelves > 0, shelves);
    ItemStack plainShelf = new ItemStack(InspirationsBuilding.shelf.get(ShelfType.RAINBOW));
    long plain = runtime.getRecipeManager().createRecipeLookup(RecipeTypes.CRAFTING)
      .limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, plainShelf))).get().count();
    ClientScene.check("JEI finds the recipes of an untextured rainbow shelf", plain >= 3, plain);
    ItemStack bush = RetexturedHelper.setTexture(new ItemStack(InspirationsBuilding.enlightenedBush.get(BushType.RED)), Blocks.OAK_LEAVES);
    long bushes = runtime.getRecipeManager().createRecipeLookup(RecipeTypes.CRAFTING)
      .limitFocus(List.of(focus.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, bush))).get().count();
    ClientScene.check("JEI finds the recipe of an enlightened bush", bushes > 0, bushes);
  }
}
