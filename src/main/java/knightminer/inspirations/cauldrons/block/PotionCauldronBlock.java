package knightminer.inspirations.cauldrons.block;

import net.minecraft.world.entity.InsideBlockEffectApplier;
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
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean intersects) {
		if (!world.isClientSide()) {
			if (isBoiling(world, pos)) {
				BoilingFourLayerCauldronBlock.boil(entity);
			}
			if (entity instanceof LivingEntity living) {
				PotionCauldronBlockEntity be = InspirationsCaudrons.potionCauldronEntity.getBlockEntity(world, pos);
				if (be != null) {
					List<MobEffectInstance> potionEffects = new ArrayList<>();
					be.getPotionContents().forEachEffect(potionEffects::add, 1.0F);
					if (potionEffects.stream().anyMatch(effect -> !living.hasEffect(effect.getEffect()))) {
						for (MobEffectInstance effect : potionEffects) {
							if (effect.getEffect().value().isInstantenous()) {
								effect.getEffect().value().applyInstantenousEffect((net.minecraft.server.level.ServerLevel) world, null, null, living, effect.getAmplifier(), 1.0D);
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
}
