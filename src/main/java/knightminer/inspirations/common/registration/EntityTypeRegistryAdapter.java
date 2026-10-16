package knightminer.inspirations.common.registration;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Entity type registration, API of Mantle 1.20's EntityTypeRegistryAdapter (MIT, SlimeKnights) */
public class EntityTypeRegistryAdapter extends RegistryAdapter<EntityType<?>> {
  public EntityTypeRegistryAdapter(RegisterEvent.RegisterHelper<EntityType<?>> helper) {
    super(helper);
  }

  public <T extends Entity> EntityType<T> register(EntityType.Builder<T> builder, String name) {
    return register(builder.build(getResource(name).toString()), name);
  }
}
