package knightminer.inspirations.tools.enchantment;

import knightminer.inspirations.common.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.Set;

/**
 * Which enchantments shields and axes take. 1.20 replaced the vanilla enchantment classes (ShieldProtectionEnchantment,
 * ExtendedKnockbackEnchantment and others); enchantments are data in 1.21, so ShieldItemMixin, AxeItemMixin and
 * EnchantmentMixin ask here.
 */
public final class InspirationsEnchanting {
  private InspirationsEnchanting() {}

  /** Protections and thorns, applied while blocking */
  private static final Set<ResourceKey<Enchantment>> SHIELD_DEFENSE = Set.of(Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.THORNS);
  /** Applied to attackers whose hit the shield blocked */
  private static final Set<ResourceKey<Enchantment>> SHIELD_ATTACK = Set.of(Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT);
  /** Weapon enchantments for axes */
  private static final Set<ResourceKey<Enchantment>> AXE_WEAPON = Set.of(Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.LOOTING);
  /** Damage enchantments axes already take from books, made available at the table */
  private static final Set<ResourceKey<Enchantment>> AXE_DAMAGE = Set.of(Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS);

  private static boolean isIn(Holder<Enchantment> enchantment, Set<ResourceKey<Enchantment>> set) {
    return enchantment.unwrapKey().map(set::contains).orElse(false);
  }

  /** True if the shield can take the enchantment from an anvil or a book */
  public static boolean shieldSupports(Holder<Enchantment> enchantment) {
    return Config.moreShieldEnchantments.getAsBoolean() && (isIn(enchantment, SHIELD_DEFENSE) || isIn(enchantment, SHIELD_ATTACK));
  }

  /** True if the shield can get the enchantment at the enchanting table */
  public static boolean shieldPrimary(Holder<Enchantment> enchantment) {
    return Config.shieldEnchantmentTable.getAsBoolean() && shieldSupports(enchantment);
  }

  /** True if the axe can take the enchantment from an anvil or a book */
  public static boolean axeSupports(Holder<Enchantment> enchantment) {
    return Config.axeWeaponEnchants.getAsBoolean() && isIn(enchantment, AXE_WEAPON);
  }

  /** True if the axe can get the enchantment at the enchanting table */
  public static boolean axePrimary(Holder<Enchantment> enchantment) {
    if (!Config.axeEnchantmentTable.getAsBoolean()) {
      return false;
    }
    return isIn(enchantment, AXE_DAMAGE) || (Config.axeWeaponEnchants.getAsBoolean() && isIn(enchantment, AXE_WEAPON));
  }

  /** Enchantability of a shield, more with a banner on it */
  public static int shieldEnchantability(ItemStack stack) {
    if (!Config.shieldEnchantmentTable.getAsBoolean()) {
      return 0;
    }
    // shields always have the (empty) banner pattern component in 1.21, a banner means a base color or layers
    boolean banner = stack.get(DataComponents.BASE_COLOR) != null
                     || !stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().isEmpty();
    return banner ? 16 : 12;
  }

  /**
   * True if an enchantment on this stack must not act through vanilla's slot iteration: a shield's knockback, fire
   * aspect and thorns act only when it blocks a hit (ToolsEvents), as the 1.20 enchantment classes did.
   */
  public static boolean skipVanillaEffect(ItemStack stack, Holder<Enchantment> enchantment) {
    return Config.moreShieldEnchantments.getAsBoolean() && stack.canPerformAction(ItemAbilities.SHIELD_BLOCK)
           && (isIn(enchantment, SHIELD_ATTACK) || enchantment.is(Enchantments.THORNS));
  }

  /** Extra incompatibilities of 1.20: fire aspect with thorns and efficiency, knockback with efficiency, looting with fortune */
  public static boolean extraIncompatible(Holder<Enchantment> first, Holder<Enchantment> second) {
    if ((Config.moreShieldEnchantments.getAsBoolean() || Config.axeWeaponEnchants.getAsBoolean()) && (pair(first, second, Enchantments.FIRE_ASPECT, Enchantments.EFFICIENCY) || pair(first, second, Enchantments.KNOCKBACK, Enchantments.EFFICIENCY))) {
      return true;
    }
    if (Config.moreShieldEnchantments.getAsBoolean() && pair(first, second, Enchantments.FIRE_ASPECT, Enchantments.THORNS)) {
      return true;
    }
    return Config.axeWeaponEnchants.getAsBoolean() && pair(first, second, Enchantments.LOOTING, Enchantments.FORTUNE);
  }

  private static boolean pair(Holder<Enchantment> first, Holder<Enchantment> second, ResourceKey<Enchantment> a, ResourceKey<Enchantment> b) {
    return (first.is(a) && second.is(b)) || (first.is(b) && second.is(a));
  }
}
