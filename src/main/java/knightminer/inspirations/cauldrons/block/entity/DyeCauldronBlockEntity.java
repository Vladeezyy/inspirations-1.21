package knightminer.inspirations.cauldrons.block.entity;

import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.common.network.CauldronColorUpdatePacket;
import knightminer.inspirations.common.network.InspirationsNetwork;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import dev.vladeezyy.kilnwork.block.SyncedBlockEntity;

import javax.annotation.Nullable;

/** Block entity for a cauldron that also holds a color */
public class DyeCauldronBlockEntity extends SyncedBlockEntity {
	private static final String TAG_COLOR = "color";
	private int color;
	private boolean dyeDirty = true;
	@Nullable
	private DyeColor dye;
	public DyeCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(InspirationsCaudrons.dyeCauldronEntity, pos, state);
	}

	/** Gets the color */
	public int getColor() {
		return color;
	}

	/** Gets the given dye */
	@Nullable
	public DyeColor getDye() {
		if (dyeDirty) {
			dye = MiscUtil.getDyeForColor(color);
			dyeDirty = false;
		}
		return dye;
	}

	/**
	 * Updates the color, sending the proper packet on the server
	 * @param color  New color
	 * @return  True if the color changed
	 */
	public boolean setColor(int color) {
		if (color != this.color) {
			this.color = color;
			this.dyeDirty = true;
			if (level != null && !level.isClientSide) {
				InspirationsNetwork.sendToClients(level, worldPosition, new CauldronColorUpdatePacket(worldPosition, color));
			}
			this.setChangedFast();
			return true;
		}
		return false;
	}

	@Override
	protected boolean shouldSyncOnUpdate() {
		return true;
	}

	@Override
	protected void saveSynced(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveSynced(nbt, registries);

		nbt.putInt(TAG_COLOR, color);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		color = nbt.getInt(TAG_COLOR);
		if (level != null && level.isClientSide) {
			MiscUtil.notifyClientUpdate(this);
		}
		this.dyeDirty = true;
	}
}
