package knightminer.inspirations.cauldrons.block.entity;

import dev.vladeezyy.kilnwork.block.SyncedBlockEntity;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.common.network.CauldronPotionUpdatePacket;
import knightminer.inspirations.common.network.InspirationsNetwork;
import knightminer.inspirations.library.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/** Block entity for a cauldron that also holds a potion */
public class PotionCauldronBlockEntity extends SyncedBlockEntity {
	private static final String TAG_POTION = "potion";

	/** Potion in the cauldron, null for none (1.20 used the empty potion) */
	@Nullable
	private Holder<Potion> potion = null;

	public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(InspirationsCaudrons.potionCauldronEntity, pos, state);
	}

	/** Gets the potion in the cauldron, null if none */
	@Nullable
	public Holder<Potion> getPotion() {
		return potion;
	}

	/** Potion of a potion item, null if it has none */
	@Nullable
	public static Holder<Potion> potionOf(ItemStack stack) {
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(null);
	}

	/** True if the item is a water bottle of its kind */
	public static boolean isWater(ItemStack stack) {
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
	}

	/** Contents for items and effects, empty if no potion */
	public PotionContents getPotionContents() {
		return potion == null ? PotionContents.EMPTY : new PotionContents(potion);
	}

	/**
	 * Updates the potion in the cauldron
	 * @param potion  New potion, null for none
	 * @return  True if the potion changed
	 */
	public boolean setPotion(@Nullable Holder<Potion> potion) {
		if (!Objects.equals(potion, this.potion)) {
			this.potion = potion;
			if (level != null && !level.isClientSide) {
				InspirationsNetwork.sendToClients(level, worldPosition, new CauldronPotionUpdatePacket(worldPosition, Optional.ofNullable(potion)));
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
		// the potion id, the format of 1.20
		nbt.putString(TAG_POTION, potion == null ? "minecraft:empty" : potion.unwrapKey().map(key -> key.location().toString()).orElse("minecraft:empty"));
	}

	/** Parses the potion from its name, null for empty or unknown (1.20 worlds have minecraft:empty) */
	@Nullable
	private static Holder<Potion> parsePotion(String key) {
		ResourceLocation id = ResourceLocation.tryParse(key);
		if (id != null) {
			return BuiltInRegistries.POTION.getHolder(id).orElse(null);
		}
		return null;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		potion = parsePotion(nbt.getString(TAG_POTION));
		if (level != null && level.isClientSide) {
			MiscUtil.notifyClientUpdate(this);
		}
	}
}
