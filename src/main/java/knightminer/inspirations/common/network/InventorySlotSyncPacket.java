package knightminer.inspirations.common.network;

import knightminer.inspirations.Inspirations;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Syncs one slot of a block inventory, such as a shelf, to clients */
public record InventorySlotSyncPacket(ItemStack itemStack, int slot, BlockPos pos) implements CustomPacketPayload {
  public static final Type<InventorySlotSyncPacket> TYPE = new Type<>(Inspirations.getResource("inventory_slot_sync"));
  public static final StreamCodec<RegistryFriendlyByteBuf, InventorySlotSyncPacket> STREAM_CODEC = StreamCodec.composite(
    ItemStack.OPTIONAL_STREAM_CODEC, InventorySlotSyncPacket::itemStack,
    ByteBufCodecs.SHORT.map(Short::intValue, Integer::shortValue), InventorySlotSyncPacket::slot,
    BlockPos.STREAM_CODEC, InventorySlotSyncPacket::pos,
    InventorySlotSyncPacket::new);

  public InventorySlotSyncPacket {
    // single player hands the client this very object, it must not share the stack of the server
    itemStack = itemStack.copy();
  }

  @Override
  public Type<InventorySlotSyncPacket> type() {
    return TYPE;
  }

  static void handle(InventorySlotSyncPacket packet, IPayloadContext context) {
    HandleClient.handle(packet);
  }

  /**
   * Simply a separate class to safely load the client side logic
   */
  private static class HandleClient {
    private static void handle(InventorySlotSyncPacket packet) {
      // Only ever sent to players in the same dimension as the position
      var level = Minecraft.getInstance().level;
      if (level == null || !level.isLoaded(packet.pos)) {
        return;
      }
      IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, packet.pos, null);
      if (handler instanceof IItemHandlerModifiable itemHandler && packet.slot >= 0 && packet.slot < itemHandler.getSlots()) {
        itemHandler.setStackInSlot(packet.slot, packet.itemStack);
      }
    }
  }
}
