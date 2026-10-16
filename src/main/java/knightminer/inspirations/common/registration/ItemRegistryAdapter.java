package knightminer.inspirations.common.registration;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Function;
import java.util.function.Supplier;

/** Item registration, API of Mantle 1.20's ItemRegistryAdapter (MIT, SlimeKnights) */
public class ItemRegistryAdapter extends RegistryAdapter<Item> {
  public ItemRegistryAdapter(RegisterEvent.RegisterHelper<Item> helper) {
    super(helper);
  }

  /** Registers a plain item */
  public Item register(Item.Properties props, String name) {
    return register(new Item(props), name);
  }

  /** Registers a block item named as the block */
  public BlockItem registerBlockItem(Block block, Item.Properties props) {
    return registerBlockItem(new BlockItem(block, props));
  }

  /** Registers the block item named as its block */
  public <I extends BlockItem> I registerBlockItem(I item) {
    return register(item, BuiltInRegistries.BLOCK.getKey(item.getBlock()));
  }

  /** Registers a block item for every block of the enum object */
  public <B extends Block> void registerBlockItem(EnumObject<?, B> blocks, Item.Properties props) {
    blocks.forEach(block -> registerBlockItem(block, props));
  }

  /** Registers a block item for every block of the enum object */
  public <B extends Block> void registerBlockItem(EnumObject<?, B> blocks, Function<B, ? extends BlockItem> mapper) {
    blocks.forEach(block -> registerBlockItem(mapper.apply(block)));
  }

  /** Registers default block items for every block of the enum object */
  public <B extends Block> void registerDefaultBlockItem(EnumObject<?, B> blocks) {
    registerBlockItem(blocks, new Item.Properties());
  }

  /** Registers a default block item */
  public BlockItem registerDefaultBlockItem(Block block) {
    return registerBlockItem(block, new Item.Properties());
  }

  /** Registers the bucket of the fluid, named {@code <name>_bucket} */
  public BucketItem registerBucket(Supplier<? extends Fluid> fluid, String name) {
    return register(new BucketItem(fluid.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)), name + "_bucket");
  }
}
