package knightminer.inspirations.tweaks.network;

import knightminer.inspirations.Inspirations;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Packet to give the client the book of a lectern, for the tinted lectern book */
public record LecternBookPacket(BlockPos pos, ItemStack book) implements CustomPacketPayload {
  public static final Type<LecternBookPacket> TYPE = new Type<>(Inspirations.getResource("lectern_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, LecternBookPacket> STREAM_CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, LecternBookPacket::pos,
    ItemStack.OPTIONAL_STREAM_CODEC, LecternBookPacket::book,
    LecternBookPacket::new);

  public LecternBookPacket {
    book = book.copy();
  }

  @Override
  public Type<LecternBookPacket> type() {
    return TYPE;
  }

  public static void handle(LecternBookPacket packet, IPayloadContext context) {
    HandleClient.handle(packet);
  }

  private static class HandleClient {
    private static void handle(LecternBookPacket packet) {
      var level = Minecraft.getInstance().level;
      if (level != null && level.isLoaded(packet.pos) && level.getBlockEntity(packet.pos) instanceof LecternBlockEntity lectern) {
        lectern.setBook(packet.book);
      }
    }
  }
}
