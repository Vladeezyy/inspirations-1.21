package knightminer.inspirations.common.registration;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

/** Block registration, API of Mantle 1.20's BlockRegistryAdapter (MIT, SlimeKnights) */
public class BlockRegistryAdapter extends RegistryAdapter<Block> {
  public BlockRegistryAdapter(RegisterEvent.RegisterHelper<Block> helper) {
    super(helper);
  }

  /** Registers the block of a fluid, named {@code <name>_fluid} */
  public LiquidBlock registerFluidBlock(Supplier<? extends FlowingFluid> fluid, MapColor color, int light, String name) {
    return make(() -> new LiquidBlock(fluid.get(), BlockBehaviour.Properties.of()
      .mapColor(color).replaceable().noCollision().strength(100.0F).pushReaction(PushReaction.POPPED)
      .noLootTable().liquid().lightLevel(state -> light)), name + "_fluid");
  }
}
