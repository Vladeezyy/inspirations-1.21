"""Writes ru_ru, uk_ua, de_de, es_es, pt_br and zh_cn from the key table of en_us_order.py.

Terms follow the vanilla translations of 1.21.1 (cauldron, bucket, soups and stews, splash and lingering potions,
lodestone compass, potted flowers). The original's translations are kept where they fit the current English text.
Run from the repository root: python3 -I tools/lang/gen_lang.py
"""
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from en_us_order import COLORS, LANG, en  # noqa: E402

FLUIDS = ['beetroot_soup', 'mushroom_stew', 'potato_soup', 'rabbit_stew', 'honey']

# per language: trapdoor with a carpet of the color, bottle of ink of the color
TRAPDOOR = {
    'ru_ru': ('Люк с {} ковром', ['белым', 'оранжевым', 'пурпурным', 'голубым', 'жёлтым', 'лаймовым', 'розовым', 'серым',
                                  'светло-серым', 'бирюзовым', 'фиолетовым', 'синим', 'коричневым', 'зелёным', 'красным', 'чёрным']),
    'uk_ua': ('Люк з {} килимом', ['білим', 'помаранчевим', 'пурпуровим', 'блакитним', 'жовтим', 'лаймовим', 'рожевим', 'сірим',
                                   'світло-сірим', 'бірюзовим', 'фіолетовим', 'синім', 'коричневим', 'зеленим', 'червоним', 'чорним']),
    'de_de': ('{} Teppichfalltür', ['Weiße', 'Orange', 'Magenta', 'Hellblaue', 'Gelbe', 'Hellgrüne', 'Rosa', 'Graue', 'Hellgraue',
                                    'Türkise', 'Violette', 'Blaue', 'Braune', 'Grüne', 'Rote', 'Schwarze']),
    'es_es': ('Trampilla con alfombra {}', ['blanca', 'naranja', 'magenta', 'azul claro', 'amarilla', 'lima', 'rosa', 'gris',
                                            'gris claro', 'cian', 'morada', 'azul', 'marrón', 'verde', 'roja', 'negra']),
    'pt_br': ('Alçapão com Tapete {}', ['Branco', 'Laranja', 'Magenta', 'Azul-Claro', 'Amarelo', 'Verde-Limão', 'Rosa', 'Cinza',
                                        'Cinza-Claro', 'Ciano', 'Roxo', 'Azul', 'Marrom', 'Verde', 'Vermelho', 'Preto']),
    'zh_cn': ('{}地毯活板门', ['白色', '橙色', '品红色', '淡蓝色', '黄色', '黄绿色', '粉红色', '灰色', '淡灰色', '青色', '紫色',
                              '蓝色', '棕色', '绿色', '红色', '黑色']),
}
BOTTLE = {
    'ru_ru': ('Пузырёк {} красителя', ['белого', 'оранжевого', 'пурпурного', 'голубого', 'жёлтого', 'лаймового', 'розового',
                                        'серого', 'светло-серого', 'бирюзового', 'фиолетового', 'синего', 'коричневого',
                                        'зелёного', 'красного', 'чёрного']),
    'uk_ua': ('Пляшка {} барвника', ['білого', 'помаранчевого', 'пурпурового', 'блакитного', 'жовтого', 'лаймового', 'рожевого',
                                      'сірого', 'світло-сірого', 'бірюзового', 'фіолетового', 'синього', 'коричневого',
                                      'зеленого', 'червоного', 'чорного']),
    'de_de': ('{} Tintenflasche', TRAPDOOR['de_de'][1]),
    'es_es': ('Frasco de tinta {}', TRAPDOOR['es_es'][1]),
    'pt_br': ('Frasco de Tinta {}', ['Branca', 'Laranja', 'Magenta', 'Azul-Clara', 'Amarela', 'Verde-Limão', 'Rosa', 'Cinza',
                                     'Cinza-Clara', 'Ciano', 'Roxa', 'Azul', 'Marrom', 'Verde', 'Vermelha', 'Preta']),
    'zh_cn': ('{}墨水瓶', TRAPDOOR['zh_cn'][1]),
}
# per fluid: (name, bucket, cauldron)
SOUPS = {
    'ru_ru': [('Свекольный суп', 'Ведро свекольного супа', 'Котёл со свекольным супом'),
              ('Тушёные грибы', 'Ведро тушёных грибов', 'Котёл с тушёными грибами'),
              ('Картофельный суп', 'Ведро картофельного супа', 'Котёл с картофельным супом'),
              ('Тушёный кролик', 'Ведро тушёного кролика', 'Котёл с тушёным кроликом'),
              ('Мёд', 'Ведро мёда', 'Котёл с мёдом')],
    'uk_ua': [('Борщ', 'Відро борщу', 'Казан із борщем'),
              ('Тушковані гриби', 'Відро тушкованих грибів', 'Казан із тушкованими грибами'),
              ('Картопляний суп', 'Відро картопляного супу', 'Казан із картопляним супом'),
              ('Тушкований кролик', 'Відро тушкованого кролика', 'Казан із тушкованим кроликом'),
              ('Мед', 'Відро меду', 'Казан із медом')],
    'de_de': [('Borschtsch', 'Borschtscheimer', 'Borschtschkessel'),
              ('Pilzsuppe', 'Pilzsuppeneimer', 'Pilzsuppenkessel'),
              ('Kartoffelsuppe', 'Kartoffelsuppeneimer', 'Kartoffelsuppenkessel'),
              ('Kaninchenragout', 'Kaninchenragouteimer', 'Kaninchenragoutkessel'),
              ('Honig', 'Honigeimer', 'Honigkessel')],
    'es_es': [('Estofado de remolacha', 'Cubo con estofado de remolacha', 'Caldero con estofado de remolacha'),
              ('Estofado de champiñones', 'Cubo con estofado de champiñones', 'Caldero con estofado de champiñones'),
              ('Sopa de patata', 'Cubo con sopa de patata', 'Caldero con sopa de patata'),
              ('Estofado de conejo', 'Cubo con estofado de conejo', 'Caldero con estofado de conejo'),
              ('Miel', 'Cubo con miel', 'Caldero con miel')],
    'pt_br': [('Sopa de Beterraba', 'Balde de Sopa de Beterraba', 'Caldeirão de Sopa de Beterraba'),
              ('Ensopado de Cogumelos', 'Balde de Ensopado de Cogumelos', 'Caldeirão de Ensopado de Cogumelos'),
              ('Sopa de Batata', 'Balde de Sopa de Batata', 'Caldeirão de Sopa de Batata'),
              ('Ensopado de Coelho', 'Balde de Ensopado de Coelho', 'Caldeirão de Ensopado de Coelho'),
              ('Mel', 'Balde de Mel', 'Caldeirão de Mel')],
    'zh_cn': [('甜菜汤', '甜菜汤桶', '甜菜汤炼药锅'),
              ('蘑菇煲', '蘑菇煲桶', '蘑菇煲炼药锅'),
              ('土豆汤', '土豆汤桶', '土豆汤炼药锅'),
              ('兔肉煲', '兔肉煲桶', '兔肉煲炼药锅'),
              ('蜂蜜', '蜂蜜桶', '蜂蜜炼药锅')],
}

# keys in the order of en_us_order.py; values per language
K = ['block.inspirations.shelf', 'block.inspirations.rainbow_shelf', 'block.inspirations.ancient_shelf', 'block.inspirations.tomes_shelf',
     'gui.inspirations.shelf.name', 'item.inspirations.colored_book',
     'block.inspirations.plain_mulch', 'block.inspirations.brown_mulch', 'block.inspirations.red_mulch', 'block.inspirations.black_mulch',
     'block.inspirations.blue_mulch', 'block.inspirations.rope', 'block.inspirations.vine', 'block.inspirations.glass_door',
     'block.inspirations.glass_trapdoor', 'block.inspirations.rock_path', 'block.inspirations.tile_path', 'block.inspirations.round_path',
     'block.inspirations.brick_path', 'block.inspirations.rose', 'block.inspirations.syringa', 'block.inspirations.paeonia',
     'block.inspirations.cyan_flower', 'block.inspirations.potted_rose', 'block.inspirations.potted_syringa',
     'block.inspirations.potted_paeonia', 'block.inspirations.potted_cyan', 'block.inspirations.white_enlightened_bush',
     'block.inspirations.red_enlightened_bush', 'block.inspirations.green_enlightened_bush', 'block.inspirations.blue_enlightened_bush',
     'block.inspirations.torch_lever', 'block.inspirations.soul_torch_lever', 'item.inspirations.redstone_book',
     'block.inspirations.collector', 'gui.inspirations.collector', 'block.inspirations.pipe', 'gui.inspirations.pipe',
     'block.inspirations.redstone_charge', 'item.inspirations.redstone_charger', 'item.inspirations.charged_arrow',
     'item.inspirations.lock', 'item.inspirations.key', 'inspirations.lock.success', 'inspirations.lock.fail.locked',
     'inspirations.lock.fail.blank', 'inspirations.unlock.success', 'inspirations.unlock.fail.no_match',
     'inspirations.unlock.fail.unlocked', 'item.inspirations.north_compass', 'item.inspirations.barometer',
     'item.inspirations.photometer', 'item.inspirations.dimension_compass', 'item.inspirations.lodestone_dimension_compass',
     'block.inspirations.cactus', 'block.inspirations.sugar_cane', 'item.inspirations.cactus_seeds', 'item.inspirations.sugar_cane_seeds',
     'item.inspirations.heartbeet', 'pack.inspirations.config.name', 'pack.inspirations.config.description',
     'item.inspirations.splash_bottle', 'item.inspirations.lingering_bottle', 'block.inspirations.potion_cauldron',
     'item.inspirations.mixed_dyed_bottle', 'block.inspirations.dye_cauldron', 'block.inspirations.suspicious_stew_cauldron',
     'block.inspirations.milk_cauldron', 'item.inspirations.milk_bottle', 'death.attack.inspirations.boiling']

V = {
    'ru_ru': ['Полка', 'Радужная полка', 'Древняя полка', 'Полка с фолиантами', 'Полка', 'Книга',
              'Мульча', 'Коричневая мульча', 'Красная мульча', 'Чёрная мульча', 'Синяя мульча', 'Верёвка', 'Верёвка из лиан',
              'Стеклянная дверь', 'Стеклянный люк', 'Тропинка из булыжника', 'Тропинка из плитки', 'Тропинка из круглых камней',
              'Тропинка из кирпича', 'Роза', 'Сирень', 'Пион', 'Голубой цветок', 'Роза в горшке', 'Сирень в горшке',
              'Пион в горшке', 'Голубой цветок в горшке', 'Белый освещённый куст', 'Красный освещённый куст',
              'Зелёный освещённый куст', 'Синий освещённый куст', 'Факел-рычаг', 'Факел-рычаг душ', 'Книга-ловушка',
              'Сборщик', 'Сборщик', 'Труба', 'Труба', 'Заряд красного камня', 'Зарядник красного камня', 'Заряженная стрела',
              'Чистый замок', 'Чистый ключ', 'Контейнер заперт', 'Этот контейнер уже заперт',
              'Замок чистый, сначала переименуйте его на наковальне', 'Контейнер открыт', 'Этот ключ не подходит к замку',
              'Контейнер не заперт', 'Северный компас', 'Барометр', 'Фотометр', 'Компас измерений',
              'Намагниченный компас измерений', 'Кактус', 'Сахарный тростник', 'Семена кактуса', 'Семена сахарного тростника',
              'Свёкла-сердце', 'Настройки Inspirations', 'Встроенный пакет для замены ресурсов.',
              'Пузырёк для взрывного зелья', 'Пузырёк для туманного зелья', 'Котёл с зельем', 'Пузырёк красителя',
              'Котёл с красителем', 'Котёл с загадочным рагу', 'Котёл с молоком', 'Бутылочка молока', '%s сварился заживо'],
    'uk_ua': ['Полиця', 'Райдужна полиця', 'Стародавня полиця', 'Полиця з фоліантами', 'Полиця', 'Книга',
              'Мульча', 'Коричнева мульча', 'Червона мульча', 'Чорна мульча', 'Синя мульча', 'Мотузка', 'Мотузка з лоз',
              'Скляні двері', 'Скляний люк', 'Стежка з кругляка', 'Стежка з плитки', 'Стежка з круглого каміння',
              'Цегляна стежка', 'Троянда', 'Бузок', 'Півонія', 'Блакитна квітка', 'Троянда у горщику', 'Бузок у горщику',
              'Півонія у горщику', 'Блакитна квітка у горщику', 'Білий освітлений кущ', 'Червоний освітлений кущ',
              'Зелений освітлений кущ', 'Синій освітлений кущ', 'Смолоскип-важіль', 'Смолоскип-важіль душ', 'Книга-пастка',
              'Збирач', 'Збирач', 'Труба', 'Труба', 'Заряд редстоуну', 'Зарядник редстоуну', 'Заряджена стріла',
              'Чистий замок', 'Чистий ключ', 'Контейнер замкнено', 'Цей контейнер уже замкнено',
              'Замок чистий, спершу дайте йому назву на ковадлі', 'Контейнер відімкнено', 'Цей ключ не підходить до замка',
              'Контейнер не замкнено', 'Північний компас', 'Барометр', 'Фотометр', 'Компас вимірів',
              'Намагнічений компас вимірів', 'Кактус', 'Цукрова тростина', 'Насіння кактуса', 'Насіння цукрової тростини',
              'Буряк-серце', 'Налаштування Inspirations', 'Вбудований пакет для заміни ресурсів.',
              'Пляшка для вибухового зілля', 'Пляшка для осідального зілля', 'Казан із зіллям', 'Пляшка барвника',
              'Казан із барвником', 'Казан із підозрілою юшкою', 'Казан із молоком', 'Пляшка молока', '%s зварився живцем'],
    'de_de': ['Regal', 'Regenbogenregal', 'Uraltes Regal', 'Foliantenregal', 'Regal', 'Buch',
              'Mulch', 'Brauner Mulch', 'Roter Mulch', 'Schwarzer Mulch', 'Blauer Mulch', 'Seil', 'Rankenseil',
              'Glastür', 'Glasfalltür', 'Pflasterpfad', 'Pfadkacheln', 'Rundpfad', 'Ziegelpfad', 'Rose', 'Flieder',
              'Pfingstrose', 'Türkise Blume', 'Eingetopfte Rose', 'Eingetopfter Flieder', 'Eingetopfte Pfingstrose',
              'Eingetopfte türkise Blume', 'Weißer erleuchteter Busch', 'Roter erleuchteter Busch', 'Grüner erleuchteter Busch',
              'Blauer erleuchteter Busch', 'Fackelhebel', 'Seelenfackelhebel', 'Redstone-Buch',
              'Sammler', 'Sammler', 'Röhre', 'Röhre', 'Redstone-Ladung', 'Redstone-Ladegerät', 'Aufgeladener Pfeil',
              'Blanko-Schloss', 'Blanko-Schlüssel', 'Behälter abgeschlossen', 'Dieser Behälter ist bereits abgeschlossen',
              'Dies ist ein Blanko-Schloss, es muss zuerst in einem Amboss umbenannt werden', 'Behälter aufgeschlossen',
              'Dieser Schlüssel passt nicht ins Schloss', 'Der Behälter ist nicht abgeschlossen', 'Nordkompass', 'Barometer',
              'Photometer', 'Dimensionskompass', 'Leitstein-Dimensionskompass', 'Kaktus', 'Zuckerrohr', 'Kaktussamen',
              'Zuckerrohrsamen', 'Herzbete', 'Inspirations-Konfiguration', 'Internes Paket zum Ersetzen von Ressourcen.',
              'Wurftrankflasche', 'Verweiltrankflasche', 'Trankkessel', 'Tintenflasche', 'Tintenkessel',
              'Kessel mit seltsamer Suppe', 'Milchkessel', 'Milchflasche', '%s wurde bei lebendigem Leib gekocht'],
    'es_es': ['Estante', 'Estante arcoíris', 'Estante antiguo', 'Estante de tomos', 'Estante', 'Libro',
              'Mantillo', 'Mantillo marrón', 'Mantillo rojo', 'Mantillo negro', 'Mantillo azul', 'Cuerda',
              'Cuerda de enredadera', 'Puerta de cristal', 'Trampilla de cristal', 'Camino de roca', 'Camino de baldosas',
              'Camino redondo', 'Camino de ladrillos', 'Rosa', 'Lila', 'Peonía', 'Flor cian', 'Maceta con rosa',
              'Maceta con lila', 'Maceta con peonía', 'Maceta con flor cian', 'Arbusto iluminado blanco',
              'Arbusto iluminado rojo', 'Arbusto iluminado verde', 'Arbusto iluminado azul', 'Palanca de antorcha',
              'Palanca de antorcha de almas', 'Libro trampa', 'Recolector', 'Recolector', 'Tubería', 'Tubería',
              'Carga de redstone', 'Cargador de redstone', 'Flecha cargada', 'Candado vacío', 'Llave vacía',
              'Contenedor bloqueado', 'Este contenedor ya está bloqueado',
              'El candado está vacío, primero hay que ponerle nombre en un yunque', 'Contenedor desbloqueado',
              'Esta llave no encaja en el candado', 'El contenedor no está bloqueado', 'Brújula del norte', 'Barómetro',
              'Fotómetro', 'Brújula dimensional', 'Brújula dimensional imantada', 'Cactus', 'Caña de azúcar',
              'Semillas de cactus', 'Semillas de caña de azúcar', 'Remolacha corazón', 'Configuración de Inspirations',
              'Paquete interno para reemplazar recursos.', 'Frasco para poción arrojadiza', 'Frasco para poción persistente',
              'Caldero con poción', 'Frasco de tinta', 'Caldero con tinta', 'Caldero con estofado sospechoso',
              'Caldero con leche', 'Frasco con leche', '%s murió hervido'],
    'pt_br': ['Prateleira', 'Prateleira Arco-Íris', 'Prateleira Antiga', 'Prateleira de Tomos', 'Prateleira', 'Livro',
              'Cobertura Morta', 'Cobertura Morta Marrom', 'Cobertura Morta Vermelha', 'Cobertura Morta Preta',
              'Cobertura Morta Azul', 'Corda', 'Corda de Trepadeira', 'Porta de Vidro', 'Alçapão de Vidro',
              'Caminho de Pedregulho', 'Caminho de Ladrilhos', 'Caminho Redondo', 'Caminho de Tijolos', 'Rosa', 'Lilás',
              'Peônia', 'Flor Ciano', 'Rosa Envasada', 'Lilás Envasado', 'Peônia Envasada', 'Flor Ciano Envasada',
              'Arbusto Iluminado Branco', 'Arbusto Iluminado Vermelho', 'Arbusto Iluminado Verde', 'Arbusto Iluminado Azul',
              'Alavanca de Tocha', 'Alavanca de Tocha das Almas', 'Livro Armadilha', 'Coletor', 'Coletor', 'Cano', 'Cano',
              'Carga de Redstone', 'Carregador de Redstone', 'Flecha Carregada', 'Cadeado em Branco', 'Chave em Branco',
              'Recipiente trancado', 'Este recipiente já está trancado',
              'O cadeado está em branco, ele deve ser nomeado em uma bigorna', 'Recipiente destrancado',
              'Esta chave não serve neste cadeado', 'O recipiente não está trancado', 'Bússola do Norte', 'Barômetro',
              'Fotômetro', 'Bússola Dimensional', 'Bússola Dimensional Magnetizada', 'Cacto', 'Cana-de-Açúcar',
              'Sementes de Cacto', 'Sementes de Cana-de-Açúcar', 'Beterraba-Coração', 'Configuração do Inspirations',
              'Pacote interno para substituir recursos.', 'Frasco de Poção Arremessável', 'Frasco de Poção Prolongada',
              'Caldeirão de Poção', 'Frasco de Tinta', 'Caldeirão de Tinta', 'Caldeirão de Ensopado Suspeito',
              'Caldeirão de Leite', 'Frasco de Leite', '%s foi fervido vivo'],
    'zh_cn': ['置物架', '彩虹置物架', '远古置物架', '典籍置物架', '置物架', '书',
              '有机覆盖物', '棕色有机覆盖物', '红色有机覆盖物', '黑色有机覆盖物', '蓝色有机覆盖物', '绳索', '藤蔓绳',
              '玻璃门', '玻璃活板门', '圆石路径', '石砖路径', '石头路径', '砖块路径', '玫瑰', '欧丁香', '牡丹', '青花',
              '玫瑰盆栽', '欧丁香盆栽', '牡丹盆栽', '青花盆栽', '白色闪亮灌木', '红色闪亮灌木', '绿色闪亮灌木', '蓝色闪亮灌木',
              '火把拉杆', '灵魂火把拉杆', '陷阱书', '采集器', '采集器', '管道', '管道', '红石充能', '红石充能器', '充能的箭',
              '空白锁', '空白钥匙', '容器已上锁', '此容器已被上锁了', '锁是空白的，必须在铁砧中命名', '容器已解锁',
              '钥匙不匹配此锁', '此容器未被上锁', '指北针', '气压计', '光度计', '维度指南针', '磁石维度指南针', '仙人掌',
              '甘蔗', '仙人掌种子', '甘蔗种子', '甜心菜', 'Inspirations 配置', '用于替换资源的内置资源包。', '喷溅型药水瓶',
              '滞留型药水瓶', '药水炼药锅', '墨水瓶', '墨水炼药锅', '谜之炖菜炼药锅', '牛奶炼药锅', '牛奶瓶', '%s被活活烫死'],
}


def build(lang):
    assert len(V[lang]) == len(K), (lang, len(V[lang]), len(K))
    table = dict(zip(K, V[lang]))
    form, colors = TRAPDOOR[lang]
    bottle, bottle_colors = BOTTLE[lang]
    for i, c in enumerate(COLORS):
        table[f'block.inspirations.{c}_carpeted_trapdoor'] = form.format(colors[i])
        table[f'item.inspirations.{c}_dyed_bottle'] = bottle.format(bottle_colors[i])
    for fluid, (name, bucket, cauldron) in zip(FLUIDS, SOUPS[lang]):
        if fluid == 'potato_soup':
            table['item.inspirations.potato_soup'] = name
        table[f'block.inspirations.{fluid}_fluid'] = name
        table[f'fluid.inspirations.{fluid}'] = name
        table[f'item.inspirations.{fluid}_bucket'] = bucket
        table[f'block.inspirations.{fluid}_cauldron'] = cauldron
    english = en()
    missing = [k for k in english if k not in table]
    extra = [k for k in table if k not in english]
    assert not missing and not extra, (lang, missing, extra)
    return {k: table[k] for k in english}


if __name__ == '__main__':
    for lang in V:
        data = build(lang)
        (LANG / f'{lang}.json').write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        print(lang, len(data))
