package knightminer.inspirations.mixin;

import knightminer.inspirations.tools.enchantment.InspirationsEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The incompatibilities the extended enchantment classes of 1.20 added */
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
  @Inject(method = "areCompatible", at = @At("RETURN"), cancellable = true)
  private static void inspirations$extraIncompatible(Holder<Enchantment> first, Holder<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValueZ() && InspirationsEnchanting.extraIncompatible(first, second)) {
      cir.setReturnValue(false);
    }
  }
}
