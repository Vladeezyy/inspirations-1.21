package knightminer.inspirations.cauldrons.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Map;

/** Cauldron with four levels */
public class FourLayerCauldronBlock extends AbstractCauldronBlock {
	public static final int MAX_FILL_LEVEL = 4;
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, MAX_FILL_LEVEL);

	public static final MapCodec<FourLayerCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		propertiesCodec(),
		CauldronInteractions.CODEC.fieldOf("interactions").forGetter(block -> block.interactions)
	).apply(inst, FourLayerCauldronBlock::new));

	public FourLayerCauldronBlock(Properties pProperties, CauldronInteraction.Dispatcher pInteractions) {
		super(pProperties, pInteractions);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 1));
	}

	@Override
	protected MapCodec<? extends AbstractCauldronBlock> codec() {
		return CODEC;
	}

	@Override
	public boolean isFull(BlockState state) {
		return state.getValue(LEVEL) == MAX_FILL_LEVEL;
	}

	@Override
	protected double getContentHeight(BlockState state) {
		return (5.0D + state.getValue(LEVEL) * 2.5D) / 16.0D;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return state.getValue(LEVEL);
	}

	/** Content shapes per level: entities inside them get {@link #entityInside} since 1.21.5 (isEntityInsideContent before) */
	private static final VoxelShape[] FILLED_SHAPES = Block.boxes(MAX_FILL_LEVEL - 1,
		level -> Shapes.or(AbstractCauldronBlock.SHAPE, Block.column(12.0, 4.0, 5.0 + (level + 1) * 2.5)));

	@Override
	protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
		return FILLED_SHAPES[state.getValue(LEVEL) - 1];
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}
}
