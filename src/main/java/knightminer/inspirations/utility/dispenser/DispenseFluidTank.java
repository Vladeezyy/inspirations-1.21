package knightminer.inspirations.utility.dispenser;

import knightminer.inspirations.library.InspirationsTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

/** Dispensers empty fluid containers into the fluid handler in front of them, or fill empty ones from it */
public class DispenseFluidTank extends DefaultDispenseItemBehavior {
  private static final DefaultDispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();
  private final DispenseItemBehavior fallback;

  public DispenseFluidTank(DispenseItemBehavior fallback) {
    this.fallback = fallback;
  }

  @Override
  protected ItemStack execute(BlockSource source, ItemStack stack) {
    if (!stack.is(InspirationsTags.Items.DISP_FLUID_TANKS)) {
      return fallback.dispense(source, stack);
    }

    Direction side = source.state().getValue(DispenserBlock.FACING);
    BlockPos pos = source.pos().relative(side);
    Level world = source.level();
    ResourceHandler<FluidResource> tank = world.getCapability(Capabilities.Fluid.BLOCK, pos, side.getOpposite());
    if (tank == null) {
      return fallback.dispense(source, stack);
    }

    // one item of the stack, the handler holds it after the exchange
    ItemStacksResourceHandler container = new ItemStacksResourceHandler(1);
    container.set(0, ItemResource.of(stack), 1);
    ResourceHandler<FluidResource> item = ItemAccess.forHandlerIndex(container, 0).getCapability(Capabilities.Fluid.ITEM);
    if (item != null) {
      // a filled container is emptied into the tank, an empty one filled from it
      boolean empty = !ResourceHandlerUtil.isEmpty(item);
      ResourceStack<FluidResource> moved = empty
        ? ResourceHandlerUtil.moveFirst(item, tank, fluid -> true, Integer.MAX_VALUE, null)
        : ResourceHandlerUtil.moveFirst(tank, item, fluid -> true, Integer.MAX_VALUE, null);
      if (moved != null) {
        FluidUtil.triggerSoundAndGameEvent(moved.resource(), world, Vec3.atCenterOf(pos), null, !empty);
        return consumeWithRemainder(source, stack, ItemUtil.getStack(container, 0));
      }
    }
    return DEFAULT.dispense(source, stack);
  }
}
