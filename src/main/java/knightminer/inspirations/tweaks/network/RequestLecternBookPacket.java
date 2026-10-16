package knightminer.inspirations.tweaks.network;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.network.InspirationsNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Packet for a client to request the lectern book */
public record RequestLecternBookPacket(BlockPos pos) implements CustomPacketPayload {
  public static final Type<RequestLecternBookPacket> TYPE = new Type<>(Inspirations.getResource("request_lectern_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, RequestLecternBookPacket> STREAM_CODEC = BlockPos.STREAM_CODEC.map(RequestLecternBookPacket::new, RequestLecternBookPacket::pos).cast();
  /** Lecterns farther than this are not answered, the client only asks for lecterns it renders */
  private static final double MAX_DISTANCE_SQR = 128 * 128;

  @Override
  public Type<RequestLecternBookPacket> type() {
    return TYPE;
  }

  public static void handle(RequestLecternBookPacket packet, IPayloadContext context) {
    BlockPos pos = packet.pos;
    // the position comes from the client: 1.20 answered for any loaded lectern of the world
    if (context.player() instanceof ServerPlayer player && player.blockPosition().distSqr(pos) <= MAX_DISTANCE_SQR
        && player.level().isLoaded(pos) && player.level().getBlockEntity(pos) instanceof LecternBlockEntity lectern) {
      ItemStack book = lectern.getBook();
      // skip syncing written books, they are big packets, and we are not going to show them anyways
      if (!book.isEmpty() && !book.is(Items.WRITABLE_BOOK) && !book.is(Items.WRITTEN_BOOK)) {
        InspirationsNetwork.sendTo(new LecternBookPacket(pos, book), player);
      }
    }
  }
}
