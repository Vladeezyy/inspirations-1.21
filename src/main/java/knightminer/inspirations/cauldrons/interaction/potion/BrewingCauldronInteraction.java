package knightminer.inspirations.cauldrons.interaction.potion;

import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.server.level.ServerLevel;
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

			// the brewing recipes (data since 26.3): the server knows them, the client only the reagents
			Holder<Potion> newPotion;
			if (level instanceof ServerLevel server) {
				newPotion = getResult(server, oldPotion, stack);
			} else {
				newPotion = !stack.isEmpty() && level.recipeAccess().propertySet(RecipePropertySet.BREWING_REAGENTS).test(stack) ? oldPotion : null;
			}
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
					ItemStack container = MiscUtil.getCraftingRemainder(stack);
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
	private static Holder<Potion> getResult(ServerLevel level, Holder<Potion> potion, ItemStack reagent) {
		if (reagent.isEmpty()) {
			return null;
		}
		ItemStack input = PotionContents.createItemStack(Items.POTION, potion);
		BrewingInput brewing = new BrewingInput(input, reagent);
		ItemStack output = level.recipeAccess().getRecipeFor(RecipeType.BREWING, brewing, level).map(recipe -> recipe.value().assemble(brewing)).orElse(ItemStack.EMPTY);
		// container recipes (gunpowder, dragon's breath) change the item, not the potion
		if (output.isEmpty() || !output.is(Items.POTION)) {
			return null;
		}
		Holder<Potion> result = output.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(null);
		return result == null || result.equals(potion) ? null : result;
	}
}
