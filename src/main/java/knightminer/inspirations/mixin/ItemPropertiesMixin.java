package knightminer.inspirations.mixin;

import knightminer.inspirations.common.registration.RegistryAdapter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * Item side of {@link BlockPropertiesMixin}: the properties take the name the item is registered under; block items
 * made by the registry adapter are named as their block, other items as themselves (the 1.21.1 names).
 */
@Mixin(Item.Properties.class)
abstract class ItemPropertiesMixin {
  @Shadow
  @Nullable
  private ResourceKey<Item> id;

  @Shadow
  public abstract Item.Properties setId(ResourceKey<Item> id);

  @Shadow
  public abstract Item.Properties useBlockDescriptionPrefix();

  @Shadow
  public abstract Item.Properties useItemDescriptionPrefix();

  @Inject(method = {"effectiveDescriptionId", "effectiveModel"}, at = @At("HEAD"))
  private void inspirations$pendingId(CallbackInfoReturnable<?> cir) {
    Identifier pending = RegistryAdapter.pendingId();
    if (pending != null && (id == null || !id.identifier().equals(pending))) {
      setId(ResourceKey.create(Registries.ITEM, pending));
      if (RegistryAdapter.pendingBlockItem()) {
        useBlockDescriptionPrefix();
      } else {
        useItemDescriptionPrefix();
      }
    }
  }
}
