package knightminer.inspirations.common.data;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.tweaks.InspirationsTweaks;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class GlobalLootProvider extends GlobalLootModifierProvider {
  public GlobalLootProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
    super(packOutput, registries, Inspirations.modID);
  }

  @Override
  protected void start() {
    HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
    add("heartbeet", new ReplaceItemLootModifier(new LootItemCondition[] {
        LootTableIdCondition.builder(Identifier.withDefaultNamespace("blocks/beetroots")).build(),
        ConfigEnabledCondition.HEARTBEETS,
        new LootItemBlockStatePropertyCondition.Builder(Blocks.BEETROOTS).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BeetrootBlock.AGE, 3)).build(),
        BonusLevelTableCondition.bonusLevelFlatChance(enchantments.getOrThrow(Enchantments.FORTUNE), 0.05f, 0.0625f, 0.083333336f, 0.1f).build()
      }, Ingredient.of(Items.BEETROOT), InspirationsTweaks.heartbeet));
  }
}
