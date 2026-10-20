package knightminer.inspirations.mixin;

import knightminer.inspirations.tools.enchantment.InspirationsEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Axes take looting, knockback and fire aspect (config tools.enchantments.axeWeapon), and get the weapon enchantments
 * at the table (tools.enchantments.axeTable). 1.20 replaced the vanilla enchantments with axe aware classes.
 */
@Mixin(AxeItem.class)
public abstract class AxeItemMixin extends Item {
  private AxeItemMixin(Properties properties) {
    super(properties);
  }

  @Override
  public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
    return InspirationsEnchanting.axeSupports(enchantment) || super.supportsEnchantment(stack, enchantment);
  }

  @Override
  public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
    if (InspirationsEnchanting.axePrimary(enchantment)) {
      return true;
    }
    return super.isPrimaryItemFor(stack, enchantment);
  }
}
