#!/usr/bin/env python3
"""Converts the item ingredients in the 1.21.1 data JSON of the mod (recipes, loot modifiers, fluid transfers) (generated and hand written) to the 1.21.2+ ingredient format, in place.

    python3 tools/port/recipes_1_21_11.py

Since 1.21.2 an ingredient is an item id, a #tag or a list of item ids; {"item"}/{"tag"} objects are gone, a list
mixing tags needs NeoForge's compound ingredient, custom NeoForge ingredients name their type in
"neoforge:ingredient_type". Ingredients sit in "key" (shaped), "ingredients" (shapeless), "ingredient" (stonecutting)
and "texture" (kilnwork:crafting_shaped_retextured); result dyed colors lose their {"rgb"} object. Datagen is gone in this port, so the converted files are kept.
Run once per checkout made from the 1.21.1 data; already converted files stay unchanged.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2] / 'src'
# components the 1.21.1 datagen wrote for a stack with only its defaults; the ingredient is partial (not strict),
# so dropping them keeps the match, and their 1.21.1 encoding no longer parses
DEFAULT_COMPONENTS = {'minecraft:attribute_modifiers', 'minecraft:enchantments', 'minecraft:lore', 'minecraft:max_stack_size',
                      'minecraft:rarity', 'minecraft:repair_cost'}


def ingredient(value):
    if isinstance(value, str):
        return value
    if isinstance(value, list):
        parts = [ingredient(v) for v in value]
        if len(parts) == 1:
            return parts[0]
        if all(isinstance(p, str) and not p.startswith('#') for p in parts):
            return parts
        return {'neoforge:ingredient_type': 'neoforge:compound', 'children': parts}
    if set(value) == {'item'}:
        return value['item']
    if set(value) == {'tag'}:
        return '#' + value['tag']
    if 'type' in value:
        out = {k: v for k, v in value.items() if k != 'type'}
        out = {'neoforge:ingredient_type': value['type'], **out}
        if value['type'] == 'neoforge:components':
            out['components'] = {k: v for k, v in value['components'].items() if k not in DEFAULT_COMPONENTS}
        return out
    return value


def components(value):
    """dyed_color is a plain RGB value since 1.21.5"""
    color = value.get('minecraft:dyed_color')
    if isinstance(color, dict):
        value['minecraft:dyed_color'] = color['rgb']
    return value


def convert(recipe):
    if isinstance(recipe.get('result'), dict) and 'components' in recipe['result']:
        components(recipe['result']['components'])
    if 'key' in recipe:
        recipe['key'] = {k: ingredient(v) for k, v in recipe['key'].items()}
    if 'ingredients' in recipe:
        recipe['ingredients'] = [ingredient(v) for v in recipe['ingredients']]
    for field in ('ingredient', 'texture'):
        if field in recipe:
            recipe[field] = ingredient(recipe[field])
    return recipe


def convert_field(field):
    def run(data):
        if field in data:
            data[field] = ingredient(data[field])
        return data
    return run


# recipes, plus the other files with item ingredients: global loot modifiers (inspirations:replace_item) and
# Kilnwork fluid transfers (item side; the fluid side is Kilnwork's own format)
FILES = [('recipe', convert), ('loot_modifiers', convert_field('original')), ('kilnwork/fluid_transfer', convert_field('input'))]
count = 0
for folder, converter in FILES:
    for path in sorted(ROOT.glob(f'*/resources/**/data/*/{folder}/**/*.json')):
        text = path.read_text()
        out = json.dumps(converter(json.loads(text)), indent=2) + '\n'
        if out != text:
            path.write_text(out)
            count += 1
print('files converted:', count)
