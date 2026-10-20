package knightminer.inspirations.mixin;

import knightminer.inspirations.tools.enchantment.InspirationsEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;


/**
 * Shields take protections, thorns, knockback and fire aspect (config tools.enchantments.moreShield), and can be
 * enchanted at the table (tools.enchantments.shieldTable). 1.20 replaced the shield item with EnchantableShieldItem.
 */
@Mixin(ShieldItem.class)
public abstract class ShieldItemMixin extends Item {
  private ShieldItemMixin(Properties properties) {
    super(properties);
  }

  @Override
  public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
    return InspirationsEnchanting.shieldSupports(enchantment) || super.supportsEnchantment(stack, enchantment);
  }

  @Override
  public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
    if (InspirationsEnchanting.shieldPrimary(enchantment)) {
      return true;
    }
    return super.isPrimaryItemFor(stack, enchantment);
  }
}
