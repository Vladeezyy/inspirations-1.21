package knightminer.inspirations.building.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.kilnwork.client.render.RenderItem;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import knightminer.inspirations.building.block.entity.ShelfInventory;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/** Items on a shelf that are not books (the books are in the block model) */
public class ShelfBlockEntityRenderer implements BlockEntityRenderer<ShelfBlockEntity, ShelfBlockEntityRenderer.State> {
	public ShelfBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

	/**
	 * Made again every frame for every shelf (1.21.9+), so the item states exist only for the slots drawn here: a shelf
	 * of books needs none
	 */
	public static class State extends BlockEntityRenderState {
		List<RenderItem> renderItems = List.of();
		@Nullable
		ItemStackRenderState[] items;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public boolean shouldRender(ShelfBlockEntity shelf, Vec3 camera) {
		// most shelves hold only books, which are in the block model
		return shelf.getInventory().getItemMask() != 0 && BlockEntityRenderer.super.shouldRender(shelf, camera);
	}

	@Override
	public void extractRenderState(ShelfBlockEntity shelf, State state, float partialTicks, Vec3 camera, @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(shelf, state, partialTicks, camera, crumbling);
		// first, find the model for item display locations
		state.renderItems = RenderItem.ITEM_LISTS.get(state.blockState, List.of());
		ShelfInventory inventory = shelf.getInventory();
		// only render non-books, books are rendered in the model
		int mask = inventory.getItemMask() & ((1 << Math.min(state.renderItems.size(), ShelfInventory.MAX_ITEMS)) - 1);
		if (mask == 0) {
			return;
		}
		state.items = new ItemStackRenderState[ShelfInventory.MAX_ITEMS];
		for (int i = 0; i < ShelfInventory.MAX_ITEMS; i++) {
			if ((mask & 1 << i) != 0) {
				ItemStackRenderState item = state.items[i] = new ItemStackRenderState();
				state.renderItems.get(i).update(item, inventory.getStackInSlot(i), shelf.getLevel(), (int) shelf.getBlockPos().asLong() + i);
			}
		}
	}

	@Override
	public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.items == null) {
			return;
		}
		// if the block is rotatable, rotate item display
		BlockState block = state.blockState;
		boolean isRotated = RenderItem.applyRotation(matrices, block);
		for (int i = 0; i < state.renderItems.size() && i < state.items.length; i++) {
			if (state.items[i] != null) {
				state.renderItems.get(i).submit(matrices, collector, state.items[i], state.lightCoords);
			}
		}
		if (isRotated) {
			matrices.popPose();
		}
	}
}
