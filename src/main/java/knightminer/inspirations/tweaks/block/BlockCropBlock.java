package knightminer.inspirations.tweaks.block;

import knightminer.inspirations.common.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.function.Supplier;

public abstract class BlockCropBlock extends CropBlock {
  public static final IntegerProperty LARGE_AGE = IntegerProperty.create("age", 0, 14);

  protected Supplier<Block> block;
  public BlockCropBlock(Supplier<Block> block, Properties props) {
    super(props);
    this.block = block;
  }


  /* Age logic */

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
    builder.add(getAgeProperty());
    // No super, we want a different age size!
  }

  @Override
  protected IntegerProperty getAgeProperty() {
    return LARGE_AGE;
  }

  @Override
  public int getMaxAge() {
    return 15;
  }

  @Override
  public BlockState getStateForAge(int age) {
    if (age == getMaxAge()) {
      return block.get().defaultBlockState();
    }
    return super.getStateForAge(age);
  }

  @Override
  public boolean isMaxAge(BlockState state) {
    // never get to max age, our max is the block
    return false;
  }

  @Override
  public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
    // Forge: prevent loading unloaded chunks when checking neighbor's light
    if (!world.isAreaLoaded(pos, 1)) return;
    // age will always be less than max, but safe to check
    int age = this.getAge(state);
    int max = this.getMaxAge();
    if (age < max) {
      if (CommonHooks.canCropGrow(world, pos, state, true)) {
        age++;
        BlockState newState = this.getStateForAge(age);
        // update again if max age, for the sake of cactus placement
        if (age == max) {
          world.setBlock(pos, newState, 3);
          if (!newState.canSurvive(world, pos)) {
            world.scheduleTick(pos, block.get(), 1);
          }
        } else {
          world.setBlock(pos, newState, 2);
        }
        CommonHooks.fireCropGrowPost(world, pos, state);
      }
    }
  }

  /* Crop logic */

  @Deprecated
  @Override
  public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
    return block.get().canSurvive(block.get().defaultBlockState(), world, pos);
  }


  /* Bonemeal */

  @Override
  public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState) {
    return Config.get(Config.bonemealBlockCrop);
  }

  @Override
  public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
    return Config.get(Config.bonemealBlockCrop);
  }


}
