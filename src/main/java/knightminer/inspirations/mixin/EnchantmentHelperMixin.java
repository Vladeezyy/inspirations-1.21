package knightminer.inspirations.mixin;

import knightminer.inspirations.common.Config;
import knightminer.inspirations.tools.enchantment.InspirationsEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shield enchantments as in 1.20: the protections of a shield count while blocking, its knockback, fire aspect and
 * thorns act only on blocked hits (ToolsEvents), not when holding or attacking with it.
 */
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
  @Inject(method = "getDamageProtection", at = @At("RETURN"), cancellable = true)
  private static void inspirations$shieldProtection(ServerLevel level, LivingEntity entity, DamageSource source, CallbackInfoReturnable<Float> cir) {
    if (!Config.moreShieldEnchantments.getAsBoolean() || !entity.isBlocking()) {
      return;
    }
    ItemStack shield = entity.getUseItem();
    ItemEnchantments enchantments = shield.getAllEnchantments(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
    if (enchantments.isEmpty()) {
      return;
    }
    MutableFloat protection = new MutableFloat(cir.getReturnValueF());
    for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
      // vanilla only counts armor slots for protections, the blocking shield is in a hand
      if (!entry.getKey().value().matchingSlot(entity.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND)) {
        entry.getKey().value().modifyDamageProtection(level, entry.getIntValue(), shield, entity, source, protection);
      }
    }
    cir.setReturnValue(protection.floatValue());
  }

  @ModifyVariable(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V", at = @At("HEAD"), argsOnly = true)
  private static EnchantmentHelper.EnchantmentInSlotVisitor inspirations$skipShieldInSlot(EnchantmentHelper.EnchantmentInSlotVisitor visitor, ItemStack stack, EquipmentSlot slot, LivingEntity entity, EnchantmentHelper.EnchantmentInSlotVisitor original) {
    if (!stack.isEmpty() && Config.moreShieldEnchantments.getAsBoolean() && stack.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK)) {
      return (enchantment, enchantmentLevel, item) -> {
        if (!InspirationsEnchanting.skipVanillaEffect(stack, enchantment)) {
          visitor.accept(enchantment, enchantmentLevel, item);
        }
      };
    }
    return visitor;
  }

  @ModifyVariable(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V", at = @At("HEAD"), argsOnly = true)
  private static EnchantmentHelper.EnchantmentVisitor inspirations$skipShield(EnchantmentHelper.EnchantmentVisitor visitor, ItemStack stack, EnchantmentHelper.EnchantmentVisitor original) {
    if (!stack.isEmpty() && Config.moreShieldEnchantments.getAsBoolean() && stack.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK)) {
      return (enchantment, enchantmentLevel) -> {
        if (!InspirationsEnchanting.skipVanillaEffect(stack, enchantment)) {
          visitor.accept(enchantment, enchantmentLevel);
        }
      };
    }
    return visitor;
  }
}
