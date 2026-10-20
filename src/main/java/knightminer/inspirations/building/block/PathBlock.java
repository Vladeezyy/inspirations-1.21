package knightminer.inspirations.building.block;

import javax.annotation.Nullable;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import knightminer.inspirations.library.InspirationsTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathBlock extends Block implements SimpleWaterloggedBlock {
  private final static BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

  private final VoxelShape shape;
  private final VoxelShape collShape;

  public PathBlock(Properties properties, VoxelShape shape) {
    super(properties);
    // Each path has a different shape, but use the bounding box for collisions.
    this.shape = shape;
    this.collShape = Shapes.create(shape.bounds());
    registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
    builder.add(WATERLOGGED);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
    return defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public FluidState getFluidState(BlockState state) {
    return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess ticks, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
    if (state.getValue(WATERLOGGED)) {
      ticks.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
    }
    return super.updateShape(state, world, ticks, currentPos, facing, facingPos, facingState, random);
  }

  /* Block Shape */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return shape;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    return collShape;
  }

  /* Solid surface below */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
    return super.canSurvive(state, world, pos) && this.canBlockStay(world, pos);
  }

  private boolean canBlockStay(LevelReader world, BlockPos pos) {
    BlockPos down = pos.below();
    return Block.canSupportRigidBlock(world, down) || world.getBlockState(down).is(InspirationsTags.Blocks.MULCH);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void neighborChanged(BlockState state, Level world, BlockPos pos, Block other, @Nullable Orientation orientation, boolean isMoving) {
    if (!this.canBlockStay(world, pos)) {
      world.destroyBlock(pos, true);
    } else if (state.getValue(WATERLOGGED)) {
      world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
    }
  }
}
