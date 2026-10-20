package knightminer.inspirations.tools;

import knightminer.inspirations.common.Config;
import knightminer.inspirations.common.ModuleBase;
import knightminer.inspirations.tools.block.RedstoneChargeBlock;
import knightminer.inspirations.tools.capability.DimensionCompass;
import knightminer.inspirations.tools.entity.RedstoneArrow;
import knightminer.inspirations.tools.item.DimensionCompassItem;
import knightminer.inspirations.tools.item.RedstoneArrowItem;
import knightminer.inspirations.tools.item.RedstoneChargerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import knightminer.inspirations.common.registration.BlockRegistryAdapter;
import knightminer.inspirations.common.registration.EntityTypeRegistryAdapter;
import knightminer.inspirations.common.registration.ItemRegistryAdapter;
import knightminer.inspirations.common.registration.RegistryAdapter;

public class InspirationsTools extends ModuleBase {
  // items
  public static Item lock;
  public static Item key;
  public static Item redstoneCharger;
  public static Item northCompass;
  public static Item barometer;
  public static Item photometer;
  public static ArrowItem redstoneArrow;

  // The "undyed" compass is White.
  public static Item dimensionCompass;

  // blocks
  public static Block redstoneCharge;

  public static EntityType<RedstoneArrow> entRSArrow;

  @SubscribeEvent
  public void setup(FMLCommonSetupEvent event) {
    NeoForge.EVENT_BUS.register(ToolsEvents.class);
    DimensionCompass.register();
    registerDispenserBehavior();
  }

  @SubscribeEvent
  public void register(RegisterEvent event) {
    event.register(Registries.BLOCK, helper -> {
      BlockRegistryAdapter registry = new BlockRegistryAdapter(helper);
      redstoneCharge = registry.make(() -> new RedstoneChargeBlock(
        Block.Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.POPPED).strength(0).lightLevel((state) -> 2)
      ), "redstone_charge");
    });
    event.register(Registries.ITEM, helper -> {
      ItemRegistryAdapter registry = new ItemRegistryAdapter(helper);
      Item.Properties props = new Item.Properties();

      redstoneArrow = registry.make(() -> new RedstoneArrowItem(props), "charged_arrow");
      redstoneCharger = registry.make(() -> new RedstoneChargerItem(new Item.Properties().durability(120).repairable(Items.REDSTONE)), "redstone_charger");

      lock = registry.register(props, "lock");
      key = registry.register(props, "key");

      northCompass = registry.register(props, "north_compass");
      barometer = registry.register(props, "barometer");
      photometer = registry.register(props, "photometer");
      dimensionCompass = registry.make(() -> new DimensionCompassItem(props), "dimension_compass");
      // the enchantable shield of 1.20 replaced the vanilla item; NeoForge cannot, ShieldItemMixin does it
    });
    event.register(Registries.ENTITY_TYPE, helper -> {
      EntityTypeRegistryAdapter registry = new EntityTypeRegistryAdapter(helper);
      entRSArrow = registry.register(EntityType.Builder
                                         .<RedstoneArrow>of(RedstoneArrow::new, MobCategory.MISC)
                                         .sized(0.5F, 0.5F)
                                         .clientTrackingRange(4)
                                         .updateInterval(20),
                                     "redstone_arrow");
    });
    // enchantments are data in 1.21: which items take them is in the mixins (AxeItemMixin, ShieldItemMixin, EnchantmentMixin)
    DimensionCompass.registerAttachment(event);
  }


  private void registerDispenserBehavior() {
    DispenserBlock.registerProjectileBehavior(redstoneArrow);
    DispenserBlock.registerBehavior(redstoneCharger, new OptionalDispenseItemBehavior() {
      @Override
      protected ItemStack execute(BlockSource source, ItemStack stack) {
        this.setSuccess(true);
        ServerLevel world = source.level();
        Direction facing = source.state().getValue(DispenserBlock.FACING);
        BlockPos pos = source.pos().relative(facing);

        if (world.getBlockState(pos).canBeReplaced(new DirectionalPlaceContext(
            world, pos, facing, ItemStack.EMPTY, facing
        ))) {
          world.setBlockAndUpdate(pos, redstoneCharge.defaultBlockState().setValue(RedstoneChargeBlock.FACING, facing));
          stack.hurtAndBreak(1, world, null, item -> {});
        } else {
          this.setSuccess(false);
        }

        return stack;
      }
    });
  }
}
