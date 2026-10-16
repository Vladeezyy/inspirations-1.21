# Inspirations 1.5.0 → NeoForge 1.21.1: port log

Original: [KnightMiner/Inspirations](https://github.com/KnightMiner/Inspirations), branch `1.20` (1.5.0, Forge 1.20.1,
Mantle 1.11, commit `ff7be877`, includes four fixes made after the last release 1.5.0.6). The first commit of this
repository is that code unchanged, so `git diff` against it shows the port.

## Scope (decided 2026-10-16)

- Everything of the `1.20` branch is ported. Features the author removed before 1.20 (anvil smashing, milking squids,
  ice, grass drops, faster rain and others) are not brought back.
- Mantle is replaced by our library [Kilnwork](https://github.com/Vladeezyy/kilnwork) 1.1.0 (the parts Inspirations
  needs: retextured blocks, models, recipes and loot functions, inventory blocks and menus, fluid types), as in our
  Ceramics port.
- The five registry substitutions of the original (water cauldron, 16 carpets, hopper, iron bars, shield item) are not
  possible on NeoForge 1.21; they are done with mixins into the vanilla classes. Each can still be turned off in the
  config.

## License

Code: LGPL-3.0-only (`LICENSE`, GPL text in `licenses/COPYING`); the original is MIT (`licenses/LICENSE-MIT`).
The textures `books_ancient.png` and `books_tomes.png` stay CC BY-NC-SA 4.0 (`licenses/TEXTURE_LICENSE.md`).
