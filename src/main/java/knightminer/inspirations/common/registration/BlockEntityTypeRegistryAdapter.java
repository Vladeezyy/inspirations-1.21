package knightminer.inspirations.common.registration;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

/** Block entity type registration, API of Mantle 1.20's BlockEntityTypeRegistryAdapter (MIT, SlimeKnights) */
public class BlockEntityTypeRegistryAdapter extends RegistryAdapter<BlockEntityType<?>> {
  public BlockEntityTypeRegistryAdapter(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
    super(helper);
  }

  public <T extends BlockEntity> BlockEntityType<T> register(BlockEntityType.BlockEntitySupplier<T> factory, Block block, String name) {
    return register(BlockEntityType.Builder.of(factory, block).build(null), name);
  }

  public <T extends BlockEntity> BlockEntityType<T> register(BlockEntityType.BlockEntitySupplier<T> factory, EnumObject<?, ? extends Block> blocks, String name) {
    return register(BlockEntityType.Builder.of(factory, blocks.values().toArray(new Block[0])).build(null), name);
  }

  public <T extends BlockEntity> BlockEntityType<T> register(BlockEntityType.BlockEntitySupplier<T> factory, List<? extends Block> blocks, String name) {
    return register(BlockEntityType.Builder.of(factory, new ArrayList<>(blocks).toArray(new Block[0])).build(null), name);
  }
}
