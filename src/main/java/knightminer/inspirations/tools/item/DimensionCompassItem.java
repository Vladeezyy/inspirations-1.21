package knightminer.inspirations.tools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

/* Yes, the interface says armor, but in mojmappings its not armor, its just dyeable */
public class DimensionCompassItem extends CompassItem {
	public DimensionCompassItem(Properties builder) {
		super(builder);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		return stack.has(DataComponents.LODESTONE_TRACKER) ? "item.inspirations.lodestone_dimension_compass" : this.getDescriptionId();
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		// basically a copy of the vanilla compass, but does not hardcode to compass item
		BlockPos blockpos = context.getClickedPos();
		Level world = context.getLevel();
		if (!world.getBlockState(blockpos).is(Blocks.LODESTONE)) {
			return InteractionResult.PASS;
		}
		world.playSound(null, blockpos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		ItemStack stack = context.getItemInHand();
		LodestoneTracker tracker = new LodestoneTracker(Optional.of(GlobalPos.of(world.dimension(), blockpos)), true);
		boolean singleItem = !player.getAbilities().instabuild && stack.getCount() == 1;
		if (singleItem) {
			stack.set(DataComponents.LODESTONE_TRACKER, tracker);
		} else {
			ItemStack copy = stack.copyWithCount(1);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			copy.set(DataComponents.LODESTONE_TRACKER, tracker);
			if (!player.getInventory().add(copy)) {
				player.drop(copy, false);
			}
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}
}
