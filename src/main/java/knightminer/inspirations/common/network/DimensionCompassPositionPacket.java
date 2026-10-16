package knightminer.inspirations.common.network;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.tools.capability.DimensionCompass;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Packet to sync the dimension compass position
 */
public record DimensionCompassPositionPacket(Optional<BlockPos> pos) implements CustomPacketPayload {
  public static final Type<DimensionCompassPositionPacket> TYPE = new Type<>(Inspirations.getResource("dimension_compass"));
  public static final StreamCodec<RegistryFriendlyByteBuf, DimensionCompassPositionPacket> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.optional(BlockPos.STREAM_CODEC), DimensionCompassPositionPacket::pos,
    DimensionCompassPositionPacket::new);

  public DimensionCompassPositionPacket(@Nullable BlockPos pos) {
    this(Optional.ofNullable(pos));
  }

  @Override
  public Type<DimensionCompassPositionPacket> type() {
    return TYPE;
  }

  static void handle(DimensionCompassPositionPacket packet, IPayloadContext context) {
    HandleClient.handle(packet);
  }

  private static class HandleClient {
    private static void handle(DimensionCompassPositionPacket packet) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
        DimensionCompass.get(player).setEnteredPosition(packet.pos.orElse(null));
      }
    }
  }
}
