package knightminer.inspirations.tools.client;

import knightminer.inspirations.Inspirations;
import knightminer.inspirations.tools.entity.RedstoneArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class RedstoneArrowRenderer extends ArrowRenderer<RedstoneArrow, ArrowRenderState> {
  private static final Identifier CHARGED_ARROW = Inspirations.getResource("textures/entity/arrow/charged.png");

  public RedstoneArrowRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  protected Identifier getTextureLocation(ArrowRenderState state) {
    return CHARGED_ARROW;
  }

  @Override
  public ArrowRenderState createRenderState() {
    return new ArrowRenderState();
  }
}
