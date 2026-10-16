package knightminer.inspirations.common.network;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.cauldrons.block.entity.PotionCauldronBlockEntity;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

/** Packet to update the potion in a cauldron, empty for no potion */
public record CauldronPotionUpdatePacket(BlockPos pos, Optional<Holder<Potion>> potion) implements CustomPacketPayload {
  public static final Type<CauldronPotionUpdatePacket> TYPE = new Type<>(Inspirations.getResource("cauldron_potion"));
  public static final StreamCodec<RegistryFriendlyByteBuf, CauldronPotionUpdatePacket> STREAM_CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, CauldronPotionUpdatePacket::pos,
    ByteBufCodecs.optional(Potion.STREAM_CODEC), CauldronPotionUpdatePacket::potion,
    CauldronPotionUpdatePacket::new);

  @Override
  public Type<CauldronPotionUpdatePacket> type() {
    return TYPE;
  }

  static void handle(CauldronPotionUpdatePacket packet, IPayloadContext context) {
    HandleClient.handle(packet);
  }

  /** Once removed client class */
  private static class HandleClient {
    private static void handle(CauldronPotionUpdatePacket packet) {
      var level = Minecraft.getInstance().level;
      if (level != null && level.isLoaded(packet.pos) && level.getBlockEntity(packet.pos) instanceof PotionCauldronBlockEntity te && te.setPotion(packet.potion.orElse(null))) {
        MiscUtil.notifyClientUpdate(te);
      }
    }
  }
}
