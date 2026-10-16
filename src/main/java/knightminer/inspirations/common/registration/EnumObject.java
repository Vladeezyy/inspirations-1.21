package knightminer.inspirations.common.registration;

import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Registry objects per enum value, filled during registration. Same API as Mantle 1.20's EnumObject (MIT,
 * SlimeKnights), which Inspirations was written against.
 */
public class EnumObject<K extends Enum<K>, V> {
  private static final EnumObject<?, ?> EMPTY = new EnumObject<>(Collections.emptyMap());

  private final Map<K, V> map;

  private EnumObject(Map<K, V> map) {
    this.map = map;
  }

  /** Object with no values, used before registration */
  @SuppressWarnings("unchecked")
  public static <K extends Enum<K>, V> EnumObject<K, V> empty() {
    return (EnumObject<K, V>) EMPTY;
  }

  /** Value for the key, throws if missing */
  public V get(K key) {
    V value = map.get(key);
    if (value == null) {
      throw new NullPointerException("No value for " + key);
    }
    return value;
  }

  @Nullable
  public V getOrNull(K key) {
    return map.get(key);
  }

  public boolean contains(@Nullable Object value) {
    return value != null && map.containsValue(value);
  }

  public Set<K> keySet() {
    return map.keySet();
  }

  /** Values in enum order */
  public List<V> values() {
    return new ArrayList<>(map.values());
  }

  public Collection<V> valueCollection() {
    return map.values();
  }

  public void forEach(BiConsumer<K, ? super V> consumer) {
    map.forEach(consumer);
  }

  public void forEach(Consumer<? super V> consumer) {
    map.values().forEach(consumer);
  }

  /** Item of every value, for item likes */
  public void forEachItem(Consumer<ItemLike> consumer) {
    for (V value : map.values()) {
      if (value instanceof ItemLike item) {
        consumer.accept(item);
      }
    }
  }

  public static class Builder<K extends Enum<K>, V> {
    private final Map<K, V> map;

    public Builder(Class<K> keyClass) {
      this.map = new EnumMap<>(keyClass);
    }

    public Builder<K, V> put(K key, V value) {
      map.put(key, value);
      return this;
    }

    public Builder<K, V> putAll(EnumObject<K, ? extends V> other) {
      map.putAll(other.map);
      return this;
    }

    public EnumObject<K, V> build() {
      return new EnumObject<>(Collections.unmodifiableMap(map));
    }
  }
}
