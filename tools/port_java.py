#!/usr/bin/env python3
"""Mechanical Forge 1.20.1 + Mantle 1.11 -> NeoForge 1.21.1 + Kilnwork rewrites for the Inspirations sources (idempotent).

    python3 -I tools/port_java.py src/main/java

Only renames whose meaning does not change. Everything else is ported by hand, see PORTLOG.md.
"""
import os
import re
import sys

# exact import renames (old -> new); None removes the import
IMPORTS = {
    'net.minecraftforge.fml.common.Mod.EventBusSubscriber': 'net.neoforged.fml.common.EventBusSubscriber',
    'net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus': 'net.neoforged.fml.common.EventBusSubscriber.Bus',
    'net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext': None,
    'net.minecraftforge.fml.DistExecutor': None,
    'net.minecraftforge.common.MinecraftForge': 'net.neoforged.neoforge.common.NeoForge',
    'net.minecraftforge.common.ForgeConfigSpec': 'net.neoforged.neoforge.common.ModConfigSpec',
    'net.minecraftforge.common.ForgeConfigSpec.BooleanValue': 'net.neoforged.neoforge.common.ModConfigSpec.BooleanValue',
    'net.minecraftforge.common.ForgeConfigSpec.Builder': 'net.neoforged.neoforge.common.ModConfigSpec.Builder',
    'net.minecraftforge.common.ForgeConfigSpec.ConfigValue': 'net.neoforged.neoforge.common.ModConfigSpec.ConfigValue',
    'net.minecraftforge.common.ForgeConfigSpec.IntValue': 'net.neoforged.neoforge.common.ModConfigSpec.IntValue',
    'net.minecraftforge.common.ForgeConfigSpec.DoubleValue': 'net.neoforged.neoforge.common.ModConfigSpec.DoubleValue',
    'net.minecraftforge.common.ToolAction': 'net.neoforged.neoforge.common.ItemAbility',
    'net.minecraftforge.common.ToolActions': 'net.neoforged.neoforge.common.ItemAbilities',
    'net.minecraftforge.common.ForgeMod': 'net.neoforged.neoforge.common.NeoForgeMod',
    'net.minecraftforge.common.ForgeHooks': 'net.neoforged.neoforge.common.CommonHooks',
    'net.minecraftforge.client.ForgeHooksClient': 'net.neoforged.neoforge.client.ClientHooks',
    'net.minecraftforge.event.ForgeEventFactory': 'net.neoforged.neoforge.event.EventHooks',
    'net.minecraftforge.common.capabilities.ForgeCapabilities': 'net.neoforged.neoforge.capabilities.Capabilities',
    'net.minecraftforge.common.extensions.IForgeMenuType': 'net.neoforged.neoforge.common.extensions.IMenuTypeExtension',
    'net.minecraftforge.registries.ForgeRegistries': 'net.minecraft.core.registries.BuiltInRegistries',
    'net.minecraftforge.registries.RegistryObject': 'net.neoforged.neoforge.registries.DeferredHolder',
    'net.minecraftforge.common.util.LazyOptional': None,
    'net.minecraftforge.fluids.ForgeFlowingFluid': 'net.neoforged.neoforge.fluids.BaseFlowingFluid',
    'net.minecraftforge.common.capabilities.Capability': None,
    # Mantle -> Kilnwork
    'slimeknights.mantle.block.InventoryBlock': 'dev.vladeezyy.kilnwork.block.InventoryBlock',
    'slimeknights.mantle.block.entity.InventoryBlockEntity': 'dev.vladeezyy.kilnwork.block.InventoryBlockEntity',
    'slimeknights.mantle.block.entity.NameableBlockEntity': 'dev.vladeezyy.kilnwork.block.NameableBlockEntity',
    'slimeknights.mantle.block.entity.MantleBlockEntity': 'dev.vladeezyy.kilnwork.block.SyncedBlockEntity',
    'slimeknights.mantle.block.RetexturedBlock': 'dev.vladeezyy.kilnwork.retexture.RetexturedBlock',
    'slimeknights.mantle.block.entity.IRetexturedBlockEntity': 'dev.vladeezyy.kilnwork.retexture.IRetexturedBlockEntity',
    'slimeknights.mantle.block.entity.DefaultRetexturedBlockEntity': 'dev.vladeezyy.kilnwork.retexture.RetexturedBlockEntity',
    'slimeknights.mantle.util.RetexturedHelper': 'dev.vladeezyy.kilnwork.retexture.RetexturedHelper',
    'slimeknights.mantle.inventory.BaseContainerMenu': 'dev.vladeezyy.kilnwork.inventory.BaseContainerMenu',
    'slimeknights.mantle.inventory.SmartItemHandlerSlot': 'dev.vladeezyy.kilnwork.inventory.SmartItemHandlerSlot',
    'slimeknights.mantle.registration.object.EnumObject': 'knightminer.inspirations.common.registration.EnumObject',
    'slimeknights.mantle.registration.adapter.RegistryAdapter': 'knightminer.inspirations.common.registration.RegistryAdapter',
    'slimeknights.mantle.registration.adapter.BlockRegistryAdapter': 'knightminer.inspirations.common.registration.BlockRegistryAdapter',
    'slimeknights.mantle.registration.adapter.ItemRegistryAdapter': 'knightminer.inspirations.common.registration.ItemRegistryAdapter',
    'slimeknights.mantle.registration.adapter.BlockEntityTypeRegistryAdapter': 'knightminer.inspirations.common.registration.BlockEntityTypeRegistryAdapter',
    'slimeknights.mantle.registration.adapter.ContainerTypeRegistryAdapter': 'knightminer.inspirations.common.registration.ContainerTypeRegistryAdapter',
    'slimeknights.mantle.registration.adapter.FluidRegistryAdapter': 'knightminer.inspirations.common.registration.FluidRegistryAdapter',
    'slimeknights.mantle.registration.adapter.EntityTypeRegistryAdapter': 'knightminer.inspirations.common.registration.EntityTypeRegistryAdapter',
    'slimeknights.mantle.registration.FluidBuilder': 'knightminer.inspirations.common.registration.FluidBuilder',
    'slimeknights.mantle.client.model.util.DynamicBakedWrapper': 'dev.vladeezyy.kilnwork.client.model.DynamicBakedWrapper',
    'slimeknights.mantle.client.model.util.SimpleBlockModel': 'dev.vladeezyy.kilnwork.client.model.SimpleBlockModel',
    'slimeknights.mantle.client.model.util.ModelHelper': 'dev.vladeezyy.kilnwork.client.model.ModelHelpers',
    'slimeknights.mantle.client.model.RetexturedModel': 'dev.vladeezyy.kilnwork.client.model.RetexturedModel',
    'slimeknights.mantle.client.render.RenderItem': 'dev.vladeezyy.kilnwork.client.render.RenderItem',
    'slimeknights.mantle.util.BlockEntityHelper': 'dev.vladeezyy.kilnwork.util.BlockEntityHelper',
}

# package prefixes (applied when no exact rename matched)
PREFIXES = [
    ('net.minecraftforge.fml.', 'net.neoforged.fml.'),
    ('net.minecraftforge.eventbus.api.', 'net.neoforged.bus.api.'),
    ('net.minecraftforge.api.distmarker.', 'net.neoforged.api.distmarker.'),
    ('net.minecraftforge.', 'net.neoforged.neoforge.'),
]

SYMBOLS = [
    (r'\bMinecraftForge\.EVENT_BUS\b', 'NeoForge.EVENT_BUS'),
    (r'\bForgeConfigSpec\b', 'ModConfigSpec'),
    (r'\bToolActions\b', 'ItemAbilities'),
    (r'\bToolAction\b', 'ItemAbility'),
    (r'\bForgeEventFactory\b', 'EventHooks'),
    (r'\bForgeHooksClient\b', 'ClientHooks'),
    (r'\bForgeHooks\b', 'CommonHooks'),
    (r'\bForgeCapabilities\b', 'Capabilities'),
    (r'\bForgeMod\b', 'NeoForgeMod'),
    (r'\bIForgeMenuType\b', 'IMenuTypeExtension'),
    (r'@Mod\.EventBusSubscriber\b', '@EventBusSubscriber'),
    (r'\bMod\.EventBusSubscriber\.Bus\b', 'EventBusSubscriber.Bus'),
    (r'\bnew ResourceLocation\(([^(),]+),\s*', r'ResourceLocation.fromNamespaceAndPath(\1, '),
    (r'\bnew ResourceLocation\(', 'ResourceLocation.parse('),
    (r'\bMantleBlockEntity\b', 'SyncedBlockEntity'),
    (r'\bDefaultRetexturedBlockEntity\b', 'RetexturedBlockEntity'),
    (r'\bModelHelper\.', 'ModelHelpers.'),
    (r'\.setSecondsOnFire\(', '.igniteForSeconds('),
    (r'\bForgeFlowingFluid\b', 'BaseFlowingFluid'),
]

REG_NAMES = {
    'ITEMS': 'ITEM', 'BLOCKS': 'BLOCK', 'ENTITY_TYPES': 'ENTITY_TYPE', 'BLOCK_ENTITY_TYPES': 'BLOCK_ENTITY_TYPE',
    'ENCHANTMENTS': 'ENCHANTMENT', 'RECIPE_SERIALIZERS': 'RECIPE_SERIALIZER', 'RECIPE_TYPES': 'RECIPE_TYPE',
    'MENU_TYPES': 'MENU', 'SOUND_EVENTS': 'SOUND_EVENT', 'PARTICLE_TYPES': 'PARTICLE_TYPE', 'MOB_EFFECTS': 'MOB_EFFECT',
    'POTIONS': 'POTION', 'ATTRIBUTES': 'ATTRIBUTE', 'FLUIDS': 'FLUID',
}


def rewrite(src):
    out = src

    def imp(m):
        static, name = m.group(1) or '', m.group(2)
        if name in IMPORTS:
            new = IMPORTS[name]
            return '' if new is None else 'import %s%s;' % (static, new)
        for old, new in PREFIXES:
            if name.startswith(old):
                return 'import %s%s;' % (static, new + name[len(old):])
        return m.group(0)
    out = re.sub(r'import (static )?([A-Za-z0-9_.]+);', imp, out)
    for a, b in SYMBOLS:
        out = re.sub(a, b, out)

    def keys(m):
        n = REG_NAMES.get(m.group(1))
        return 'Registries.%s' % n if n else m.group(0)
    out = re.sub(r'\bForgeRegistries\.Keys\.([A-Z_]+)', keys, out)

    def inst(m):
        n = REG_NAMES.get(m.group(1))
        return 'BuiltInRegistries.%s' % n if n else m.group(0)
    out = re.sub(r'\bForgeRegistries\.([A-Z_]+)\b', inst, out)
    needs = []
    if re.search(r'(?<![A-Za-z])Registries\.', out) and 'import net.minecraft.core.registries.Registries;' not in out:
        needs.append('net.minecraft.core.registries.Registries')
    if 'NeoForge.EVENT_BUS' in out and 'import net.neoforged.neoforge.common.NeoForge;' not in out:
        needs.append('net.neoforged.neoforge.common.NeoForge')
    if '@EventBusSubscriber' in out and 'import net.neoforged.fml.common.EventBusSubscriber;' not in out:
        needs.append('net.neoforged.fml.common.EventBusSubscriber')
    if needs:
        m = re.search(r'^package [^;]+;\n', out, re.M)
        ins = ''.join('import %s;\n' % n for n in needs)
        out = out[:m.end()] + '\n' + ins + out[m.end():]
    seen, lines = set(), []
    for line in out.split('\n'):
        if line.startswith('import '):
            if line in seen:
                continue
            seen.add(line)
        lines.append(line)
    return '\n'.join(lines)


def main(root):
    changed = 0
    for d, _, files in os.walk(root):
        for f in files:
            if not f.endswith('.java'):
                continue
            p = os.path.join(d, f)
            with open(p, encoding='utf-8') as fh:
                src = fh.read()
            new = rewrite(src)
            if new != src:
                with open(p + '.tmp', 'w', encoding='utf-8') as fh:
                    fh.write(new)
                os.replace(p + '.tmp', p)
                changed += 1
    print('changed', changed, 'files')


if __name__ == '__main__':
    main(sys.argv[1])
