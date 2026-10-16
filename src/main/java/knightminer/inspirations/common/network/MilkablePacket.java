package knightminer.inspirations.common.network;

import knightminer.inspirations.Inspirations;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static knightminer.inspirations.tweaks.TweaksEvents.TAG_MILKCOOLDOWN;

/** Tells clients whether a cow can be milked, for the milk cooldown */
public record MilkablePacket(int entityID, boolean milkable) implements CustomPacketPayload {
  public static final Type<MilkablePacket> TYPE = new Type<>(Inspirations.getResource("milkable"));
  public static final StreamCodec<RegistryFriendlyByteBuf, MilkablePacket> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.INT, MilkablePacket::entityID,
    ByteBufCodecs.BOOL, MilkablePacket::milkable,
    MilkablePacket::new);

  public MilkablePacket(Entity entity, boolean milkable) {
    this(entity.getId(), milkable);
  }

  @Override
  public Type<MilkablePacket> type() {
    return TYPE;
  }

  static void handle(MilkablePacket packet, IPayloadContext context) {
    HandleClient.handle(packet);
  }

  /**
   * Simply a separate class to safely load the client side logic
   */
  private static class HandleClient {
    private static void handle(MilkablePacket packet) {
      var level = Minecraft.getInstance().level;
      Entity entity = level == null ? null : level.getEntity(packet.entityID);
      if (entity == null) {
        return;
      }
      // value for not milkable does not matter as long as its greater than 0
      entity.getPersistentData().putShort(TAG_MILKCOOLDOWN, (short)(packet.milkable ? 0 : 100));
    }
  }
}
