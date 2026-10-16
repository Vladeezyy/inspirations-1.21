package knightminer.inspirations.common.registration;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Fluid registration, API of Mantle 1.20's FluidRegistryAdapter (MIT, SlimeKnights) */
public class FluidRegistryAdapter extends RegistryAdapter<Fluid> {
  public FluidRegistryAdapter(RegisterEvent.RegisterHelper<Fluid> helper) {
    super(helper);
  }

  /** Registers the still fluid {@code <name>} and the flowing fluid {@code flowing_<name>}, returns the still one */
  public BaseFlowingFluid register(FluidBuilder builder, String name) {
    BaseFlowingFluid[] fluids = new BaseFlowingFluid[2];
    BaseFlowingFluid.Properties props = builder.build(() -> fluids[0], () -> fluids[1]);
    fluids[0] = register(new BaseFlowingFluid.Source(props), name);
    fluids[1] = register(new BaseFlowingFluid.Flowing(props), "flowing_" + name);
    return fluids[0];
  }
}
