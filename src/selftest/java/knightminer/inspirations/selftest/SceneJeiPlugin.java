package knightminer.inspirations.selftest;

import knightminer.inspirations.Inspirations;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/** Keeps the JEI runtime for the client scene, dev runs only */
@JeiPlugin
public class SceneJeiPlugin implements IModPlugin {
  @Nullable
  static IJeiRuntime runtime;

  @Override
  public ResourceLocation getPluginUid() {
    return Inspirations.getResource("scene");
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    runtime = jeiRuntime;
  }
}
