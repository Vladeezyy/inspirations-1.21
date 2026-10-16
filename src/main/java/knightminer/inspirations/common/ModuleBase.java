package knightminer.inspirations.common;

import knightminer.inspirations.Inspirations;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public class ModuleBase {
  /**
   * Adds entries from a loot table in the inspirations directory to a vanilla loot table
   * @param event LootTableLoadEvent
   * @param name  Name of vanilla table and the inspirations table
   */
  protected static void addToVanillaLoot(LootTableLoadEvent event, String name) {
    if (!event.getName().getNamespace().equals("minecraft") || !event.getName().getPath().equals(name)) {
      return;
    }
    LootTable table = event.getTable();
    if (table != LootTable.EMPTY) {
      ResourceLocation location = Inspirations.getResource(name);
      table.addPool(new LootPool.Builder()
                        .name(location.toString())
                        .setRolls(ConstantValue.exactly(1))
                        .add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, location)))
                        .build()
                   );
    }
  }
}
