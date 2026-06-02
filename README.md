# ViScriptRecipe

ViScriptRecipe 是一个面向 Minecraft 1.21.1 / NeoForge 的可视化配方编辑与覆盖模组。它可以通过 LDLib2/ViScriptLib 的编辑器创建 `.recipe` 文件，并在游戏中把这些条目应用为新增、替换或移除配方。

玩家使用教程见：[docs/wiki.md](docs/wiki.md)。

联动配方只会在对应模组已安装时注册到编辑器中。下面的类型 ID 以代码中已注册的 `RecipeEditorType` 为准，可作为 `.recipe` 条目的 `type` 对照。

## 基本信息

- Minecraft：`1.21.1`
- Mod Loader：NeoForge `21+`
- 必需依赖：LDLib2
- 配方文件目录：LDLib2 资产目录下的 `viscript_recipe/recipes/*.recipe`
- 打开编辑器：`/viscript_recipe editor [file]`，仅单人世界可用
- 重载配方：`/viscript_recipe reload`
- 查看状态：`/viscript_recipe status`

## 原版配方

| 工作站 | 支持的配方类型 |
| --- | --- |
| 工作台 | `minecraft:crafting_shaped`、`minecraft:crafting_shapeless` |
| 熔炉 | `minecraft:smelting` |
| 高炉 | `minecraft:blasting` |
| 烟熏炉 | `minecraft:smoking` |
| 营火 | `minecraft:campfire_cooking` |
| 切石机 | `minecraft:stonecutting` |
| 锻造台 | `minecraft:smithing_transform` |

## 联动配方

### Iron's Spells 'n Spellbooks (`irons_spellbooks`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 炼金锅 | `irons_spellbooks:alchemist_cauldron_fill`、`irons_spellbooks:alchemist_cauldron_empty`、`irons_spellbooks:alchemist_cauldron_brew` |
| 奥数铁砧 | `irons_spellbooks:arcane_anvil_transform` |

奥数铁砧配方通过菜单覆盖逻辑生效，不直接写入原版 `RecipeManager`。

### Ice and Fire CE (`iceandfire`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 龙钢锻炉 | `iceandfire:dragonforge` |

龙钢锻炉支持火龙、冰龙、雷龙三种龙息类型。

### Farmer's Delight (`farmersdelight`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 厨锅 | `farmersdelight:cooking` |
| 砧板 | `farmersdelight:cutting` |

### Create (`create`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 粉碎轮 | `create:crushing` |
| 石磨 | `create:milling` |
| 动力锯 | `create:cutting`、`create:block_cutting` |
| 动力冲压机 | `create:auto_packing`、`create:pressing`、`create:compacting` |
| 砂纸 | `create:sandpaper_polishing` |
| 鼓风机 | `create:blasting`、`create:smoking`、`create:splashing`、`create:haunting` |
| 注液器 | `create:filling` |
| 分液池 | `create:emptying` |
| 动力搅拌器 | `create:mixing` |
| 机械手 | `create:deploying` |
| 手动右键应用 | `create:item_application` |
| 动力合成器 | `create:mechanical_crafting` |
| 序列组装 | `create:sequenced_assembly` |

`create:block_cutting` 会根据多个输出派生多个配方 ID。序列组装步骤支持部署、冲压、切削和注液。

### Extended Crafting (`extendedcrafting`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 合成核心 | `extendedcrafting:combination` |
| 合成台 | `extendedcrafting:shaped_table`、`extendedcrafting:shapeless_table`、`extendedcrafting:ultimate_singularity` |
| 量子压缩机 | `extendedcrafting:compressor_recipe` |
| 末影合成器 | `extendedcrafting:shaped_ender_crafter`、`extendedcrafting:shapeless_ender_crafter` |
| 通量合成器 | `extendedcrafting:shaped_flux_crafter`、`extendedcrafting:shapeless_flux_crafter` |

合成台配方通过配方数据中的层级字段支持基础、高级、精英和终极合成台。

### Ars Nouveau (`ars_nouveau`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 附魔装置 | `ars_nouveau:enchanting_apparatus`、`ars_nouveau:armor_upgrade`、`ars_nouveau:enchantment` |
| 灌注室 | `ars_nouveau:imbuement` |
| 抄写台 | `ars_nouveau:glyph` |
| 粉碎 | `ars_nouveau:crush` |

### Kaleidoscope Cookery (`kaleidoscope_cookery`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 炒锅 | `kaleidoscope_cookery:pot` |
| 汤锅 | `kaleidoscope_cookery:stockpot` |
| 石磨 | `kaleidoscope_cookery:millstone` |
| 砧板 | `kaleidoscope_cookery:chopping_board` |
| 蒸笼 | `kaleidoscope_cookery:steamer` |
| 茶壶 | `kaleidoscope_cookery:teapot` |

### Avaritia (`avaritia`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 合成台 | `avaritia:shaped_table`、`avaritia:shapeless_table`、`avaritia:no_consume_catalyst_shaped` |
| 中子压缩机 | `avaritia:compressor` |
| 终极锻造台 | `avaritia:extreme_smithing` |

Avaritia 合成台配方通过配方数据中的层级字段支持幽匿、下界、末地和终极合成台。

## 支持的操作

每个配方条目都支持以下操作：

- `add`：新增配方；如果 ID 已存在，会替换同名配方。
- `replace`：替换配方；如果 ID 不存在，会作为新增配方应用。
- `remove`：移除配方。

配置项 `recipes.showcase_only_viscript_recipes` 可以让 JEI 只展示 ViScriptRecipe 加载的配方。
