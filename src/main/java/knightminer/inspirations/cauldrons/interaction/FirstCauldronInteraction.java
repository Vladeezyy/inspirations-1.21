package knightminer.inspirations.cauldrons.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Merges multiple interactions into one, using the first that matches */
public record FirstCauldronInteraction(CauldronInteraction... interactions) implements CauldronInteraction {
	@Override
	public ItemInteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
		for (CauldronInteraction interaction : interactions) {
			ItemInteractionResult result = interaction.interact(state, level, pos, player, hand, stack);
			if (result.consumesAction()) {
				return result;
			}
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
}
