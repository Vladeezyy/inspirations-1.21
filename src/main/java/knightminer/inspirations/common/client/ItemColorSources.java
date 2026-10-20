package knightminer.inspirations.common.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import knightminer.inspirations.Inspirations;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Item colors and item model properties as in 1.21.1, read by the item definitions: the tint source
 * {@code inspirations:item_color} ({@code "index"}: tint index) calls the color function of the item, the numeric
 * property {@code inspirations:property} ({@code "name"}) calls the property function of the item with that name.
 * The functions are registered in Java (client events) per item, the definitions are written by
 * {@code tools/port/item_definitions.py}.
 */
public final class ItemColorSources {
  private ItemColorSources() {}

  /** Color of a tint index of a stack, -1 for none (white) */
  @FunctionalInterface
  public interface ItemColor {
    int getColor(ItemStack stack, int tintIndex);
  }

  /** Numeric property of a stack; the owner is the holder, an item frame or the item entity */
  @FunctionalInterface
  public interface ItemProperty {
    float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed);
  }

  /** The entity behind the owner (also through an offset, as for item frames), null for block entities */
  @Nullable
  public static Entity entityOf(@Nullable ItemOwner owner) {
    while (owner instanceof ItemOwner.OffsetFromOwner offset) {
      owner = offset.owner();
    }
    return owner instanceof Entity entity ? entity : null;
  }

  private static final Map<Item, ItemColor> COLORS = new IdentityHashMap<>();
  private static final Map<Item, Map<String, ItemProperty>> PROPERTIES = new IdentityHashMap<>();

  /** Registers the color function of the items (null and air are skipped) */
  public static void registerColor(ItemColor color, ItemLike... items) {
    for (ItemLike item : items) {
      if (item != null && item.asItem() != Items.AIR) {
        COLORS.put(item.asItem(), color);
      }
    }
  }

  /** Registers a named numeric property of the item */
  public static void registerProperty(@Nullable ItemLike item, String name, ItemProperty property) {
    if (item != null) {
      PROPERTIES.computeIfAbsent(item.asItem(), key -> new java.util.HashMap<>()).put(name, property);
    }
  }

  /** Color of the stack from its registered function, -1 if none */
  public static int getColor(ItemStack stack, int tintIndex) {
    ItemColor color = COLORS.get(stack.getItem());
    return color == null ? -1 : color.getColor(stack, tintIndex);
  }

  public record Tint(int index) implements ItemTintSource {
    public static final MapCodec<Tint> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Codec.INT.fieldOf("index").forGetter(Tint::index)
    ).apply(inst, Tint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
      return getColor(stack, index);
    }

    @Override
    public MapCodec<Tint> type() {
      return MAP_CODEC;
    }
  }

  public record Property(String name) implements RangeSelectItemModelProperty {
    public static final MapCodec<Property> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Codec.STRING.fieldOf("name").forGetter(Property::name)
    ).apply(inst, Property::new));

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
      Map<String, ItemProperty> properties = PROPERTIES.get(stack.getItem());
      ItemProperty property = properties == null ? null : properties.get(name);
      return property == null ? 0 : property.get(stack, level, owner, seed);
    }

    @Override
    public MapCodec<Property> type() {
      return MAP_CODEC;
    }
  }

  /** Ids of the two types */
  public static final net.minecraft.resources.Identifier TINT_ID = Inspirations.getResource("item_color");
  public static final net.minecraft.resources.Identifier PROPERTY_ID = Inspirations.getResource("property");
}
