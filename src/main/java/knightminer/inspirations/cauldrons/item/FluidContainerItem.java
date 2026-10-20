package knightminer.inspirations.cauldrons.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Item holding a fixed amount of fluid that can only be drained whole, leaving the empty container: milk bottles and
 * potato soup. Replaces Mantle's ConstantFluidContainerWrapper of 1.20; a transfer API handler since NeoForge 21.9.
 */
public class FluidContainerItem extends ItemAccessResourceHandler<FluidResource> {
  /** Fluid in a bowl, Mantle's value */
  public static final int BOWL = 250;
  /** Fluid in a bottle, Mantle's value */
  public static final int BOTTLE = 250;

  private final FluidResource fluid;
  private final int amount;
  private final Item full;
  private final Item empty;

  public FluidContainerItem(ItemAccess access, Item full, FluidStack fluid, Item empty) {
    super(access, 1);
    this.fluid = FluidResource.of(fluid);
    this.amount = fluid.getAmount();
    this.full = full;
    this.empty = empty;
  }

  private boolean isFull(ItemResource resource) {
    return resource.is(full);
  }

  @Override
  protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
    return isFull(accessResource) ? fluid : FluidResource.EMPTY;
  }

  @Override
  protected int getAmountFrom(ItemResource accessResource, int index) {
    return isFull(accessResource) ? amount : 0;
  }

  @Override
  protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
    // all or nothing, like a bucket: only the empty container is a valid result
    return newAmount == 0 ? ItemResource.of(new ItemStack(empty)) : ItemResource.EMPTY;
  }

  @Override
  public boolean isValid(int index, FluidResource resource) {
    return false;
  }

  @Override
  protected int getCapacity(int index, FluidResource resource) {
    return amount;
  }
}
