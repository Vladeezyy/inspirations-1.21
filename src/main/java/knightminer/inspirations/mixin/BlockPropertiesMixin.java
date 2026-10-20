package knightminer.inspirations.mixin;

import knightminer.inspirations.common.registration.RegistryAdapter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * Since 1.21.2 a block reads its id from its properties when it is made. Blocks of this mod are made inside
 * {@link RegistryAdapter#make(java.util.function.Supplier, String)}, which knows the name: the properties take it
 * (they are shared by several blocks in 1.20 code, so an id set for an earlier block is replaced).
 */
@Mixin(BlockBehaviour.Properties.class)
abstract class BlockPropertiesMixin {
  @Shadow
  @Nullable
  private ResourceKey<Block> id;

  @Shadow
  public abstract BlockBehaviour.Properties setId(ResourceKey<Block> id);

  @Inject(method = {"effectiveDrops", "effectiveDescriptionId"}, at = @At("HEAD"))
  private void inspirations$pendingId(CallbackInfoReturnable<?> cir) {
    Identifier pending = RegistryAdapter.pendingId();
    if (pending != null && (id == null || !id.identifier().equals(pending))) {
      setId(ResourceKey.create(Registries.BLOCK, pending));
    }
  }
}
