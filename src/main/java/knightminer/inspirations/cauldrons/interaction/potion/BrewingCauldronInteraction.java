package knightminer.inspirations.cauldrons.interaction.potion;

import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

import static knightminer.inspirations.cauldrons.block.BoilingFourLayerCauldronBlock.isBoiling;
import static net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL;

/** Cauldron interaction for brewing in a cauldron */
public class BrewingCauldronInteraction implements CauldronInteraction {
	/** Fixed type of potion, if null fetches it from the block entity */
	@Nullable
	private final Holder<Potion> fixedInput;

	public BrewingCauldronInteraction(@Nullable Holder<Potion> fixedInput) {
		this.fixedInput = fixedInput;
	}

	@Override
	public InteractionResult interact(BlockState oldState, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		// cauldron needs to be above fire
		if (isBoiling(level, pos)) {
			// if given a fixed input, use that
			PotionCauldronBlockEntity cauldron = null;
			Holder<Potion> oldPotion = this.fixedInput;
			if (oldPotion == null) {
				cauldron = InspirationsCaudrons.potionCauldronEntity.getBlockEntity(level, pos);
				if (cauldron == null || cauldron.getPotion() == null) {
					return InteractionResult.TRY_WITH_EMPTY_HAND;
				}
				oldPotion = cauldron.getPotion();
			}

			// the level's brewing recipes, vanilla mixes and NeoForge recipes alike
			Holder<Potion> newPotion = getResult(level.potionBrewing(), oldPotion, stack);
			// found either?
			if (newPotion != null) {
				if (!level.isClientSide()) {
					// if we have a fixed input, update the cauldron then fetch the BE
					if (fixedInput != null) {
						// update the block, if already a potion cauldron it will remain one, but it may be water before
						level.setBlockAndUpdate(pos, InspirationsCaudrons.potionCauldron.defaultBlockState().setValue(LEVEL, oldState.getValue(LEVEL)));
						cauldron = InspirationsCaudrons.potionCauldronEntity.getBlockEntity(level, pos);
						if (cauldron == null) {
							return InteractionResult.CONSUME;
						}
					}

					// update potion
					cauldron.setPotion(newPotion);

					// consume items
					ItemStack container = stack.getCraftingRemainder();
					MiscUtil.shrinkHeldItem(player, hand, stack, 1);
					if (!container.isEmpty()) {
						MiscUtil.givePlayerItem(player, container.copy());
					}

					player.awardStat(Stats.USE_CAULDRON);
					level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	/**
	 * Gets the potion brewed from the potion with the reagent
	 * @return  New potion, or null if the reagent does not brew this potion into another one
	 */
	@Nullable
	private static Holder<Potion> getResult(PotionBrewing brewing, Holder<Potion> potion, ItemStack reagent) {
		if (reagent.isEmpty() || !brewing.isIngredient(reagent)) {
			return null;
		}
		ItemStack input = PotionContents.createItemStack(Items.POTION, potion);
		ItemStack output = brewing.mix(reagent, input);
		// container mixes (gunpowder, dragon's breath) change the item, not the potion
		if (output.isEmpty() || output == input || !output.is(Items.POTION)) {
			return null;
		}
		Holder<Potion> result = output.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(null);
		return result == null || result.equals(potion) ? null : result;
	}
}
