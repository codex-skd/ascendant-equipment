# Changelog — Ascendant Equipment


## [1.2.1] - 2026-08-23

### Change

- **Gemas transparentes para items no poseídos**: las gemas del Gem Case que el jugador no posee ahora se renderizan parcialmente transparentes para distinguirlas de las gemas poseídas, mejorando la experiencia visual al abrir el cofre de gemas.

## [1.2.0] - 2026-08-22

### Feature

- **Migración completa del guidebook de Patchouli a Vellumli**: el guidebook "Shadows Chronicle" dependía por completo del mod real Patchouli, que ni siquiera era una dependencia declarada de este mod (solo "funcionaba" en modpacks de prueba porque tenían Patchouli instalado por otro motivo). Portados los 5 idiomas restantes (ja_jp, pt_br, tr_tr, uk_ua, zh_cn) al nuevo sistema Vellumli, eliminado por completo el contenido antiguo de Patchouli y corregida la receta para depender de Vellumli.
- **Traducción española del guidebook**: 126 ficheros de contenido más claves de nombre/subtítulo/bienvenida.
- **Entrega automática del guidebook**: cada jugador lo recibe en la primera conexión (persiste tras muerte/reconexión); si se pierde, puede fabricarse de nuevo (libro + lingote de oro).

### Fix

- **El guidebook no aparecía en la pestaña creativa ni en JEI**: el campo `creative_tab` del libro apuntaba a una pestaña vanilla inexistente (`tools_and_utilities`, que no existe en esta versión de Minecraft — la pestaña real es simplemente `tools`). Corregido, y además registrado explícitamente en la pestaña Adventure propia del mod para garantizar su aparición.

## [1.1.1] - 2026-08-21

### Fixed

- **Cofres de las torres se generaban vacíos**: las 4 variantes de estructura de torre (`tower_leaf`, `tower_main`, `tower_sand`, `tower_spruce`) tenían el tag `LootTable` del cofre apuntando a `apotheosis:chests/tome_tower`, residuo de cuando la estructura se exportó del Apotheosis original sin renombrar el namespace. Como este mod no depende de Apotheosis, la loot table nunca se resolvía y el servidor logueaba `does not exist or could not be loaded`, dejando el cofre vacío. Redirigido a la loot table propia del mod `ascendant_equipment:chests/tome_tower`, que ya existía pero ninguna estructura construida referenciaba. Solo afecta a torres generadas tras esta actualización; los mundos existentes con datos rotos permanecen así en sus chunks.

## [1.1.0] - 2026-08-20

### Change

- **Actualización de NeoForge**: actualizado de 26.2.0.45-beta a 26.2.0.57.
- **Configuración unificada**: el módulo de configuración real (`EquipmentConfig`, bosses/augmenting/spawners/curios/etc) ahora carga y persiste correctamente en `config/ascendant/equipment/ascendant_equipment.cfg`. Antes su carga nunca se invocaba, así que el fichero nunca se generaba y solo se usaban valores por defecto en memoria.
- **Curios → Regalia Slots API**: la condición de datos que habilita el slot "charm" ahora depende de nuestro propio mod `regalia_slots_api` (fork compatible con Curios) en vez de `curios`.

## [1.0.10] - 2026-08-18

### Change

- **Nombre de JAR con versión del cargador**: el artefacto ahora se compila como `ascendant_equipment-26.2-neoforge-26.2.0.45-beta-1.0.10.jar`.
- **Documentación del workflow**: actualizada `docs/WORKFLOW_ASCENDANT_EQUIPMENT_26-2.md` para reflejar la nueva rama de trabajo.

## [1.0.9] - 2026-08-18

### Fixed

- `bonus.ascendant_equipment:enchantment.desc` (y sus variantes `.global`/`.mustExist`), usado por `EnchantmentBonus` y `EnchantmentAffix`, no existía en ninguno de los dos ficheros lang — cualquier gema o afijo que otorgara o mejorara un encantamiento mostraba la clave de traducción cruda en vez de su descripción
- `misc.ascendant_equipment.{iron,diamond,netherite}` también faltaban: `OmneticBonus`/`OmneticAffix` construyen esta clave en runtime desde el campo `"name"` del JSON de datos de gema/afijo (p. ej. `gems/the_nether/molten_breach.json`, `affixes/breaker/effect/omnetic.json`), así que el nombre crudo del tier se colaba en la línea del tooltip de "efectividad contra todos los bloques"

## [1.0.8] - 2026-08-18

### Fixed

- **Gem Case / Ender Gem Case**: ambos bloques se registraban con `requiresCorrectToolForDrops()`, así que romperlos con la herramienta incorrecta (o a mano) eliminaba el bloque sin drop alguno — exactamente el comportamiento vanilla de los minerales minados sin pico. `GemCaseBlock.getDrops()` solo incrusta las gemas almacenadas en el item dropeado cuando los drops se generan realmente, así que cada gema dentro se perdía permanentemente sin dejar nada en el suelo. Eliminado el requisito de herramienta para igualar los cofres/barriles vanilla, que nunca condicionan sus drops a la herramienta

## [1.0.7] - 2026-08-17

### Fixed

- **Registro de compatibilidad con Curios**: añadida la llamada `CuriosCompat.register()` que faltaba en commonSetup para evitar el crash del servidor "Unknown registry key: ascendant_equipment:charm"

## [1.0.6] - 2026-08-14

### Fixed

- `button.ascendant_equipment.activate_tier` (el tooltip del botón "Activate" en el mapa de World Tier) perdió el placeholder `": %s"` del nombre de tier de la plantilla original de Apotheosis, así que mostraba un estático "Activate Tier" en vez de "Activate World Tier: Frontier". Detectado en una auditoría de seguimiento del mismo fichero lang tras el fix de la v1.0.5
- Añadidas las claves de nombre de tier que faltaban, `button.ascendant_equipment.{frontier,ascent,summit,pinnacle}` — solo existía `haven`, así que esos 4 tiers no tenían nada que sustituir en las plantillas `%s` corregidas

## [1.0.5] - 2026-08-14

### Fixed

- **Tooltip de bloqueo del mapa de World Tier**: `button.tier_locked`, `button.tier_advancement` e `info.criteria_{done,unfinished,unknown}` estaban traducidos como cadenas genéricas estáticas sin `%s`, así que el nombre del tier, el título del avance y el texto por criterio que pasaba `WorldTierSelectScreen#tierLocked` se descartaban silenciosamente — cada tier bloqueado mostraba el mismo genérico "Tier Locked" / "Incomplete x5" en vez de "Frontier (Locked)" / "Equip a Common Chestplate". Restauradas las plantillas originales de Apotheosis con `%s` + glifo de checkbox
- Añadidas las claves de nombre de tier que faltaban, `button.ascendant_equipment.{frontier,ascent,summit,pinnacle}` — solo existía `haven`, así que esos 4 tiers no tenían nada que sustituir en las plantillas `%s` corregidas

## [1.0.4] - 2026-08-13

### Fixed

- **Mapa de World Tier**: pasar el cursor por un icono de tier (Haven, Frontier, Ascent, Summit, Pinnacle) nunca mostraba su tooltip. `init()` recreaba todos los botones en cada llamada en vez de solo la primera vez, dejando widgets obsoletos/duplicados tras un resize; y la cola de tooltips de los botones de tier no era atendida por el pipeline de render dividido de la pantalla (`extractBackground`/`extractRenderState`/`extractContents`)

## [1.0.2] - 2026-08-12

### Fixed

- **Tooltips de modificadores de atributos (regresión, todos los items)**: `neoforge.modifier.plus`/`neoforge.modifier.take` fueron sobrescritos con una plantilla de un argumento `"+%d"`/`"-%d"`, pero el `IAttributeExtension#toComponent()` de NeoForge llama a estas claves con dos argumentos (valor, nombre del atributo) para construir cada línea de tooltip de modificador de atributo del juego. La sobrescritura rota descartaba silenciosamente el nombre del atributo de todas las líneas de estadísticas de items con afijos, dejando solo un icono y un valor entre corchetes. Restauradas las plantillas propias de NeoForge `"+%s %s"` / `"%s %s"`
- `LootRarity#toComponent` construía su clave de traducción con dos puntos en vez de un punto, nunca coincidía con ninguna entrada lang y colaba la clave cruda truncada (`_equipment:common: 60%`) en los tooltips de probabilidad de drop
- Añadidas las claves que faltaban `rarity.ascendant_equipment.*` (common/uncommon/rare/epic/mythic) y `purity.ascendant_equipment.*` (cracked/chipped/flawed/normal/flawless/perfect) — existían en código, nunca en lang
- Añadido `button.ascendant_equipment.haven`, ausente a diferencia de todas las demás claves de botón de world tier
- Añadidas las 5 etiquetas de tipo de daño que faltaban (fire/fall/explosion/projectile/lightning) usadas por las descripciones de afijos de reducción de daño; solo existían physical/magic, así que p. ej. un equipo de reducción de daño de caída mostraba la clave cruda `misc.ascendant_equipment.fall`
- Eliminado el `%s` sin sustituir de la etiqueta de dificultad de World Tier (nunca recibía argumento; la dificultad se muestra vía los iconos de espada)
- es_es: corregida la errata "Raridad" → "Rareza"

## [1.0.1] - 2026-08-11

### Fixed

- Las claves `translate` de los advancements (puertas de progresión + challenge: Haven, Frontier, Ascent, Summit, Pinnacle y sus gateways) apuntaban al namespace del mod origin en vez de `ascendant_equipment`, con fallback a texto crudo sin traducir
- Añadido el texto title/desc/criteria que faltaba para los 6 advancements de progresión y las 5 challenge gates — estas claves nunca se habían escrito, no solo mal nombradas
- Añadidas las claves de nombre/descripción de world tier que faltaban (`text.ascendant_equipment.world_tier.<tier>[.desc]`) y las 6 claves title/desc de etapas del tutorial de World Tier, que antes mostraban claves lang crudas en pantalla

### Project

- Reducidos los locales enviados a en_us y es_es; los otros 10 tampoco tenían traducidas las claves anteriores y quedan aplazados a una pasada de localización futura

## [1.0.0] - 2026-08-10

Primera versión estable.

### Fixed

- Tooltips de gemas: añadidas las 21 traducciones de nombres de gemas que faltaban, todas las etiquetas de categoría `gem_class.*` y `loot_category.*`, y todas las descripciones de bonus de gema `bonus.ascendant_equipment:*.desc` (durabilidad, flecha sangrienta, bloqueo de leech, todas las estadísticas, mageslayer, multi-atributo) — antes se mostraban como claves crudas sin traducir
- Potion Charm: el nombre del item ahora incluye el efecto de poción (`Charm of %s`), y se añadieron las líneas de tooltip que faltaban `.desc`, `.desc3`, `.enabled`, `.disabled`, `.curios_only`
- `misc.ascendant_equipment.right_click_to_socket` ahora lleva los placeholders de nombre de gema/item tanto en en_us como en es_es (el mensaje los descartaba silenciosamente)
- Eliminadas dos claves lang placeholder residuales (`gem_class.ascendant_equipment.`, `bonus.ascendant_equipment.`) que no coincidían con ninguna búsqueda real
- Terminología es_es: "Empotrar/Empotramiento" → "Acoplar/Acoplamiento" para coincidir con la traducción española establecida de Apotheosis
- Normalizadas las rutas de assets de texturas (`textures/blocks/` → `textures/block/`, `textures/items/` → `textures/item/`) y las referencias de modelos que apuntaban a ellas
- Eliminados artefactos de build residuales (ficheros `nul`, directorios `assets/`/`com/` sueltos en raíz) que se habían colado en el working tree

### Project

- Fases 16 (arte restante) y 17 (pasada de QA dedicada) del roadmap cerradas como fuera de alcance para esta release — sale con el arte placeholder actual y sin una fase de QA de paridad dedicada

## [0.0.0-beta.19] - 2026-08-08

### Fixed

- Redespliegue tras la verificación de los fixes de inicialización de codecs y registros

## [0.0.0-beta.18] - 2026-08-08

### Fixed

- **Crítico**: faltaban las llamadas `initCodecs()`, causando que todos los dispatchers de codec de registros dinámicos (LootRule, SpawnCondition, EntityModifier, GemBonus) estuvieran vacíos
- Las rarezas ahora cargan correctamente (5/5: common, uncommon, rare, epic, mythic)
- Las gemas ahora cargan correctamente (21/21 gemas en todas las dimensiones)
- Los afijos ahora cargan correctamente (93/93 con todas las categorías: melee, ranged, armor, breaker, shield, generic)
- Los invaders ahora cargan correctamente (23/23 entre overworld, nether y end)
- Elites y Augments ahora cargan y registran correctamente
- Añadido el registro de `ConfigPayload` que faltaba, previniendo el crash de sincronización de config

### Known Issues

- Registro `extra_gem_bonuses` vacío (sin ficheros de datos incluidos)
- `loot_category:charm` no reconocido (categoría deprecada del upstream)

## [0.0.0-beta.15] - 2026-08-08

### Fixed

- **Crítico**: los registros dinámicos (RarityRegistry, GemRegistry, AffixRegistry, etc.) nunca recibían datos de los datapacks porque faltaban las llamadas `registerToBus()` en `FMLCommonSetupEvent`
- Las extensiones de receta de JEI (Malice, Supremacy, Unnaming) ya no crashean con `NoSuchElementException` con listas de rarezas vacías
- Añadidas comprobaciones defensivas de lista vacía en las extensiones JEI como salvaguarda secundaria
- Registrados `EquipmentEvents` y `AscEqMobEvents` en `NeoForge.EVENT_BUS` (los handlers nunca estaban activos)
- Registrados todos los payloads de red (BossSpawn, RerollResult, RadialState, WorldTier, LinkItemToChat, GemCaseSelect)
- Añadido el procesador de anotaciones Mixin para generación de refmap en la salida de build

## [0.0.0-beta.14] - 2026-08-08

### Fixed

- **Crítico**: las texturas de items no cargaban debido a que faltaba `src/main/resources` en la configuración de build de Gradle
- Actualizado sourceSets.main.resources para incluir explícitamente las fuentes de assets manuales junto a las generadas
- Todas las texturas de items ahora renderizan correctamente (gem_dust, materials, sigils, runes, etc.)
- Procesamiento correcto del pipeline de assets para modelos, texturas y blockstates

## [0.0.0-beta.13] - 2026-08-07

### Fixed

- **Crítico**: la pestaña de modo creativo estaba vacía pese a tener items registrados
- Implementado `TabFillingRegistry` para poblar la pestaña Ascendant: Adventure con todos los items del mod
- Los 49 items de `AscEq.Items` aparecen ahora en modo creativo (materiales, gemas, sigils, mesas, etc.)
- Los items se muestran con sus texturas correctas (se acabaron las texturas magenta de textura ausente)

## [0.0.0-beta.12] - 2026-07-XX

### Added

- Port inicial de Apotheosis 26.1 a NeoForge 26.2
- Sistemas core de equipo y afijos
- Sistema de engaste de gemas
- Sistema de spawners
- Progresión de world tiers

---
