package knightminer.inspirations.common.registration;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

/** Properties of a flowing fluid pair, API of Mantle 1.20's FluidBuilder (MIT, SlimeKnights) */
public class FluidBuilder {
  private final Supplier<? extends FluidType> type;
  private Supplier<? extends LiquidBlock> block;
  private Supplier<? extends Item> bucket;

  private FluidBuilder(Supplier<? extends FluidType> type) {
    this.type = type;
  }

  public static FluidBuilder create(Supplier<? extends FluidType> type) {
    return new FluidBuilder(type);
  }

  public FluidBuilder block(Supplier<? extends LiquidBlock> block) {
    this.block = block;
    return this;
  }

  public FluidBuilder bucket(Supplier<? extends Item> bucket) {
    this.bucket = bucket;
    return this;
  }

  /** Properties for both fluids, given suppliers of the two */
  BaseFlowingFluid.Properties build(Supplier<? extends BaseFlowingFluid> still, Supplier<? extends BaseFlowingFluid> flowing) {
    BaseFlowingFluid.Properties props = new BaseFlowingFluid.Properties(type, still, flowing);
    if (block != null) {
      props.block(block);
    }
    if (bucket != null) {
      props.bucket(bucket);
    }
    return props;
  }
}
