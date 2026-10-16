package knightminer.inspirations.common.registration;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Menu type registration, API of Mantle 1.20's ContainerTypeRegistryAdapter (MIT, SlimeKnights) */
public class ContainerTypeRegistryAdapter extends RegistryAdapter<MenuType<?>> {
  public ContainerTypeRegistryAdapter(RegisterEvent.RegisterHelper<MenuType<?>> helper) {
    super(helper);
  }

  /** Menu with extra data from the server, such as the block position */
  public <C extends AbstractContainerMenu> MenuType<C> registerType(IContainerFactory<C> factory, String name) {
    return register(IMenuTypeExtension.create(factory), name);
  }
}
