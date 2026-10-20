package knightminer.inspirations.utility.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.LevelReader;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.utility.InspirationsUtility;
import knightminer.inspirations.utility.block.entity.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import dev.vladeezyy.kilnwork.block.InventoryBlock;
import dev.vladeezyy.kilnwork.util.BlockEntityHelper;

import javax.annotation.Nullable;

public class PipeBlock extends InventoryBlock implements SimpleWaterloggedBlock {
  // Facing is the direction we output to.
  public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
  // These six values specify if another pipe/hopper is in this direction for us
  // to visually connect to.
  private static final BooleanProperty NORTH = BlockStateProperties.NORTH;
  private static final BooleanProperty EAST = BlockStateProperties.EAST;
  private static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
  private static final BooleanProperty WEST = BlockStateProperties.WEST;
  private static final BooleanProperty UP = BlockStateProperties.UP;
  private static final BooleanProperty DOWN = BlockStateProperties.DOWN;
  // If this is set, there is a hopper on our output side which isn't facing towards us.
  // We then render a longer pipe to connect with the spout model.
  public static final BooleanProperty HOPPER = BooleanProperty.create("hopper");

  private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

  // Direction.getIndex() -> Property. Order is D-U-N-S-W-E
  public static final BooleanProperty[] DIR_ENABLED = new BooleanProperty[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};

  public PipeBlock(Properties props) {
    super(props);
    this.registerDefaultState(this.getStateDefinition().any()
                             .setValue(FACING, Direction.NORTH)
                             .setValue(NORTH, false)
                             .setValue(EAST, false)
                             .setValue(SOUTH, false)
                             .setValue(WEST, false)
                             .setValue(UP, false)
                             .setValue(DOWN, false)
                             .setValue(HOPPER, false)
                             .setValue(WATERLOGGED, false)
                        );
  }


  /* Block state settings */

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
    builder.add(WATERLOGGED, FACING, NORTH, EAST, SOUTH, WEST, UP, DOWN, HOPPER);
  }

  @Deprecated
  @Override
  public BlockState rotate(BlockState state, Rotation rot) {
    return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
  }

  @Deprecated
  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
  }

  @Deprecated
  @Override
  public BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess ticks, BlockPos pos, Direction neighFacing, BlockPos neighPos, BlockState neighState, RandomSource random) {
    Direction outFacing = state.getValue(FACING);

    // We only need to check the one side that updated.
    state = state.setValue(DIR_ENABLED[neighFacing.get3DDataValue()], canConnectTo(world, pos, outFacing, neighFacing));

    // Check if the output side is a hopper if that side was changed.
    if (outFacing == neighFacing) {
      BlockState offsetState = world.getBlockState(pos.relative(outFacing));
      state = state.setValue(HOPPER,
                         offsetState.getBlock() instanceof HopperBlock &&
                         offsetState.getValue(HopperBlock.FACING) != outFacing.getOpposite()
                        );
    }
    return state;
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    Level world = context.getLevel();
    BlockPos pos = context.getClickedPos();

    Direction facing = context.getClickedFace().getOpposite();
    // only allow up if allowed in the config.
    if (!Config.get(Config.pipeUpwards) && facing == Direction.UP) {
      facing = context.getHorizontalDirection();
    }

    BlockState offsetState = world.getBlockState(pos.relative(facing));
    // When first placed, check every side.
    return this.defaultBlockState()
               .setValue(FACING, facing)
               .setValue(HOPPER, offsetState.getBlock() instanceof HopperBlock && offsetState.getValue(HopperBlock.FACING) != facing.getOpposite())
               .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER)
               .setValue(UP, canConnectTo(world, pos, facing, Direction.UP))
               .setValue(DOWN, canConnectTo(world, pos, facing, Direction.DOWN))
               .setValue(NORTH, canConnectTo(world, pos, facing, Direction.NORTH))
               .setValue(EAST, canConnectTo(world, pos, facing, Direction.EAST))
               .setValue(SOUTH, canConnectTo(world, pos, facing, Direction.SOUTH))
               .setValue(WEST, canConnectTo(world, pos, facing, Direction.WEST));
  }

  @Deprecated
  @Override
  public FluidState getFluidState(BlockState state) {
    return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
  }

  @Override
  protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
    // return false if holding a pipe to make easier to place
    Item item = stack.getItem();
    if (item == InspirationsUtility.pipe.asItem() || Block.byItem(item) instanceof HopperBlock) {
      return InteractionResult.PASS;
    }
    return super.useItemOn(stack, state, world, pos, player, hand, trace);
  }


  /* Model and shape */

  private static boolean canConnectTo(LevelReader world, BlockPos pos, Direction facing, Direction side) {
    // ignore side pipe is facing
    if (facing == side) return false;

    BlockState state = world.getBlockState(pos.relative(side));
    Block block = state.getBlock();
    Direction opposite = side.getOpposite();
    // if it is a known item output thingy and is facing us, connect
    if ((block instanceof PipeBlock || block instanceof DropperBlock) && state.getValue(FACING) == opposite) return true;
    // hopper check, we can skip on down since hoppers cannot face up
    return side != Direction.DOWN && block instanceof HopperBlock && state.getValue(HopperBlock.FACING) == opposite;
  }


  /* Tile Entity */

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new PipeBlockEntity(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> matchType) {
    return BlockEntityHelper.serverTicker(level, matchType, InspirationsUtility.tilePipe, PipeBlockEntity.SERVER_TICKER);
  }


  /* Bounds */

  // base bounds
  private static final VoxelShape BOUNDS_CENTER = Shapes.box(0.375, 0.25, 0.375, 0.625, 0.5, 0.625),
  // main bounds for side pipes
  BOUNDS_DOWN = Shapes.box(0.375, 0, 0.375, 0.625, 0.25, 0.625),
      BOUNDS_UP = Shapes.box(0.375, 0.5, 0.375, 0.625, 1, 0.625),
      BOUNDS_NORTH = Shapes.box(0.375, 0.25, 0, 0.625, 0.5, 0.375),
      BOUNDS_SOUTH = Shapes.box(0.375, 0.25, 0.625, 0.625, 0.5, 1),
      BOUNDS_WEST = Shapes.box(0, 0.25, 0.375, 0.375, 0.5, 0.625),
      BOUNDS_EAST = Shapes.box(0.625, 0.25, 0.375, 1, 0.5, 0.625),
  // extra bounds for the raytrace to select the little connections
  BOUNDS_DOWN_CONNECT = Shapes.box(0.34375, 0, 0.34375, 0.65625, 0.0625, 0.65625),
      BOUNDS_UP_CONNECT = Shapes.box(0.34375, 0.9375, 0.34375, 0.65625, 1, 0.65625),
      BOUNDS_NORTH_CONNECT = Shapes.box(0.34375, 0.21875, 0, 0.65625, 0.53125, 0.0625),
      BOUNDS_SOUTH_CONNECT = Shapes.box(0.34375, 0.21875, 0.9375, 0.65625, 0.53125, 1),
      BOUNDS_WEST_CONNECT = Shapes.box(0, 0.21875, 0.34375, 0.0625, 0.53125, 0.65625),
      BOUNDS_EAST_CONNECT = Shapes.box(0.9375, 0.21875, 0.34375, 1, 0.53125, 0.65625);


  // Compute a static lookup table for all the combinations.
  // First index is the facing, the second is a connections bitmask.
  private static final VoxelShape[][] BOUNDS = new VoxelShape[6][64];

  static {
    // above side bounds in an array to index easier - DUNSWE
    VoxelShape[] BOUNDS_SIDES = {
        BOUNDS_DOWN, BOUNDS_UP, BOUNDS_NORTH, BOUNDS_SOUTH, BOUNDS_WEST, BOUNDS_EAST
    };
    VoxelShape[] BOUNDS_CONN_SIDES = {
        BOUNDS_DOWN_CONNECT, BOUNDS_UP_CONNECT, BOUNDS_NORTH_CONNECT,
        BOUNDS_SOUTH_CONNECT, BOUNDS_WEST_CONNECT, BOUNDS_EAST_CONNECT
    };
    for (int i = 0; i < 64; i++) {
      VoxelShape shape = BOUNDS_CENTER;
      for (int j = 0; j < 6; j++) {
        if ((i & (1 << j)) != 0) {
          shape = Shapes.or(shape, BOUNDS_CONN_SIDES[j], BOUNDS_SIDES[j]);
        }
      }
      for (int j = 0; j < 6; j++) {
        BOUNDS[j][i] = Shapes.or(shape, BOUNDS_SIDES[j]);
      }
    }
  }

  @Deprecated
  @Override
  public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    int bitmask = 0;
    for (int i = 0; i < 6; i++) {
      bitmask |= state.getValue(DIR_ENABLED[i]) ? (1 << i) : 0;
    }
    return BOUNDS[state.getValue(FACING).get3DDataValue()][bitmask];
  }
}
