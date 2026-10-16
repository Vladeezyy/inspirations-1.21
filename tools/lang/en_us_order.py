"""Writes en_us.json: every key the mod shows (checked by the client scene), grouped by module.
Run from the repository root: python3 -I tools/lang/en_us_order.py"""
import json
import pathlib

LANG = pathlib.Path(__file__).resolve().parents[2] / 'src/main/resources/assets/inspirations/lang'
COLORS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple',
          'blue', 'brown', 'green', 'red', 'black']


def title(c):
    return ' '.join(w.capitalize() for w in c.split('_'))


def en():
    out = {}
    # building
    out.update({
        'block.inspirations.shelf': 'Shelf',
        'block.inspirations.rainbow_shelf': 'Rainbow Shelf',
        'block.inspirations.ancient_shelf': 'Ancient Shelf',
        'block.inspirations.tomes_shelf': 'Tomes Shelf',
        'gui.inspirations.shelf.name': 'Shelf',
        'item.inspirations.colored_book': 'Book',
        'block.inspirations.plain_mulch': 'Mulch',
        'block.inspirations.brown_mulch': 'Brown Mulch',
        'block.inspirations.red_mulch': 'Red Mulch',
        'block.inspirations.black_mulch': 'Black Mulch',
        'block.inspirations.blue_mulch': 'Blue Mulch',
        'block.inspirations.rope': 'Rope',
        'block.inspirations.vine': 'Vine Rope',
        'block.inspirations.glass_door': 'Glass Door',
        'block.inspirations.glass_trapdoor': 'Glass Trapdoor',
        'block.inspirations.rock_path': 'Cobble Path',
        'block.inspirations.tile_path': 'Path Tiles',
        'block.inspirations.round_path': 'Round Path',
        'block.inspirations.brick_path': 'Bricks Path',
        'block.inspirations.rose': 'Rose',
        'block.inspirations.syringa': 'Syringa',
        'block.inspirations.paeonia': 'Paeonia',
        'block.inspirations.cyan_flower': 'Cyan Flower',
        'block.inspirations.potted_rose': 'Potted Rose',
        'block.inspirations.potted_syringa': 'Potted Syringa',
        'block.inspirations.potted_paeonia': 'Potted Paeonia',
        'block.inspirations.potted_cyan': 'Potted Cyan Flower',
        'block.inspirations.white_enlightened_bush': 'White Enlightened Bush',
        'block.inspirations.red_enlightened_bush': 'Red Enlightened Bush',
        'block.inspirations.green_enlightened_bush': 'Green Enlightened Bush',
        'block.inspirations.blue_enlightened_bush': 'Blue Enlightened Bush',
    })
    # utility
    out.update({
        'block.inspirations.torch_lever': 'Torch Lever',
        'block.inspirations.soul_torch_lever': 'Soul Torch Lever',
        'item.inspirations.redstone_book': 'Trapped Book',
        'block.inspirations.collector': 'Collector',
        'gui.inspirations.collector': 'Collector',
        'block.inspirations.pipe': 'Pipe',
        'gui.inspirations.pipe': 'Pipe',
    })
    for c in COLORS:
        out[f'block.inspirations.{c}_carpeted_trapdoor'] = f'{title(c)} Carpeted Trapdoor'
    # tools
    out.update({
        'block.inspirations.redstone_charge': 'Redstone Charge',
        'item.inspirations.redstone_charger': 'Redstone Charger',
        'item.inspirations.charged_arrow': 'Charged Arrow',
        'item.inspirations.lock': 'Blank Lock',
        'item.inspirations.key': 'Blank Key',
        'inspirations.lock.success': 'Container locked',
        'inspirations.lock.fail.locked': 'This container is already locked',
        'inspirations.lock.fail.blank': 'The lock is blank, it must be named in an anvil',
        'inspirations.unlock.success': 'Container unlocked',
        'inspirations.unlock.fail.no_match': 'This key does not fit the lock',
        'inspirations.unlock.fail.unlocked': 'The container is not locked',
        'item.inspirations.north_compass': 'North Compass',
        'item.inspirations.barometer': 'Barometer',
        'item.inspirations.photometer': 'Photometer',
        'item.inspirations.dimension_compass': 'Dimension Compass',
        'item.inspirations.lodestone_dimension_compass': 'Lodestone Dimension Compass',
    })
    # tweaks
    out.update({
        'block.inspirations.cactus': 'Cactus',
        'block.inspirations.sugar_cane': 'Sugar Cane',
        'item.inspirations.cactus_seeds': 'Cactus Seeds',
        'item.inspirations.sugar_cane_seeds': 'Sugar Cane Seeds',
        'item.inspirations.heartbeet': 'Heartbeet',
        'pack.inspirations.config.name': 'Inspirations Config',
        'pack.inspirations.config.description': 'Internal pack to handle resource replacements.',
    })
    # cauldrons
    out.update({
        'item.inspirations.splash_bottle': 'Splash Potion Bottle',
        'item.inspirations.lingering_bottle': 'Lingering Potion Bottle',
        'block.inspirations.potion_cauldron': 'Potion Cauldron',
        'item.inspirations.mixed_dyed_bottle': 'Ink Bottle',
    })
    for c in COLORS:
        out[f'item.inspirations.{c}_dyed_bottle'] = f'{title(c)} Ink Bottle'
    out['block.inspirations.dye_cauldron'] = 'Ink Cauldron'
    for fluid, name in [('beetroot_soup', 'Beetroot Soup'), ('mushroom_stew', 'Mushroom Stew'), ('potato_soup', 'Potato Soup'),
                        ('rabbit_stew', 'Rabbit Stew'), ('honey', 'Honey')]:
        if fluid == 'potato_soup':
            out['item.inspirations.potato_soup'] = name
        out[f'block.inspirations.{fluid}_fluid'] = name
        out[f'fluid.inspirations.{fluid}'] = name
        out[f'item.inspirations.{fluid}_bucket'] = f'{name} Bucket'
        out[f'block.inspirations.{fluid}_cauldron'] = f'{name} Cauldron'
    out.update({
        'block.inspirations.suspicious_stew_cauldron': 'Suspicious Stew Cauldron',
        'block.inspirations.milk_cauldron': 'Milk Cauldron',
        'item.inspirations.milk_bottle': 'Milk Bottle',
        'death.attack.inspirations.boiling': '%s was boiled alive',
    })
    return out


if __name__ == '__main__':
    data = en()
    (LANG / 'en_us.json').write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(len(data), 'keys')
