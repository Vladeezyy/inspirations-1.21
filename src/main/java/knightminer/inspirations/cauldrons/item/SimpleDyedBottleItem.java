package knightminer.inspirations.cauldrons.item;

import net.minecraft.core.component.DataComponents;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Item for a dye item that is also a bottle.
 */
public class SimpleDyedBottleItem extends DyeItem {
  /**
   *
   * @param props    Item properties
   * @param color    Dye color
   */
  private final DyeColor color;

  public SimpleDyedBottleItem(Properties props, DyeColor color) {
    // the dye color is the dye component since 26.1 (the shared properties get it right before each item is made)
    super(props.component(DataComponents.DYE, color));
    this.color = color;
  }

  /** Color of the dye in the bottle */
  public DyeColor getDyeColor() {
    return color;
  }

  @Override
  public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
    // this method reimplements many entity dye behaviors to properly return bottles
    DyeColor color = getDyeColor();

    // dye sheep
    if (target instanceof Sheep sheep) {
      if (!sheep.isSheared() && sheep.getColor() != color) {
        sheep.setColor(color);
        player.playSound(SoundEvents.BOTTLE_EMPTY, 1.0F, 1.0F);

        // give back bottle;
        consumeItem(player, hand, stack);
      }
      return InteractionResult.SUCCESS;
    }

    return InteractionResult.PASS;
  }

  /**
   * Helper to consume an item, returning the bottle containr
   * @param player  Player entity
   * @param hand    Hand used
   * @param stack   Consumed stack
   */
  private static void consumeItem(Player player, InteractionHand hand, ItemStack stack) {
    if (!player.isCreative()) {
      ItemStack bottle = MiscUtil.getCraftingRemainder(stack);
      if (stack.getCount() == 1) {
        player.setItemInHand(hand, bottle);
      } else {
        stack.shrink(1);
        player.getInventory().placeItemBackInInventory(bottle);
      }
    }
  }
}
