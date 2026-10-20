package knightminer.inspirations.library.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.vladeezyy.kilnwork.client.model.RetexturedBlockModel;
import dev.vladeezyy.kilnwork.client.model.RetexturedModelLoader;
import dev.vladeezyy.kilnwork.client.model.RetexturedQuads;
import knightminer.inspirations.building.block.entity.ShelfBlockEntity;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shelf model (loader {@code inspirations:shelf}): a retextured model (Kilnwork) plus {@code "books"}, one element or
 * a list of elements per slot. All books are baked once, each in its own quad group; the block model keeps the books
 * of the filled slots ({@link ShelfBlockEntity#BOOKS}), the item shows them all.
 */
public final class ShelfModel {
  private ShelfModel() {}

  public static final UnbakedModelLoader<UnbakedModel> LOADER = ShelfModel::read;

  private static UnbakedModel read(JsonObject json, JsonDeserializationContext context) {
    Set<String> retextured = RetexturedModelLoader.getRetexturedNames(json);
    JsonArray bookArray = GsonHelper.getAsJsonArray(json, "books");
    if (bookArray.isEmpty()) {
      throw new JsonSyntaxException("Must have at least one book element");
    }
    List<List<CuboidModelElement>> books = new ArrayList<>(bookArray.size());
    for (JsonElement book : bookArray) {
      List<CuboidModelElement> elements = new ArrayList<>();
      if (book.isJsonArray()) {
        for (JsonElement element : book.getAsJsonArray()) {
          elements.add(context.deserialize(element, CuboidModelElement.class));
        }
      } else {
        elements.add(context.deserialize(book, CuboidModelElement.class));
      }
      books.add(elements);
    }
    JsonObject vanilla = json.deepCopy();
    vanilla.remove("books");
    CuboidModel model = RetexturedModelLoader.readVanilla(vanilla, context);
    return new RetexturedModelLoader.Wrapped(model, new Geometry(model.geometry(), model.parent(), retextured, books));
  }

  /** The shelf with every book, each book in quad group = its slot */
  private static class Geometry extends RetexturedModelLoader.Geometry {
    private final List<List<CuboidModelElement>> books;

    Geometry(@Nullable UnbakedGeometry own, @Nullable Identifier parent, Set<String> retextured, List<List<CuboidModelElement>> books) {
      super(own, parent, retextured);
      this.books = books;
    }

    @Override
    protected QuadCollection bakeQuads(TextureSlots textures, ModelBaker baker, ModelState state, ModelDebugName name) {
      QuadCollection.Builder builder = new QuadCollection.Builder();
      add(builder, super.bakeQuads(textures, baker, state, name));
      for (int slot = 0; slot < books.size(); slot++) {
        QuadCollection book = UnbakedCuboidGeometry.bake(books.get(slot), textures, baker, state, name);
        RetexturedQuads.markGroup(book, slot);
        add(builder, book);
      }
      return builder.build();
    }

    private static void add(QuadCollection.Builder builder, QuadCollection quads) {
      quads.getQuads(null).forEach(builder::addUnculledFace);
      for (Direction side : Direction.values()) {
        for (BakedQuad quad : quads.getQuads(side)) {
          builder.addCulledFace(side, quad);
        }
      }
    }
  }

  /** Block model of the shelf: the books of the filled slots, retextured */
  public static class Block extends RetexturedBlockModel {
    public Block(BlockStateModel delegate) {
      super(delegate);
    }

    @Override
    protected int extraKey(ModelData data) {
      Integer books = data.get(ShelfBlockEntity.BOOKS);
      return books == null ? 0 : books;
    }

    @Override
    protected boolean alwaysTransform() {
      return true;
    }

    @Override
    protected boolean keep(BakedQuad quad, int books) {
      int slot = RetexturedQuads.group(quad);
      return slot < 0 || (books & 1 << slot) != 0;
    }
  }
}
