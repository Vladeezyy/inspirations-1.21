package knightminer.inspirations;

import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.common.CommonsClientEvents;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.common.InspirationsCommons;
import knightminer.inspirations.common.data.GlobalLootProvider;
import knightminer.inspirations.common.network.InspirationsNetwork;
import knightminer.inspirations.tools.InspirationsTools;
import knightminer.inspirations.tweaks.InspirationsTweaks;
import knightminer.inspirations.utility.InspirationsUtility;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.config.ModConfig.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;


@Mod(Inspirations.modID)
public class Inspirations {
  public static final String modID = "inspirations";
  public static final Logger log = LogManager.getLogger(modID);

  public Inspirations(IEventBus modBus, ModContainer container) {
    container.registerConfig(Type.SERVER, Config.SERVER_SPEC);
    container.registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
    // 1.20 loaded this file by hand before registration; a startup config is loaded right here, under the same name
    container.registerConfig(Type.STARTUP, Config.COMMON_SPEC, modID + "-common.toml");

    modBus.register(new InspirationsCommons());
    modBus.register(new InspirationsBuilding());
    modBus.register(new InspirationsUtility());
    modBus.register(new InspirationsTools());
    modBus.register(new InspirationsTweaks());
    modBus.register(new InspirationsCaudrons());
    modBus.addListener(Config::configChanged);
    modBus.addListener(InspirationsNetwork::register);

    if (FMLEnvironment.getDist() == Dist.CLIENT) {
      CommonsClientEvents.onConstruct(modBus);
    }
  }



  /* Utilities */

  /**
   * Gets a resource location under the Inspirations name
   * @param name Resource path
   * @return Resource location
   */
  public static Identifier getResource(String name) {
    return Identifier.fromNamespaceAndPath(modID, name);
  }

  /**
   * Gets a resource location as a string, under the Inspirations namespace
   * @param name Resource path
   * @return Resource location string
   */
  public static String resourceName(String name) {
    return String.format("%s:%s", modID, name.toLowerCase(Locale.US));
  }

  /**
   * Gets the given name prefixed with Inspirations.
   * @param name Name to prefix
   * @return Prefixed name
   */
  public static String prefix(String name) {
    return String.format("%s.%s", modID, name.toLowerCase(Locale.US));
  }
}
