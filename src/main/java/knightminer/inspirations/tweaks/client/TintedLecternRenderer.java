package knightminer.inspirations.tweaks.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import knightminer.inspirations.Inspirations;
import knightminer.inspirations.common.client.ItemColorSources;
import knightminer.inspirations.common.network.InspirationsNetwork;
import knightminer.inspirations.library.MiscUtil;
import knightminer.inspirations.library.client.ClientUtil;
import knightminer.inspirations.tweaks.network.RequestLecternBookPacket;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/** Based on {@link net.minecraft.client.renderer.blockentity.LecternRenderer}, but tints the book cover */
public class TintedLecternRenderer implements BlockEntityRenderer<LecternBlockEntity, TintedLecternRenderer.State> {
  private static final String TAG_REQUESTED = "inspirations_requested";
  public static final SpriteId BOOK_LOCATION = new SpriteId(TextureAtlas.LOCATION_BLOCKS, Inspirations.getResource("entity/lectern_book"));
  private static final BookModel.State BOOK_STATE = BookModel.State.forAnimation(0.0F, 0.1F, 0.9F, 1.2F);

  private final SpriteGetter sprites;
  private final BookModel bookModel;
  /** Parts drawn with the tint, and the rest of the book */
  private final List<ModelPart> cover, pages;

  public TintedLecternRenderer(BlockEntityRendererProvider.Context context) {
    this.sprites = context.sprites();
    ModelPart book = context.bakeLayer(ModelLayers.BOOK);
    this.bookModel = new BookModel(book);
    // the pose never changes, the parts below are drawn alone with it
    bookModel.setupAnim(BOOK_STATE);
    this.cover = List.of(book.getChild("left_lid"), book.getChild("right_lid"), book.getChild("seam"));
    this.pages = List.of(book.getChild("left_pages"), book.getChild("right_pages"), book.getChild("flip_page1"), book.getChild("flip_page2"));
  }

  public static class State extends BlockEntityRenderState {
    boolean hasBook;
    float yRot;
    /** Cover color, -1 for the vanilla book */
    int tint = -1;
  }

  @Override
  public State createRenderState() {
    return new State();
  }

  @Override
  public void extractRenderState(LecternBlockEntity lectern, State state, float partialTicks, Vec3 camera, @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
    BlockEntityRenderer.super.extractRenderState(lectern, state, partialTicks, camera, crumbling);
    BlockState block = lectern.getBlockState();
    state.hasBook = block.getValue(LecternBlock.HAS_BOOK);
    state.yRot = block.getValue(LecternBlock.FACING).getClockWise().toYRot();
    state.tint = -1;
    if (!state.hasBook) {
      // mark the book as not requested so changing books requests it again
      lectern.getPersistentData().remove(TAG_REQUESTED);
      return;
    }
    // if we have not requested it, request the book from the server
    CompoundTag persistent = lectern.getPersistentData();
    if (!persistent.getBooleanOr(TAG_REQUESTED, false)) {
      persistent.putBoolean(TAG_REQUESTED, true);
      InspirationsNetwork.sendToServer(new RequestLecternBookPacket(lectern.getBlockPos()));
    }
    // check if we care about this book, use default for vanilla or unfetched
    ItemStack book = lectern.getBook();
    if (!book.isEmpty() && !book.is(Items.WRITTEN_BOOK) && !book.is(Items.WRITABLE_BOOK)) {
      int color = ClientUtil.getItemColor(book.getItem());
      int itemColors = ItemColorSources.getColor(book, 0);
      if (color != -1 || itemColors != -1) {
        if (itemColors != -1) {
          // make item colors result dominate
          color = MiscUtil.combineColors(color, itemColors, 3);
        }
        state.tint = ARGB.opaque(color);
      }
    }
  }

  @Override
  public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
    if (!state.hasBook) {
      return;
    }
    pose.pushPose();
    pose.translate(0.5F, 1.0625F, 0.5F);
    pose.rotateDegrees(Axis.YP, -state.yRot);
    pose.rotateDegrees(Axis.ZP, 67.5F);
    pose.translate(0.0F, -0.125F, 0.0F);
    if (state.tint == -1) {
      collector.submitModel(bookModel, BOOK_STATE, pose, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, EnchantTableRenderer.BOOK_TEXTURE, sprites, 0);
    } else {
      RenderType type = BOOK_LOCATION.renderType(RenderTypes::entitySolid);
      var sprite = sprites.get(BOOK_LOCATION);
      for (ModelPart part : cover) {
        collector.submitModelPart(part, pose, type, state.lightCoords, OverlayTexture.NO_OVERLAY, sprite, state.tint, 0);
      }
      for (ModelPart part : pages) {
        collector.submitModelPart(part, pose, type, state.lightCoords, OverlayTexture.NO_OVERLAY, sprite, -1, 0);
      }
    }
    // the break overlay is its own submit since 26.3, over the whole book
    if (state.breakProgress != null) {
      collector.order(1).submitCrumblingOverlay(bookModel, BOOK_STATE, pose, EnchantTableRenderer.BOOK_TEXTURE.renderType(bookModel.renderType()),
        state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
    }
    pose.popPose();
  }

  @Override
  public net.minecraft.world.phys.AABB getRenderBoundingBox(LecternBlockEntity blockEntity) {
    net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
    return new net.minecraft.world.phys.AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.5, pos.getZ() + 1.0);
  }
}
