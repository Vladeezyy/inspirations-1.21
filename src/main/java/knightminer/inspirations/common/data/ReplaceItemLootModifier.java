package knightminer.inspirations.common.data;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Replaces every dropped stack matching {@code original} with {@code replacement}, keeping the count.
 * Same JSON as Mantle 1.20's {@code mantle:replace_item} (without functions), which Inspirations used for heartbeets.
 */
public class ReplaceItemLootModifier extends LootModifier {
  public static final MapCodec<ReplaceItemLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(
    Ingredient.CODEC_NONEMPTY.fieldOf("original").forGetter(m -> m.original),
    BuiltInRegistries.ITEM.byNameCodec().fieldOf("replacement").forGetter(m -> m.replacement)
  )).apply(inst, ReplaceItemLootModifier::new));

  private final Ingredient original;
  private final Item replacement;

  public ReplaceItemLootModifier(LootItemCondition[] conditions, Ingredient original, Item replacement) {
    super(conditions);
    this.original = original;
    this.replacement = replacement;
  }

  @Override
  protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
    for (int i = 0; i < loot.size(); i++) {
      ItemStack stack = loot.get(i);
      if (original.test(stack)) {
        loot.set(i, new ItemStack(replacement, stack.getCount()));
      }
    }
    return loot;
  }

  @Override
  public MapCodec<? extends IGlobalLootModifier> codec() {
    return CODEC;
  }
}
