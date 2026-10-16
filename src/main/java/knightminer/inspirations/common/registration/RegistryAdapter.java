package knightminer.inspirations.common.registration;

import knightminer.inspirations.Inspirations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Function;

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
  public ResourceLocation getResource(String name) {
    return Inspirations.getResource(name);
  }

  /** Registers the value under the name */
  public <V extends T> V register(V value, String name) {
    helper.register(getResource(name), value);
    return value;
  }

  /** Registers the value under the full location */
  public <V extends T> V register(V value, ResourceLocation name) {
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
      builder.put(value, register(mapper.apply(value), value.getSerializedName() + "_" + name));
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
      builder.put(value, register(mapper.apply(value), prefix + "_" + value.getSerializedName()));
    }
    return builder.build();
  }
}
