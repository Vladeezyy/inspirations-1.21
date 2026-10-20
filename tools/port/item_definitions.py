#!/usr/bin/env python3
"""Writes the client item definitions (assets/inspirations/items/*.json, needed since 1.21.4) from the item models.

    python3 tools/port/item_definitions.py <minecraft client resources jar> <neoforge jar>

One definition per models/item/<id>.json:
- "overrides" (item property predicates) become a range_dispatch on inspirations:property (named like the predicate,
  the Java getters are registered under that name); the overridden models lose their "overrides"
- the NeoForge fluid container loader becomes the neoforge:fluid_container item model, textures from its parent
- models whose parents use kilnwork:retextured or inspirations:shelf become kilnwork:retextured (texture component)
- faces with a tintindex get inspirations:item_color tint sources (the Java item colors, by item and index)
"""
import json
import sys
import zipfile
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[2] / 'src/main/resources/assets'
JARS = [zipfile.ZipFile(path) for path in sys.argv[1:]]
RETEXTURED = {'kilnwork:retextured', 'inspirations:shelf'}


def model_path(model_id):
    ns, path = model_id.split(':') if ':' in model_id else ('minecraft', model_id)
    return ns, path


def load(model_id):
    ns, path = model_path(model_id)
    local = ASSETS / ns / 'models' / (path + '.json')
    if local.exists():
        return json.loads(local.read_text())
    for jar in JARS:
        try:
            return json.loads(jar.read(f'assets/{ns}/models/{path}.json'))
        except KeyError:
            pass
    return None


def chain(model_id):
    """The model and its parents"""
    out = []
    while model_id and not model_id.startswith('builtin/') and len(out) < 20:
        model = load(model_id)
        if model is None:
            break
        out.append(model)
        model_id = model.get('parent')
    return out


def max_tint(models):
    tint = -1
    for model in models:
        for element in model.get('elements', []):
            for face in element.get('faces', {}).values():
                tint = max(tint, face.get('tintindex', -1))
        if 'elements' in model:
            break
        if 'books' in model:
            for book in model['books']:
                for element in book if isinstance(book, list) else [book]:
                    for face in element.get('faces', {}).values():
                        tint = max(tint, face.get('tintindex', -1))
    # generated item models: layerN is tint index N when the item has colors
    for model in models:
        layers = [int(key[5:]) for key in model.get('textures', {}) if key.startswith('layer') and key[5:].isdigit()]
        if layers and any(m.get('parent') in ('item/generated', 'minecraft:item/generated', 'item/handheld', 'minecraft:item/handheld') for m in models):
            tint = max(tint, max(layers))
            break
    return tint


def tints(item, models):
    return [{'type': 'inspirations:item_color', 'index': i} for i in range(max_tint(models) + 1)]


def plain(model_id, item):
    out = {'type': 'minecraft:model', 'model': model_id}
    t = tints(item, chain(model_id))
    if t:
        out['tints'] = t
    return out


def definition(item, model_id):
    model = load(model_id)
    models = chain(model_id)
    loaders = {m.get('loader') for m in models}
    if model.get('loader') == 'neoforge:fluid_container':
        textures = {}
        for m in reversed(models):
            textures.update(m.get('textures', {}))
        def ref(t):
            return t if ':' in t else 'minecraft:' + t
        out = {'type': 'neoforge:fluid_container', 'fluid': model['fluid'],
               'textures': {k: ref(v) for k, v in textures.items() if k in ('base', 'fluid', 'cover', 'particle')}}
        if model.get('flip_gas'):
            out['flip_gas'] = True
        return out
    if loaders & RETEXTURED:
        out = {'type': 'kilnwork:retextured', 'model': model_id}
        t = tints(item, models)
        if t:
            out['tints'] = t
        return out
    if 'overrides' in model:
        overrides = model['overrides']
        names = {key for o in overrides for key in o['predicate']}
        if len(names) != 1:
            raise ValueError(f'{item}: overrides on several properties {names}')
        name = names.pop()
        entries = [{'threshold': o['predicate'][name], 'model': plain(o['model'], item)} for o in overrides]
        return {'type': 'minecraft:range_dispatch', 'property': 'inspirations:property', 'name': name.split(':')[-1],
                'entries': entries, 'fallback': plain(model_id, item)}
    return plain(model_id, item)


count = 0
for path in sorted((ASSETS / 'inspirations/models/item').glob('*.json')):
    item = path.stem
    model_id = f'inspirations:item/{item}'
    out = {'model': definition(item, model_id)}
    target = ASSETS / 'inspirations/items' / f'{item}.json'
    target.parent.mkdir(exist_ok=True)
    target.write_text(json.dumps(out, indent=2) + '\n')
    count += 1
# overrides are read from the item definitions now; models with overrides would log an unknown field otherwise
for path in sorted((ASSETS / 'inspirations/models/item').rglob('*.json')):
    model = json.loads(path.read_text())
    if 'overrides' in model:
        del model['overrides']
        path.write_text(json.dumps(model, indent=2) + '\n')
# the fluid container loader is gone from block models since 1.21.4, the definitions hold all of it now
for path in sorted((ASSETS / 'inspirations/models/item').glob('*.json')):
    if json.loads(path.read_text()).get('loader') == 'neoforge:fluid_container':
        path.unlink()
print('item definitions written:', count)
