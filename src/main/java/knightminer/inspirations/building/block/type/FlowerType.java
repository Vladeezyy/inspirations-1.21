package knightminer.inspirations.building.block.type;

import net.minecraft.world.item.DyeColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * Variants for each of the mulch types
 */
public enum FlowerType implements StringRepresentable {
  CYAN(Items.DYE.pick(DyeColor.CYAN)),
  SYRINGA(Items.DYE.pick(DyeColor.MAGENTA)),
  PAEONIA(Items.DYE.pick(DyeColor.PINK)),
  ROSE(Items.DYE.pick(DyeColor.RED));

  private final String name = name().toLowerCase(Locale.ROOT);
  private final Item dye;

  FlowerType(Item dye) {
    this.dye = dye;
  }

  /**
   * Gets the dye for this color
   * @return Dye color
   */
  public Item getDye() {
    return dye;
  }

  @Override
  public String getSerializedName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
