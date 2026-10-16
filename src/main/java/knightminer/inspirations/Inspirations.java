package knightminer.inspirations;

import knightminer.inspirations.building.InspirationsBuilding;
import knightminer.inspirations.cauldrons.InspirationsCaudrons;
import knightminer.inspirations.common.CommonsClientEvents;
import knightminer.inspirations.common.Config;
import knightminer.inspirations.common.InspirationsCommons;
import knightminer.inspirations.common.data.GlobalLootProvider;
import knightminer.inspirations.common.datagen.FluidTransferProvider;
import knightminer.inspirations.common.datagen.InspirationsBlockTagsProvider;
import knightminer.inspirations.common.datagen.InspirationsDamageTypeProvider;
import knightminer.inspirations.common.datagen.InspirationsFluidTagsProvider;
import knightminer.inspirations.common.datagen.InspirationsItemTagsProvider;
import knightminer.inspirations.common.datagen.InspirationsLootTableProvider;
import knightminer.inspirations.common.datagen.RenderItemProvider;
import knightminer.inspirations.common.datagen.SpriteSourcesProvider;
import knightminer.inspirations.common.network.InspirationsNetwork;
import knightminer.inspirations.tools.InspirationsTools;
import knightminer.inspirations.tweaks.InspirationsTweaks;
import knightminer.inspirations.utility.InspirationsUtility;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.config.ModConfig.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.Set;
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

    modBus.register(this);
    modBus.register(new InspirationsCommons());
    modBus.register(new InspirationsBuilding());
    modBus.register(new InspirationsUtility());
    modBus.register(new InspirationsTools());
    modBus.register(new InspirationsTweaks());
    modBus.register(new InspirationsCaudrons());
    modBus.addListener(Config::configChanged);
    modBus.addListener(InspirationsNetwork::register);
    NeoForge.EVENT_BUS.register(Inspirations.class);

    if (FMLEnvironment.dist == Dist.CLIENT) {
      CommonsClientEvents.onConstruct(modBus);
    }
  }

  @SubscribeEvent
  void gatherData(GatherDataEvent event) {
    DataGenerator gen = event.getGenerator();
    PackOutput packOutput = gen.getPackOutput();
    CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
    boolean server = event.includeServer();
    ExistingFileHelper existing = event.getExistingFileHelper();

    RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder();
    InspirationsDamageTypeProvider.registerGenerator(registrySetBuilder);
    DatapackBuiltinEntriesProvider datapackRegistryProvider = new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, registrySetBuilder, Set.of(modID));
    gen.addProvider(server, datapackRegistryProvider);
    lookupProvider = datapackRegistryProvider.getRegistryProvider();

    BlockTagsProvider blockTags = new InspirationsBlockTagsProvider(packOutput, lookupProvider, existing);
    gen.addProvider(server, blockTags);
    gen.addProvider(server, new InspirationsItemTagsProvider(packOutput, lookupProvider, blockTags.contentsGetter(), existing));
    gen.addProvider(server, new InspirationsFluidTagsProvider(packOutput, lookupProvider, existing));
    gen.addProvider(server, new InspirationsLootTableProvider(packOutput, lookupProvider));
    gen.addProvider(server, new GlobalLootProvider(packOutput, lookupProvider));
    gen.addProvider(server, new InspirationsDamageTypeProvider(packOutput, lookupProvider, existing));
    gen.addProvider(server, new FluidTransferProvider(packOutput, lookupProvider));

    boolean client = event.includeClient();
    gen.addProvider(client, new SpriteSourcesProvider(packOutput, lookupProvider, existing));
    gen.addProvider(client, new RenderItemProvider(packOutput));
  }


  /* Utilities */

  /**
   * Gets a resource location under the Inspirations name
   * @param name Resource path
   * @return Resource location
   */
  public static ResourceLocation getResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(modID, name);
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
