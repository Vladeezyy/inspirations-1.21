package knightminer.inspirations.common.registration;

import knightminer.inspirations.Inspirations;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Function;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Registers objects during {@link RegisterEvent} under the Inspirations namespace. Same API as Mantle 1.20's
 * RegistryAdapter (MIT, SlimeKnights), over NeoForge's register helper.
 */
public class RegistryAdapter<T> {
  protected final RegisterEvent.RegisterHelper<T> helper;

  public RegistryAdapter(RegisterEvent.RegisterHelper<T> helper) {
    this.helper = helper;
  }

  /** Location for the name */
  public Identifier getResource(String name) {
    return Inspirations.getResource(name);
  }

  /** Id of the block or item being made by {@link #make(Supplier, String)}, read by the properties mixins */
  private static final ThreadLocal<Identifier> PENDING_ID = new ThreadLocal<>();
  /** True while a block item is made: its name comes from the block */
  private static final ThreadLocal<Boolean> PENDING_BLOCK_ITEM = new ThreadLocal<>();

  /**
   * Id for properties made without one (since 1.21.2 blocks and items need their id in the properties before they are
   * made; {@code BlockPropertiesMixin} and {@code ItemPropertiesMixin} set it from here)
   */
  @Nullable
  public static Identifier pendingId() {
    return PENDING_ID.get();
  }

  /** True if the item being made is a block item */
  public static boolean pendingBlockItem() {
    return Boolean.TRUE.equals(PENDING_BLOCK_ITEM.get());
  }

  /** Makes the value with the pending id set */
  protected static <V> V withPendingId(Identifier id, boolean blockItem, Supplier<V> factory) {
    Identifier previous = PENDING_ID.get();
    Boolean previousBlockItem = PENDING_BLOCK_ITEM.get();
    PENDING_ID.set(id);
    PENDING_BLOCK_ITEM.set(blockItem);
    try {
      return factory.get();
    } finally {
      PENDING_ID.set(previous);
      PENDING_BLOCK_ITEM.set(previousBlockItem);
    }
  }

  /** Makes the value (its properties get the id) and registers it under the name */
  public <V extends T> V make(Supplier<V> factory, String name) {
    return register(withPendingId(getResource(name), false, factory), name);
  }

  /** Registers the value under the name */
  public <V extends T> V register(V value, String name) {
    helper.register(getResource(name), value);
    return value;
  }

  /** Registers the value under the full location */
  public <V extends T> V register(V value, Identifier name) {
    helper.register(name, value);
    return value;
  }

  /** Registers one value per enum value, named {@code <value>_<name>} */
  public <E extends Enum<E> & StringRepresentable, V extends T> EnumObject<E, V> registerEnum(Function<E, V> mapper, E[] values, String name) {
    if (values.length == 0) {
      throw new IllegalArgumentException("Must have at least one value");
    }
    EnumObject.Builder<E, V> builder = new EnumObject.Builder<>(values[0].getDeclaringClass());
    for (E value : values) {
      builder.put(value, make(() -> mapper.apply(value), value.getSerializedName() + "_" + name));
    }
    return builder.build();
  }

  /** Registers one value per enum value, named {@code <prefix>_<value>} */
  public <E extends Enum<E> & StringRepresentable, V extends T> EnumObject<E, V> registerEnum(Function<E, V> mapper, String prefix, E[] values) {
    if (values.length == 0) {
      throw new IllegalArgumentException("Must have at least one value");
    }
    EnumObject.Builder<E, V> builder = new EnumObject.Builder<>(values[0].getDeclaringClass());
    for (E value : values) {
      builder.put(value, make(() -> mapper.apply(value), prefix + "_" + value.getSerializedName()));
    }
    return builder.build();
  }
}
