package knightminer.inspirations.common.network;

import knightminer.inspirations.tweaks.network.LecternBookPacket;
import knightminer.inspirations.tweaks.network.RequestLecternBookPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import javax.annotation.Nullable;

/** Packets of Inspirations, NeoForge payloads in place of Mantle's network wrapper of 1.20 */
public class InspirationsNetwork {
  private InspirationsNetwork() {}

  /** Registers all packets */
  public static void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar("1");
    registrar.playToClient(InventorySlotSyncPacket.TYPE, InventorySlotSyncPacket.STREAM_CODEC, InventorySlotSyncPacket::handle);
    registrar.playToClient(MilkablePacket.TYPE, MilkablePacket.STREAM_CODEC, MilkablePacket::handle);
    registrar.playToClient(CauldronColorUpdatePacket.TYPE, CauldronColorUpdatePacket.STREAM_CODEC, CauldronColorUpdatePacket::handle);
    registrar.playToClient(CauldronPotionUpdatePacket.TYPE, CauldronPotionUpdatePacket.STREAM_CODEC, CauldronPotionUpdatePacket::handle);
    registrar.playToClient(DimensionCompassPositionPacket.TYPE, DimensionCompassPositionPacket.STREAM_CODEC, DimensionCompassPositionPacket::handle);
    // lectern syncing
    registrar.playToServer(RequestLecternBookPacket.TYPE, RequestLecternBookPacket.STREAM_CODEC, RequestLecternBookPacket::handle);
    registrar.playToClient(LecternBookPacket.TYPE, LecternBookPacket.STREAM_CODEC, LecternBookPacket::handle);
  }

  /**
   * Sends a vanilla packet to a player
   * @param player Player receiving packet
   * @param packet Packet
   */
  public static void sendPacket(Entity player, Packet<?> packet) {
    if (player instanceof ServerPlayer serverPlayer) {
      serverPlayer.connection.send(packet);
    }
  }

  /** Sends a packet to one player */
  public static void sendTo(CustomPacketPayload packet, ServerPlayer player) {
    PacketDistributor.sendToPlayer(player, packet);
  }

  /** Sends a packet to the server */
  public static void sendToServer(CustomPacketPayload packet) {
    net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);
  }

  /**
   * Sends a packet to all clients near a location
   * @param world  World, does nothing if not a WorldServer
   * @param pos    Players too far from this position will not receive the packet
   * @param packet Packet
   */
  public static void sendToClients(@Nullable Level world, BlockPos pos, CustomPacketPayload packet) {
    if (world instanceof ServerLevel serverLevel) {
      sendToClients(serverLevel, pos, packet);
    }
  }

  /**
   * Sends a packet to all clients tracking the chunk of a location
   * @param world  World
   * @param pos    Position
   * @param packet Packet
   */
  public static void sendToClients(ServerLevel world, BlockPos pos, CustomPacketPayload packet) {
    PacketDistributor.sendToPlayersTrackingChunk(world, ChunkPos.containing(pos), packet);
  }
}
