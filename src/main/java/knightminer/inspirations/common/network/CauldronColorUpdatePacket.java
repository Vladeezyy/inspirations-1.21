package knightminer.inspirations.common.network;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.cauldrons.block.entity.DyeCauldronBlockEntity;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Packet to update the color in a cauldron */
public record CauldronColorUpdatePacket(BlockPos pos, int color) implements CustomPacketPayload {
  public static final Type<CauldronColorUpdatePacket> TYPE = new Type<>(Inspirations.getResource("cauldron_color"));
  public static final StreamCodec<RegistryFriendlyByteBuf, CauldronColorUpdatePacket> STREAM_CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, CauldronColorUpdatePacket::pos,
    ByteBufCodecs.INT, CauldronColorUpdatePacket::color,
    CauldronColorUpdatePacket::new);

  @Override
  public Type<CauldronColorUpdatePacket> type() {
    return TYPE;
  }

  static void handle(CauldronColorUpdatePacket packet, IPayloadContext context) {
    HandleClient.handle(packet);
  }

  /** Once removed client class */
  private static class HandleClient {
    private static void handle(CauldronColorUpdatePacket packet) {
      var level = Minecraft.getInstance().level;
      if (level != null && level.isLoaded(packet.pos) && level.getBlockEntity(packet.pos) instanceof DyeCauldronBlockEntity te && te.setColor(packet.color)) {
        MiscUtil.notifyClientUpdate(te);
      }
    }
  }
}
