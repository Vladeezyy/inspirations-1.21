package knightminer.inspirations.mixin;

import knightminer.inspirations.tools.enchantment.InspirationsEnchanting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shields go into the enchanting table (config tools.enchantments.shieldTable): since 1.21.2 that needs the enchantable
 * component, given here (and in EnchantmentHelperMixin) instead of on the item, so the value can follow the config and
 * the banner.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
  @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
  private void inspirations$shieldEnchantable(CallbackInfoReturnable<Boolean> cir) {
    ItemStack stack = (ItemStack)(Object)this;
    if (InspirationsEnchanting.shieldEnchantability(stack) > 0) {
      ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
      cir.setReturnValue(enchantments != null && enchantments.isEmpty());
    }
  }
}
