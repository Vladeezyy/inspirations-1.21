package knightminer.inspirations.mixin;

import knightminer.inspirations.tools.enchantment.InspirationsEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

/**
 * Axes take looting, knockback and fire aspect (config tools.enchantments.axeWeapon), and get the weapon enchantments
 * at the table (tools.enchantments.axeTable). 1.20 replaced the vanilla enchantments with axe aware classes; 26.3 has
 * no axe class (axes are items with tool properties), so the item class asks for the axe tag. The rest is NeoForge's
 * default of {@link IItemExtension}.
 */
@Mixin(Item.class)
public abstract class ItemMixin implements IItemExtension {
  @Override
  public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
    if (stack.is(ItemTags.AXES) && InspirationsEnchanting.axeSupports(enchantment)) {
      return true;
    }
    return stack.is(Items.ENCHANTED_BOOK) || enchantment.value().isSupportedItem(stack);
  }

  @Override
  public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
    if (stack.is(ItemTags.AXES) && InspirationsEnchanting.axePrimary(enchantment)) {
      return true;
    }
    if (stack.getItem() == Items.BOOK) {
      return true;
    }
    Optional<HolderSet<Item>> primaryItems = enchantment.value().definition().primaryItems();
    return this.supportsEnchantment(stack, enchantment) && (primaryItems.isEmpty() || stack.is(primaryItems.get()));
  }
}
