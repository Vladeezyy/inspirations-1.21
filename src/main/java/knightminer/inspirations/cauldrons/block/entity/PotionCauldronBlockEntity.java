package knightminer.inspirations.cauldrons.block.entity;

import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
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
import net.minecraft.resources.Identifier;
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
			if (level != null && !level.isClientSide()) {
				InspirationsNetwork.sendToClients(level, worldPosition, new CauldronPotionUpdatePacket(worldPosition, Optional.ofNullable(potion)));
			}
			this.setChangedFast();
			return true;
		}
		return false;
	}

	/** Broken (not turned into another cauldron): the potion makes a cloud, as in 1.20 */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		// the new block is already in the world here
		if (level == null || potion == null || level.getBlockState(pos).is(BlockTags.CAULDRONS)) {
			return;
		}
		int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);
		AreaEffectCloud cloud = new AreaEffectCloud(level, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
		cloud.setRadius(0.5F * waterLevel + 0.5F);
		cloud.setDuration(20 * (waterLevel + 1));
		cloud.setRadiusOnUse(-0.5F);
		cloud.setWaitTime(10);
		cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
		// 1.20 set the potion and added its effects again, so the cloud had every effect twice
		cloud.setPotionContents(getPotionContents());
		level.addFreshEntity(cloud);
		level.playSound(null, pos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
	}

	@Override
	protected boolean shouldSyncOnUpdate() {
		return true;
	}

	@Override
	protected void saveSynced(ValueOutput output) {
		super.saveSynced(output);
		// the potion id, the format of 1.20
		output.putString(TAG_POTION, potion == null ? "minecraft:empty" : potion.unwrapKey().map(key -> key.identifier().toString()).orElse("minecraft:empty"));
	}

	/** Parses the potion from its name, null for empty or unknown (1.20 worlds have minecraft:empty) */
	@Nullable
	private static Holder<Potion> parsePotion(String key) {
		Identifier id = Identifier.tryParse(key);
		if (id != null) {
			return BuiltInRegistries.POTION.get(id).orElse(null);
		}
		return null;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		potion = parsePotion(input.getStringOr(TAG_POTION, ""));
		if (level != null && level.isClientSide()) {
			MiscUtil.notifyClientUpdate(this);
		}
	}
}
