package knightminer.inspirations.cauldrons.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * Item holding a fixed amount of fluid that can only be drained whole, leaving the empty container: milk bottles and
 * potato soup. Replaces Mantle's ConstantFluidContainerWrapper of 1.20.
 */
public class FluidContainerItem implements IFluidHandlerItem {
  /** Fluid in a bowl, Mantle's value */
  public static final int BOWL = 250;
  /** Fluid in a bottle, Mantle's value */
  public static final int BOTTLE = 250;

  private ItemStack container;
  private final FluidStack fluid;
  private final Item empty;
  private boolean drained = false;

  public FluidContainerItem(ItemStack container, FluidStack fluid, Item empty) {
    this.container = container;
    this.fluid = fluid;
    this.empty = empty;
  }

  @Override
  public ItemStack getContainer() {
    return container;
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    return drained ? FluidStack.EMPTY : fluid.copy();
  }

  @Override
  public int getTankCapacity(int tank) {
    return fluid.getAmount();
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return false;
  }

  @Override
  public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
    return 0;
  }

  @Override
  public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
    if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluid)) {
      return FluidStack.EMPTY;
    }
    return drain(resource.getAmount(), action);
  }

  @Override
  public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
    // all or nothing, like a bucket
    if (drained || maxDrain < fluid.getAmount() || container.getCount() != 1) {
      return FluidStack.EMPTY;
    }
    if (action.execute()) {
      container = new ItemStack(empty);
      drained = true;
    }
    return fluid.copy();
  }
}
