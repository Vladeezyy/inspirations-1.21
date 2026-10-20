#!/usr/bin/env python3
"""Converts the mod's 1.21.11 data to 26.1, in place (run after recipes_1_21_11.py, once per checkout).

    python3 tools/port/data_26_1.py

- NeoForge 26.1 loads every file of data/*/loot_modifiers itself: the list global_loot_modifiers.json is gone (it would
  be read as a modifier and fail with "No key type").
- The item tag minecraft:dyeable is gone: the cauldron washes the dye off #minecraft:cauldron_can_remove_dye, and dyeing
  in the crafting grid is one minecraft:crafting_dye recipe per item, as vanilla does for leather armor.
"""
import json
from pathlib import Path

DATA = Path(__file__).resolve().parents[2] / 'src/generated/resources/data'


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n')


done = []
lst = DATA / 'neoforge/loot_modifiers/global_loot_modifiers.json'
if lst.exists():
    lst.unlink()
    done.append('removed ' + str(lst.relative_to(DATA)))

dyeable = DATA / 'minecraft/tags/item/dyeable.json'
if dyeable.exists():
    items = json.loads(dyeable.read_text())['values']
    write(DATA / 'minecraft/tags/item/cauldron_can_remove_dye.json', {'values': items})
    dyeable.unlink()
    done.append('dyeable -> cauldron_can_remove_dye')
    # the crafting grid dyeing of 1.21.11 (minecraft:crafting_special_armordye on #dyeable), with the item's config
    props = {'inspirations:colored_book': 'colored_books', 'inspirations:dimension_compass': 'dimension_compass'}
    for item in items:
        name = item.split(':')[1]
        recipe = {'type': 'minecraft:crafting_dye', 'category': 'misc', 'dye': '#minecraft:dyes', 'group': 'inspirations:dyed_' + name,
                  'result': {'id': item}, 'target': item}
        if item in props:
            recipe = {'neoforge:conditions': [{'type': 'inspirations:config', 'prop': props[item]}], **recipe}
        write(DATA / f'inspirations/recipe/dye/{name}.json', recipe)
        done.append('recipe dye/' + name)
print('\n'.join(done) or 'nothing to convert')
