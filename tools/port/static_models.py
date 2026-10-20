#!/usr/bin/env python3
"""Turns the models of the 1.21.1 loaders inspirations:trim and kilnwork:colored_block into plain block models.

    python3 tools/port/static_models.py <minecraft client resources jar>

Both loaders only change the elements of the parent model once (trim: cuts N pixels off the top of every column, as
TrimModel did; colored_block: colors and light per element index, as Kilnwork's colored block model did). Since 1.21.5
block models have no such loaders, the result is written into the model itself: the same parent and textures plus the
computed "elements" (NeoForge "neoforge_data" for color and light). Run it again for each Minecraft version, the vanilla
parents (block/cactus, block/sugar_cane) can change.
"""
import copy
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2] / 'src/main/resources/assets'
VANILLA = zipfile.ZipFile(sys.argv[1])


def load(model_id):
    ns, path = model_id.split(':') if ':' in model_id else ('minecraft', model_id)
    local = ROOT / ns / 'models' / (path + '.json')
    if local.exists():
        return json.loads(local.read_text())
    return json.loads(VANILLA.read(f'assets/{ns}/models/{path}.json'))


def elements(model):
    """Elements of the model or of the first parent that has them"""
    while model is not None:
        if 'elements' in model:
            return copy.deepcopy(model['elements'])
        model = load(model['parent']) if 'parent' in model else None
    raise ValueError('no elements')


def trim_uv(face, amount):
    if 'uv' not in face:
        return face  # automatic UV follows the new size
    uv = list(face['uv'])
    index, compare = {0: (1, 3), 180: (3, 1), 90: (0, 2), 270: (2, 0)}[face.get('rotation', 0)]
    uv[index] += -amount if uv[index] > uv[compare] else amount
    face = dict(face)
    face['uv'] = uv
    return face


def trim(parts, amount):
    """TrimModel: cuts amount pixels off the top of each column (columns by from x/z)"""
    highest, tops = {}, {}
    for part in parts:
        xz = (part['from'][0], part['from'][2])
        height = part['to'][1]
        face = part.get('faces', {}).get('up')
        if xz in highest:
            is_highest = height > highest[xz]
            if is_highest:
                highest[xz] = height
            if face is not None and is_highest or xz not in tops:
                tops[xz] = face
        else:
            highest[xz] = height
            if face is not None:
                tops[xz] = face
    out = []
    for part in parts:
        xz = (part['from'][0], part['from'][2])
        new_height = highest[xz] - amount
        if new_height == 0:
            new_height = 0.05
        old_height = part['to'][1]
        if new_height > old_height:
            out.append(part)
            continue
        to = [part['to'][0], new_height, part['to'][2]]
        if to[1] < part['from'][1]:
            continue
        zero = to[1] == part['from'][1]
        faces = {}
        for side, face in part.get('faces', {}).items():
            vertical = side in ('up', 'down')
            if not zero or vertical:
                faces[side] = face if vertical else trim_uv(face, old_height - new_height)
        if 'up' not in faces and tops.get(xz) is not None:
            faces['up'] = tops[xz]
        part = dict(part)
        part['to'] = to
        part['faces'] = faces
        out.append(part)
    return out


def color(parts, colors):
    """Kilnwork colored_block: per element index {"color": "AARRGGBB" or "RRGGBB", "luminosity": 0-15}"""
    for i, part in enumerate(parts):
        if i >= len(colors):
            break
        data = {}
        if 'color' in colors[i]:
            hex_color = colors[i]['color']
            value = int(hex_color, 16) | (0xFF000000 if len(hex_color) == 6 else 0)
            data['color'] = value - (1 << 32) if value >= 1 << 31 else value
        if colors[i].get('luminosity', -1) >= 0:
            data['block_light'] = colors[i]['luminosity']
        if data:
            part['neoforge_data'] = data
    return parts


count = 0
for path in sorted(ROOT.glob('*/models/**/*.json')):
    model = json.loads(path.read_text())
    loader = model.get('loader')
    if loader not in ('inspirations:trim', 'kilnwork:colored_block'):
        continue
    parts = elements(load(model['parent']))
    if loader == 'inspirations:trim':
        parts = trim(parts, model['trim'])
    else:
        parts = color(parts, model.get('colors', []))
    out = {k: v for k, v in model.items() if k not in ('loader', 'trim', 'colors')}
    out['elements'] = parts
    path.write_text(json.dumps(out, indent=2) + '\n')
    count += 1
print('static models written:', count)
