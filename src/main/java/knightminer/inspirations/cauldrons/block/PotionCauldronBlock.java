package knightminer.inspirations.cauldrons.block;

import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static knightminer.inspirations.cauldrons.block.BoilingFourLayerCauldronBlock.isBoiling;

/** Cauldron that contains a data instance for the potion */
public class PotionCauldronBlock extends BoilingThreeLayerCauldronBlock implements EntityBlock {
	public PotionCauldronBlock(Properties props) {
		super(props, Biome.Precipitation.NONE, InspirationsCaudrons.POTION_CAULDRON_INTERACTIONS);
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
		if (!world.isClientSide && isEntityInsideContent(state, pos, entity)) {
			if (isBoiling(world, pos)) {
				BoilingFourLayerCauldronBlock.boil(entity);
			}
			if (entity instanceof LivingEntity living) {
				PotionCauldronBlockEntity be = InspirationsCaudrons.potionCauldronEntity.getBlockEntity(world, pos);
				if (be != null) {
					List<MobEffectInstance> effects = new ArrayList<>();
					be.getPotionContents().forEachEffect(effects::add);
					if (effects.stream().anyMatch(effect -> !living.hasEffect(effect.getEffect()))) {
						for (MobEffectInstance effect : effects) {
							if (effect.getEffect().value().isInstantenous()) {
								effect.getEffect().value().applyInstantenousEffect(null, null, living, effect.getAmplifier(), 1.0D);
							} else {
								living.addEffect(new MobEffectInstance(effect));
							}
						}
						lowerFillLevel(state, world, pos);
						world.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0f, 1.0f);
					}
				}
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PotionCauldronBlockEntity(pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			// if broken, potion!
			if (!isMoving && !newState.is(BlockTags.CAULDRONS)) {
				PotionCauldronBlockEntity be = InspirationsCaudrons.potionCauldronEntity.getBlockEntity(level, pos);
				if (be != null && be.getPotion() != null) {
					int waterLevel = state.getValue(LEVEL);
					AreaEffectCloud cloud = new AreaEffectCloud(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
					cloud.setRadius(0.5F * waterLevel + 0.5F);
					cloud.setDuration(20 * (waterLevel + 1));
					cloud.setRadiusOnUse(-0.5F);
					cloud.setWaitTime(10);
					cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
					// 1.20 set the potion and added its effects again, so the cloud had every effect twice
					cloud.setPotionContents(be.getPotionContents());
					level.addFreshEntity(cloud);
					level.playSound(null, pos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
				}
			}
			level.removeBlockEntity(pos);
		}
	}
}
