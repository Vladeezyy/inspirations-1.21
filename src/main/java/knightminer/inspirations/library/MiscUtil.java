package knightminer.inspirations.library;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MiscUtil {
  private static final String TAG_DISPLAY = "display";
  private static final String TAG_COLOR = "color";

  /**
   * Compute a voxelshape, rotated by the provided yaw.
   */
  public static VoxelShape makeRotatedShape(Direction side, int x1, int y1, int z1, int x2, int y2, int z2) {
    float yaw = -(float)Math.PI / 2F * side.get2DDataValue();
    Vec3 first = new Vec3(x1 - 8, y1 - 8, z1 - 8).yRot(yaw);
    Vec3 second = new Vec3(x2 - 8, y2 - 8, z2 - 8).yRot(yaw);
    return Shapes.box(
        0.5 + Math.min(first.x, second.x) / 16.0, 0.5 + Math.min(first.y, second.y) / 16.0, 0.5 + Math.min(first.z, second.z) / 16.0,
        0.5 + Math.max(first.x, second.x) / 16.0, 0.5 + Math.max(first.y, second.y) / 16.0, 0.5 + Math.max(first.z, second.z) / 16.0);
  }

  /**
   * Combines two colors
   * @param color1 First color
   * @param color2 Second color
   * @param scale  Determines how many times color2 is applied
   * @return Combined color
   */
  public static int combineColors(int color1, int color2, int scale) {
    if (scale == 0) {
      return color1;
    }
    int a = color1 >> 24 & 0xFF;
    int r = color1 >> 16 & 0xFF;
    int g = color1 >> 8 & 0xFF;
    int b = color1 & 0xFF;
    int a2 = color2 >> 24 & 0xFF;
    int r2 = color2 >> 16 & 0xFF;
    int g2 = color2 >> 8 & 0xFF;
    int b2 = color2 & 0xFF;

    for (int i = 0; i < scale; i++) {
      a = (int)Math.sqrt(a * a2);
      r = (int)Math.sqrt(r * r2);
      g = (int)Math.sqrt(g * g2);
      b = (int)Math.sqrt(b * b2);
    }
    return a << 24 | r << 16 | g << 8 | b;
  }

  /** List of dye color values */
  private static final int[] DYE_COLORS;
  static {
    DYE_COLORS = new int[16];
    for (DyeColor color : DyeColor.values()) {
      DYE_COLORS[color.getId()] = color.getTextureDiffuseColor() & 0xFFFFFF;
    }
  }

  /** RGB of an ARGB color, the format of 1.21 colors */
  /** The crafting remainder of the stack as a new stack, empty if none; a template since 26.1 */
  public static ItemStack getCraftingRemainder(ItemStack stack) {
    net.minecraft.world.item.ItemStackTemplate remainder = stack.getCraftingRemainder();
    return remainder == null ? ItemStack.EMPTY : remainder.create();
  }

  public static int rgb(int argb) {
    return argb & 0xFFFFFF;
  }

  /** Gets the color for the given dye */
  public static int getColor(DyeColor color) {
    return DYE_COLORS[color.getId()];
  }

  /**
   * Merge three float color components between 0 and 1 into a hex color integer
   * @param component float color component array, must be length 3
   * @return Color integer value
   */
  public static int getColorInteger(float[] component) {
    return ((int)(component[0] * 255) & 0xFF) << 16
           | ((int)(component[1] * 255) & 0xFF) << 8
           | ((int)(component[2] * 255) & 0xFF);
  }

  /**
   * Gets the dye color for the given color int
   * @param color Dye color input
   * @return EnumDyeColor matching, or null for no match
   */
  @Nullable
  public static DyeColor getDyeForColor(int color) {
    for (DyeColor dyeColor : DyeColor.values()) {
      if (getColor(dyeColor) == color) {
        return dyeColor;
      }
    }
    return null;
  }

  /**
   * Gets the color from the given stack, the {@code minecraft:dyed_color} component
   * @param stack  Stack
   * @return  Stack color, -1 if none
   */
  public static int getColor(ItemStack stack) {
    DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
    return color != null ? color.rgb() : -1;
  }

  /**
   * Checks if the stack currently has color
   * @param stack  Stack to check
   * @return  True if it has color
   */
  public static boolean hasColor(ItemStack stack) {
    return stack.has(DataComponents.DYED_COLOR);
  }

  /**
   * Sets the color on the given stack
   * @param stack  Stack
   * @param color  Color to set
   * @return  Stack with color
   */
  public static ItemStack setColor(ItemStack stack, int color) {
    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color & 0xFFFFFF));
    return stack;
  }

  /**
   * Clears the color on a stack
   * @param stack  Stack instance
   * @return  Stack without color
   */
  public static ItemStack clearColor(ItemStack stack) {
    stack.remove(DataComponents.DYED_COLOR);
    return stack;
  }

  /**
   * Notifies render global that TE data changed requiring a block update.
   * Used since refreshModelData alone does not seem sufficient.
   * @param te  TE to update
   */
  public static void notifyClientUpdate(BlockEntity te) {
    Level world = te.getLevel();
    if (world != null && world.isClientSide()) {
      te.requestModelDataUpdate();
      BlockState state = te.getBlockState();
      world.sendBlockUpdated(te.getBlockPos(), state, state, Block.UPDATE_NONE | Block.UPDATE_SUPPRESS_DROPS);
    }
  }

  /** Shrinks the held item */
  public static void shrinkHeldItem(Player player, InteractionHand hand, ItemStack held, int count) {
    if (!player.getAbilities().instabuild) {
      held.shrink(count);
      if (held.isEmpty()) {
        player.setItemInHand(hand, ItemStack.EMPTY);
      }
    }
  }

  /** Gives the given item to the player */
  public static void givePlayerItem(Player player, ItemStack stack) {
    if (!player.getInventory().add(stack)) {
      player.drop(stack, false);
    }
  }

	/**
	 * Divides a sum of colors, favoring pref if the remainder is non-zero
	 * @param sum      Color sum
	 * @param pref     Preferred component
	 * @param divisor  Number to divide by
	 * @return Divided sum
	 */
	private static int divide(int sum, int pref, int divisor) {
		int color = sum / divisor;
		// if there was a remainder, favor the original color
		if (sum % divisor != 0 && pref > color) {
			color++;
		}
		return color;
	}

	/**
	 * Gets the red value of a color
	 * @param color  Color
	 * @return Red value
	 */
	private static int getRed(int color) {
		return (color & 0xFF0000) >> 16;
	}

	/**
	 * Gets the green value of a color
	 * @param color  Color
	 * @return Green value
	 */
	private static int getGreen(int color) {
		return (color & 0xFF00) >> 8;
	}

	/**
	 * Gets the blue value of a color
	 * @param color  Color
	 * @return Blue value
	 */
	private static int getBlue(int color) {
		return (color & 0xFF);
	}

	/**
	 * Adds two colors
	 * @param newColor    New color added
	 * @param newLevels   Amount of new color added
	 * @param original    Color in cauldron
	 * @param origLevels  Number of levels in the cauldron
	 * @return  Added colors
	 */
	public static int addColors(int newColor, int newLevels, int original, int origLevels) {
		// keep original components as we average towards them
		int nr = getRed(newColor);
		int ng = getGreen(newColor);
		int nb = getBlue(newColor);
		// sum color components, add in 4 copies as a bottle is 4 levels
		// add in one copy per level of the original color
		int r = (nr * newLevels) + (getRed(original)   * origLevels);
		int g = (ng * newLevels) + (getGreen(original) * origLevels);
		int b = (nb * newLevels) + (getBlue(original)  * origLevels);
		// base acts as preference
		// divide per component, tending towards base
		int c = origLevels + newLevels;
		return divide(r, nr, c) << 16 | divide(g, ng, c) << 8 | divide(b, nb, c);
	}

	/**
	 * Adds particles
	 * @param type   Particle type
	 * @param world  World instance
	 * @param pos    Block position
	 * @param offset Current fluid height
	 * @param rand   Random instance
	 */
	public static void addParticles(ParticleOptions type, Level world, BlockPos pos, int count, double offset, RandomSource rand) {
		for (int i = 0; i < count; i++) {
			double x = pos.getX() + 0.1875D + (rand.nextFloat() * 0.625D);
			double y = pos.getY() + offset;
			double z = pos.getZ() + 0.1875D + (rand.nextFloat() * 0.625D);
			world.addParticle(type, x, y, z, 0, 0, 0);
		}
	}
}
