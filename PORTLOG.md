# Inspirations 1.5.0 → NeoForge 1.21.1: port log

Original: [KnightMiner/Inspirations](https://github.com/KnightMiner/Inspirations), branch `1.20` (1.5.0, Forge 1.20.1,
Mantle 1.11, commit `ff7be877`, includes four fixes made after the last release 1.5.0.6). The first commit of this
repository is that code unchanged, so `git diff` against it shows the port.

## Scope (decided 2026-10-16)

- Everything of the `1.20` branch is ported. Features the author removed before 1.20 (anvil smashing, milking squids,
  ice, grass drops, faster rain and others) are not brought back; language keys left over from them are removed.
- Mantle is replaced by our library [Kilnwork](https://github.com/Vladeezyy/kilnwork) 1.1.0 (the parts Inspirations
  needs: retextured blocks, models, recipes and loot functions, inventory blocks and menus, item lists, colored block
  models, 1.20 item stacks in block entities), as in our Ceramics port. It ships inside the jar (Jar-in-Jar).
- The five registry substitutions of the original (water cauldron, 16 carpets, hopper, iron bars, shield item) are not
  possible on NeoForge 1.21; they are done with mixins into the vanilla classes. Each can still be turned off in the
  config.

## Method

- Java kept file by file with the original packages and class names. Code that only moved between APIs is changed in
  place; behavior changes are commented in the code.
- Resources: assets of the original kept (model loaders renamed to Kilnwork's), datagen of the original ported and run
  (`./gradlew runData`): 205 recipes, 205 advancements, 94 loot tables, the same counts as the 1.20 output.
- Tests are in a separate source set `src/selftest`, loaded only in dev runs, never in the jar.

## API changes (1.20.1 Forge → 1.21.1 NeoForge)

| 1.20.1 | 1.21.1 |
|---|---|
| registry substitution of `water_cauldron`, carpets, `hopper`, `iron_bars`, `shield` | mixins: `LayeredCauldronBlockMixin` (boiling), `WoolCarpetBlockMixin` (fit to stairs), `HopperBlockMixin` (waterlogging), `IronBarsBlockMixin` (ladder over rope), `ShieldItemMixin` (enchantable) |
| enchantment classes replaced (protection, thorns, knockback, fire aspect, looting) | data driven enchantments: `supportsEnchantment` / `isPrimaryItemFor` on shields and axes, `EnchantmentMixin` and `EnchantmentHelperMixin` (protection only while blocking, thorns from the shield event) |
| Mantle `NetworkWrapper` packets | `CustomPacketPayload` records with `StreamCodec` |
| capabilities (`LazyOptional`) for shelves, collectors, pipes, dimension compass | `RegisterCapabilitiesEvent`, `BlockCapabilityCache`, a data attachment for the compass |
| `CauldronInteraction` maps | `CauldronInteraction.newInteractionMap`, `ItemInteractionResult` |
| potion NBT, `PotionUtils` | `PotionContents`, `Holder<Potion>`, brewing through `RegisterBrewingRecipesEvent` |
| item NBT (`texture`, colors, compass target, suspicious stew effects) | components: `kilnwork:texture`, `DYED_COLOR`, `LODESTONE_TRACKER`, `SUSPICIOUS_STEW_EFFECTS` |
| Forge conditions and loot modifiers | `neoforge:conditions` with `inspirations:config`, GLM `inspirations:replace_item` |
| `Block.use` | `useItemOn` / `useWithoutItem` |
| `forge:` tags | `c:` tags (`c:honey`, `c:bottles/splash`, soups) |
| common config | `ModConfig.Type.STARTUP` (registration reads it) |

**Worlds from 1.20.** The world upgrade keeps unknown item NBT as `minecraft:custom_data` and block entity data as is.
Shelves, bushes and cauldrons read the old `texture` keys (also from `ForgeData`), old item stacks in their inventories
(`LegacyItemStacks`, through the vanilla data fixers), old suspicious stew `Effects` and the potion `minecraft:empty`.
The dimension compass position of 1.20 players (a Forge capability) is not read.

## Bugs fixed (of the original or found during the port)

| Bug | Where it came from |
|---|---|
| Creative shelves textured with planks, the recipe takes slabs, JEI shows no recipe ([#247](https://github.com/KnightMiner/Inspirations/issues/247)) | `InspirationsCommons` creative tab used `ItemTags.PLANKS` |
| Two collectors duplicate one dropped item ([#227](https://github.com/KnightMiner/Inspirations/issues/227)) | the stack was inserted without a copy |
| Crashes with mods that change cauldrons or iron bars ([#245](https://github.com/KnightMiner/Inspirations/issues/245), [#250](https://github.com/KnightMiner/Inspirations/issues/250)) | registry substitutions, replaced by mixins |
| Crash while chunks build: `ConcurrentModificationException` in block colors and shelf models | `ClientUtil.COLOR_CACHE` and `ShelfModel.texturedCache` were `HashMap`s used by the chunk builder threads (found by the stress scene) |
| Shelf cannot be turned off | config keys `building.bookshelf.enable` and `building.bookshelf` collide, the file keeps one; now `building.shelf` |
| Skeleton skulls, cave spider webs and flowers never drop | loot table listeners were never registered |
| Shearing vines: the check was inverted | `ToolsEvents` |
| Dispensers placing anvils also dispense them, missing return | `InspirationsTweaks` dispenser behavior |
| Lectern book requests answered for any position | `RequestLecternBookPacket` had no distance check |
| Potion clouds apply effects twice | lingering bottle interaction |
| Client menus created with a null block entity | menu factories |
| Shelf inventory not cleared before loading | `ShelfInventory.deserialize` |
| Wrong hue in `ClientUtil` color mixing | HSB arguments swapped |
| Rope shape uses the wrong position | `RopeBlock` |
| Book power cache never cleared | `InspirationsRegistry.clearBookCache` is now called on tag reload |
| A potion with only custom effects poured into an empty cauldron made a cauldron of the empty potion | `PotionIntoEmptyInteraction` |
| Missing names: potted flowers, rabbit stew cauldron (`block.inspirations.rabbit_stew` typo); fitted carpets and wet hopper now use the vanilla names (as carpeted pressure plates do) | `en_us.json` |
| Dead non-static client event handler; `NeoForge.EVENT_BUS.register(Inspirations.class)` with a non-static method crashes on 1.21 | `CommonsClientEvents`, `Inspirations` |
| Dyeable items tag missing colored books and dimension compass; `mantle:soup` incomplete | tag providers |

Open issues of the original checked and not changed: #251 (ModernFix reload listener: the listener is registered
through `RegisterClientReloadListenersEvent`, no error in the run with ModernFix), #244 and #237 (1.19 crashes, fixed
upstream), #231 (the dimension compass recipe has the config condition), #236 (not reproducible upstream either), #240
(documentation), feature requests #242, #241, #221 (French translation posted as text in an issue, not added), #202,
#158, #126, #99, #96, #50, #48, #18, #14. #226 is done (`c:honey`).

## Languages

`en_us` cleaned: 101 keys of removed features (TAN juices, brick buttons, 1.16 cauldron and JEI texts, potions the
mod no longer registers) dropped, 5 missing names added; 123 keys, all used (the client scene checks both directions).
`ru_ru`, `de_de`, `zh_cn` completed (the original's translations kept where they fit), `uk_ua`, `es_es`, `pt_br` new.
All from `tools/lang/en_us_order.py` and `tools/lang/gen_lang.py`, terms from the vanilla 1.21.1 translations.
Checked with `modding-kb/scripts/langcheck.py`; the client scene passes in `ru_ru` and `de_de`.

## Verification

- `tools/selftest.sh`: dev server, fresh world, 55 checks — registries, recipes and conditions, cauldron interactions
  (dye, potions, soups, honey, milk, boiling damage, brewing, tipped arrows), shelves (books, enchanting power,
  comparator), collectors (#227), pipes, rope, locks and keys, enchantments on shields and axes, loot injections,
  bonemeal, smooth growth, 1.20 data conversion, save/load.
- `tools/scenetest.sh` (client): showcase of every block with 11 screenshots, 40 checks — models and textures (no missing
  sprites, no magenta pixels on screen), retextured shelves and bushes, tints, fluid sprites, lectern book sync, block
  entity client data, boiling particles, item properties, screens, JEI (#247), config resource pack, translations.
  Passes in `en_us`, `ru_ru`, `de_de`; with Sodium, Iris, Lithium, FerriteCore, ModernFix and Jade (`-Pwith_compat`,
  40/40); on NeoForge 21.1.197 without JEI (37/37).
- `tools/scenetest.sh -Pscene=stress`: broken common config, 1024 shelves full of books and 96 cauldrons (235 FPS against
  253 on an empty floor), 200 ticks of 64 shelves and 32 cauldrons changing per tick (198 FPS; this found the chunk
  builder crash), 2000 lectern requests for random positions, enchanting power and milk cooldown at their limits,
  leaving and rejoining the world — 10/10.
- `tools/nettest.sh`: dev client on a dev dedicated server, using blocks as a player — 11/11 (shelf, dye and potion
  cauldrons, lectern, collector screen, milk cooldown packet, dimension compass across dimensions).
- Release jar on real NeoForge 21.1.197 and 21.1.259 dedicated servers (`prodtest/serve.sh`): starts, Kilnwork loaded
  from the jar, no errors. The jar has the license texts and no test classes.

## Minecraft 1.21.11 (NeoForge 21.11.48), branch `mc/1.21.11`

Ported from this 1.21.1 code with Kilnwork 1.1.0 for 1.21.11. Datagen is not ported; its 1.21.1 output is converted once
and kept as data.

- Models: the loaders `inspirations:trim` and `kilnwork:colored_block` became plain JSON (`tools/port/static_models.py`,
  rerun per Minecraft version). Retextured models (shelves, enlightened bushes) remap the quad UVs onto the texture
  block's sprite instead of baking a model per texture; shelf books are quad groups chosen by the `BOOKS` model data.
- Item definitions (`items/*.json`, `tools/port/item_definitions.py`): item colors and properties stay Java functions,
  read through the tint source `inspirations:item_color` and the numeric property `inspirations:property`. The config
  resource pack also replaces the definitions of the enchanted book and the firework rocket (their tints).
- Data: `tools/port/recipes_1_21_11.py` converts the ingredients (recipes, the heartbeet loot modifier, Kilnwork fluid
  transfers) to the 1.21.2 string format and `dyed_color` to an integer.
- Registration: `Properties.setId` comes from a pending id set by the registry adapters (`BlockPropertiesMixin`,
  `ItemPropertiesMixin`).
- Fluid and item handlers on the transfer API (`ResourceHandler`, `ItemAccessResourceHandler`, `VanillaContainerWrapper`).
- Milk bottles remove one random effect of any kind: NeoForge 21.11 has no effect cures (vanilla milk clears all).
- Shields at the enchanting table need the `enchantable` component since 1.21.2: `EnchantmentHelperMixin` and
  `ItemStackMixin` give it to shields (12, 16 with a banner) while the config option is on.
- Hoppers keep their items when they turn wet or dry through `shouldChangedStateKeepBlockEntity` alone (1.21.5 removed
  `onRemove`; the drops are skipped with the block entity).
- `minecraft:cutout_mipped` is gone (NeoForge 21.11 knows solid, cutout, translucent, tripwire) and an unknown render
  type draws solid without a warning: the bushes use `cutout`, and Kilnwork keeps the render type of retextured parts.
- Mulch holds `VegetationBlock` plants (BushBlock is one kind of them since 1.21.5); bonemeal on sand and mycelium no
  longer casts to BushBlock.
- The shelf renderer is asked every frame for every shelf (render states, 1.21.9): shelves holding only books are
  skipped by `shouldRender`, the non-book slots are a cached mask.

Verification on 1.21.11: selftest 55/55, client scene 41/41 (11 screenshots compared with the 1.21.1 ones; the scene
world is in another biome now, so the vine rope has the savanna tint), net test 11/11. Stress (machine under other load,
numbers of one run): empty floor 151 FPS, vanilla baseline of 1024 full chiseled bookshelves 140 FPS, 1024 shelves full
of books 158 FPS at 7.6 MSPT, plus 96 cauldrons 154 FPS at 4.6 MSPT, 200 ticks of changes 126 FPS at 5.8 MSPT — 10/10.

## Minecraft 26.1.2 (NeoForge 26.1.2.121), branch `mc/26.1.2`

From `mc/1.21.11`, Java 25, Kilnwork `26.1.2-1.1.0`. Data converted once more with `tools/port/data_26_1.py`.

- Cauldron interactions are dispatchers (`CauldronInteractions.EMPTY/WATER`, `put(item, interaction)`); the
  Inspirations cauldrons register theirs as `inspirations:<name>` (`RegisterCauldronInteractionEvent.Dispatcher`), the
  default bucket interactions are added by the mod (package-private in vanilla). Interactions made in common setup do
  not make item stacks any more (components are bound later): results are suppliers.
- Dyes: the color is the `dye` component; the `dyeable` tag is gone — washing in a cauldron uses
  `#minecraft:cauldron_can_remove_dye`, crafting dyes one `crafting_dye` recipe per item (colored book, dimension
  compass).
- Block colors are lists of `BlockTintSource` per block (one per tint index); `AbstractClientEvents.tintSources`
  adapts the 1.21 color functions and makes their RGB opaque (the alpha counts since 26.1).
- Fluids: `RegisterFluidModelsEvent` with still/flowing materials and a constant tint (the client fluid extensions lost
  the textures); the translucent layer of honey comes from its texture.
- Models: the retextured parts and the shelf books on `BlockStateModelPart` and quad material infos; the layer of a
  retextured quad is taken from the new sprite.
- Item properties get the `ItemOwner` (item frames included) instead of `stack.getFrame()`.
- Recipes: `Recipe.CommonInfo`, book info and `ItemStackTemplate` results; the loot condition type is the codec;
  loot modifiers take a priority; `global_loot_modifiers.json` is gone (NeoForge loads the folder).
- GUI on `GuiGraphicsExtractor` (`extractBackground`, `extractLabels`), overlay messages with `sendOverlayMessage`,
  `level.getRandom()`, `ChunkPos.containing`, `BreakBlockEvent`.
- Tests: the net client joins the server from code (26.x ignores quick play), the time and weather are set with
  commands (clocks), the item tint layers are an `IntList`.

Verification on 26.1.2: selftest 57/57 (two new checks for the dye changes), client scene 41/41 (screenshots equal to
1.21.11), net test 11/11. Stress: empty floor 244 FPS, vanilla baseline 250 FPS, 1024 shelves 236 FPS at 3.2 MSPT,
plus 96 cauldrons 226 FPS at 3.1 MSPT, 200 ticks of changes 183 FPS at 4.3 MSPT — 10/10.

## License

Code: LGPL-3.0-only (`LICENSE`, GPL text in `licenses/COPYING`); the original is MIT (`licenses/LICENSE-MIT`).
The textures `books_ancient.png` and `books_tomes.png` stay CC BY-NC-SA 4.0 (`licenses/TEXTURE_LICENSE.md`).
