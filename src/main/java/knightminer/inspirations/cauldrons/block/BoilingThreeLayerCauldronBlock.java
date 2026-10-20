package knightminer.inspirations.cauldrons.block;

import net.minecraft.world.entity.InsideBlockEffectApplier;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.function.Predicate;

import static knightminer.inspirations.cauldrons.block.BoilingFourLayerCauldronBlock.isBoiling;

/** Cauldron block that has four layers and emits boiling particles when over fire */
public class BoilingThreeLayerCauldronBlock extends LayeredCauldronBlock {
	public BoilingThreeLayerCauldronBlock(Properties props, Precipitation precipitation, CauldronInteraction.Dispatcher interactions) {
		super(precipitation, interactions, props);
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
		if (!world.isClientSide() && isBoiling(world, pos)) {
			BoilingFourLayerCauldronBlock.boil(entity);
		}
		// puts out burning entities like vanilla
		super.entityInside(state, world, pos, entity, effects, intersects);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		if (isBoiling(level, pos)) {
			MiscUtil.addParticles(InspirationsCaudrons.boilingParticle, level, pos, 2, getContentHeight(state), rand);
		}
	}
}
