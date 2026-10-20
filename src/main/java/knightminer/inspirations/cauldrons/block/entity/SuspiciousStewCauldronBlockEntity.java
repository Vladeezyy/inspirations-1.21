package knightminer.inspirations.cauldrons.block.entity;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import dev.vladeezyy.kilnwork.block.SyncedBlockEntity;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Block entity for cauldron suspicious stew contents */
public class SuspiciousStewCauldronBlockEntity extends SyncedBlockEntity {
	/** Effects in 1.21, the codec of the suspicious stew component */
	private static final String TAG_EFFECTS = "effects";
	/** Keys of 1.20 worlds, the stew NBT of 1.20: {Effects: [{EffectId: byte, forge:effect_id: string, EffectDuration: int}]} */
	private static final String LEGACY_EFFECTS = "Effects";
	private static final String LEGACY_EFFECT_ID = "EffectId";
	private static final String LEGACY_EFFECT_NAME = "forge:effect_id";
	private static final String LEGACY_DURATION = "EffectDuration";

	private List<SuspiciousStewEffects.Entry> effects = List.of();

	public SuspiciousStewCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(InspirationsCaudrons.suspiciousStewCauldronEntity, pos, state);
	}

	/** Gets the effects in the cauldron, as the stew component */
	public SuspiciousStewEffects getEffects() {
		return new SuspiciousStewEffects(effects);
	}

	/** Sets the effects in the cauldron */
	public void setEffects(SuspiciousStewEffects effects) {
		this.effects = List.copyOf(effects.effects());
		this.setChangedFast();
	}

	/** Adds a new effect to the cauldron */
	public void addEffect(Holder<MobEffect> effect, int duration) {
		List<SuspiciousStewEffects.Entry> list = new ArrayList<>(effects);
		list.add(new SuspiciousStewEffects.Entry(effect, duration));
		this.effects = List.copyOf(list);
		this.setChangedFast();
	}

	/** Adds the durations of the list, times the size, into the map */
	private static void scaleList(Map<Holder<MobEffect>, Integer> existingEffects, List<SuspiciousStewEffects.Entry> list, int size) {
		for (SuspiciousStewEffects.Entry entry : list) {
			existingEffects.merge(entry.effect(), entry.duration() * size, Integer::sum);
		}
	}

	/**
	 * Merges the given effects into the existing ones, averaging the durations by the stew amounts
	 * @param existingSize  Amount of stew in the cauldron
	 * @param mergeEffects  Effects to add
	 * @param mergeSize     Amount of stew added
	 */
	public void mergeEffects(int existingSize, SuspiciousStewEffects mergeEffects, int mergeSize) {
		Map<Holder<MobEffect>, Integer> existingEffects = new LinkedHashMap<>();
		scaleList(existingEffects, effects, existingSize);
		scaleList(existingEffects, mergeEffects.effects(), mergeSize);
		int totalSize = mergeSize + existingSize;
		List<SuspiciousStewEffects.Entry> list = new ArrayList<>();
		existingEffects.forEach((effect, duration) -> list.add(new SuspiciousStewEffects.Entry(effect, duration / totalSize)));
		this.effects = List.copyOf(list);
		this.setChangedFast();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		var effects = input.read(TAG_EFFECTS, SuspiciousStewEffects.CODEC);
		if (effects.isPresent()) {
			this.effects = effects.get().effects();
		} else {
			input.read(LEGACY_EFFECTS, CompoundTag.CODEC.listOf()).ifPresent(list -> this.effects = readLegacy(list));
		}
	}

	/** Reads the effects of a 1.20 world */
	private static List<SuspiciousStewEffects.Entry> readLegacy(List<CompoundTag> list) {
		List<SuspiciousStewEffects.Entry> entries = new ArrayList<>();
		for (CompoundTag tag : list) {
			Holder<MobEffect> effect = null;
			var name = tag.getString(LEGACY_EFFECT_NAME);
			if (name.isPresent()) {
				Identifier id = Identifier.tryParse(name.get());
				if (id != null) {
					effect = BuiltInRegistries.MOB_EFFECT.get(id).orElse(null);
				}
			}
			if (effect == null && tag.get(LEGACY_EFFECT_ID) instanceof net.minecraft.nbt.NumericTag numeric) {
				// 1.20 numeric ids are the vanilla registration order, ids start at 1
				effect = BuiltInRegistries.MOB_EFFECT.get(numeric.byteValue() - 1).orElse(null);
			}
			if (effect != null) {
				entries.add(new SuspiciousStewEffects.Entry(effect, tag.getIntOr(LEGACY_DURATION, 0)));
			}
		}
		return List.copyOf(entries);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store(TAG_EFFECTS, SuspiciousStewEffects.CODEC, getEffects());
	}
}
