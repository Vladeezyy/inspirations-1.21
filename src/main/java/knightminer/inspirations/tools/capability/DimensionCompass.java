package knightminer.inspirations.tools.capability;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.network.DimensionCompassPositionPacket;
import knightminer.inspirations.common.network.InspirationsNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.Nullable;

/**
 * Position where the player entered the current dimension. A data attachment on players, the 1.20 capability of the
 * same id; positions saved by 1.20 worlds are not read (they reset on the next dimension change).
 */
public class DimensionCompass implements IDimensionCompass {
	public static final Identifier KEY = Inspirations.getResource("dimension_compass");

	/** Attachment type, saved with the player */
	public static final AttachmentType<DimensionCompass> TYPE = AttachmentType.builder(DimensionCompass::new).serialize(new IAttachmentSerializer<DimensionCompass>() {
		@Override
		public DimensionCompass read(IAttachmentHolder holder, ValueInput input) {
			DimensionCompass compass = new DimensionCompass();
			compass.read(input);
			return compass;
		}

		@Override
		public boolean write(DimensionCompass attachment, ValueOutput output) {
			if (attachment.enteredPosition == null) {
				return false;
			}
			attachment.write(output);
			return true;
		}
	}).build();

	/** Registers the attachment and the listeners */
	public static void register() {
		NeoForge.EVENT_BUS.addListener(DimensionCompass::dimensionChange);
		NeoForge.EVENT_BUS.addListener(DimensionCompass::playerLoggedIn);
		NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, DimensionCompass::dimensionTravel);
	}

	/** Called from the module's registry event */
	public static void registerAttachment(RegisterEvent event) {
		event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, helper -> helper.register(KEY, TYPE));
	}

	/** Compass data of the player */
	public static DimensionCompass get(Player player) {
		return player.getData(TYPE);
	}

	@Nullable
	private BlockPos enteredPosition;

	@Nullable
	@Override
	public BlockPos getEnteredPosition() {
		return enteredPosition;
	}

	@Override
	public void setEnteredPosition(@Nullable BlockPos pos) {
		enteredPosition = pos;
	}

	/** Writes {X, Y, Z}, the 1.20 format */
	public void write(ValueOutput output) {
		if (enteredPosition != null) {
			output.putInt("X", enteredPosition.getX());
			output.putInt("Y", enteredPosition.getY());
			output.putInt("Z", enteredPosition.getZ());
		}
	}

	public void read(ValueInput input) {
		var x = input.getInt("X");
		var y = input.getInt("Y");
		var z = input.getInt("Z");
		if (x.isPresent() && y.isPresent() && z.isPresent()) {
			setEnteredPosition(new BlockPos(x.get(), y.get(), z.get()));
		} else {
			setEnteredPosition(null);
		}
	}


	// events

	/**
	 * Syncs the position to the player
	 * @param player  Player
	 * @param pos     New position
	 */
	private static void sync(Player player, @Nullable BlockPos pos) {
		if (player instanceof ServerPlayer serverPlayer) {
			InspirationsNetwork.sendTo(new DimensionCompassPositionPacket(pos), serverPlayer);
		}
	}

	/**
	 * Called when the player changes dimensions to update the position
	 * @param event  Event
	 */
	private static void dimensionChange(PlayerChangedDimensionEvent event) {
		Player player = event.getEntity();
		BlockPos pos = player.blockPosition();
		sync(player, pos);
		get(player).setEnteredPosition(pos);
	}

	/**
	 * Less ideal event for end because dimension change is not fired for the end portal
	 * Lowest priority should ensure this is not canceled, and if its not vanilla, the dimension change will update later
	 */
	private static void dimensionTravel(EntityTravelToDimensionEvent event) {
		if (event.getDimension() == Level.OVERWORLD) {
			Entity entity = event.getEntity();
			if (entity.level().dimension() == Level.END && entity instanceof ServerPlayer player) {
				// probably not needed as the client reset in my experience, but might as well
				get(player).setEnteredPosition(null);
				sync(player, null);
			}
		}
	}

	/**
	 * Called when the player joins the server to send them the stored position
	 * @param event  Event
	 */
	private static void playerLoggedIn(PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		BlockPos pos = get(player).getEnteredPosition();
		// defaults to null, so sync should not be needed
		if (pos != null) {
			sync(player, pos);
		}
	}
}
