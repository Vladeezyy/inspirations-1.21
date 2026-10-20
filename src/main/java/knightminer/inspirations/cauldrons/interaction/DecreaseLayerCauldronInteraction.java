package knightminer.inspirations.cauldrons.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.function.Supplier;

/**
 * Decreases the cauldron by one layer, returning the given result. The result is made on use: since 26.1 item stacks
 * can not be made before the components are bound, after common setup.
 */
public class DecreaseLayerCauldronInteraction extends AbstractDecreaseLayerCauldronInteraction {
	private final Supplier<ItemStack> result;
	public DecreaseLayerCauldronInteraction(Supplier<ItemStack> result, IntegerProperty prop, boolean consumeInput, SoundEvent sound) {
		super(prop, consumeInput, sound);
		this.result = result;
	}

	public DecreaseLayerCauldronInteraction(ItemLike result, IntegerProperty prop, SoundEvent sound) {
		this(() -> new ItemStack(result), prop, true, sound);
	}

	public DecreaseLayerCauldronInteraction(ItemLike result, IntegerProperty prop) {
		this(result, prop, SoundEvents.BOTTLE_FILL);
	}

	@Override
	protected ItemStack getResult(BlockState state, Level level, BlockPos pos, ItemStack stack) {
		return result.get();
	}
}
