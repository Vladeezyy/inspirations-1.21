package knightminer.inspirations.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.StemBlock;
import net.neoforged.neoforge.common.util.TriState;
import com.mojang.serialization.MapCodec;

public class MulchBlock extends FallingBlock {
  private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

  public static final MapCodec<MulchBlock> CODEC = simpleCodec(MulchBlock::new);

  public MulchBlock(Properties properties) {
    super(properties);
  }

  @Override
  protected MapCodec<? extends FallingBlock> codec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction direction, BlockState plant) {
    // we are fine with most plants, but saplings are a bit much
    // this is mostly cop out since I have no way of stopping sapling growth
    // 1.20 allowed the plains plant type: flowers, grass and other bushes growing on dirt, not crops or mushrooms
    Block block = plant.getBlock();
    if (block instanceof BushBlock && !(block instanceof SaplingBlock) && !(block instanceof CropBlock) && !(block instanceof MushroomBlock) && !(block instanceof StemBlock)) {
      return TriState.TRUE;
    }
    return TriState.DEFAULT;
  }
}
