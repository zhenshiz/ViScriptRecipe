# ViScriptRecipe

ViScriptRecipe 是一个面向 Minecraft 1.21.1 / NeoForge 的可视化配方编辑与覆盖模组。它的目标很直接：把原本需要手写 JSON、写 KubeJS、反复重启或重载数据包的配方修改，变成可以在游戏内通过编辑器完成的可视化流程。

玩家使用教程见：https://doc.mafuyu.moe/wiki/ViScriptRecipe

## 功能总览

- 游戏内可视化编辑 `.recipe` 文件，支持创建、替换、删除配方。
- 按工作站分类展示配方类型，联动模组只有在已安装时才会出现在编辑器中。
- 支持物品输入、物品标签输入、工具动作输入、流体输入、Create 流体标签输入、概率输出、数量输出等常见配方参数。
- 左侧配方列表支持新增、删除、复制到下方，适合批量制作相似配方。
- 支持通过配方 ID 导入当前世界已经加载的配方，兼容时会自动切换到对应配方类型并填充参数。
- 支持 `add`、`replace`、`remove` 三种操作，可以把 `.recipe` 当作一个轻量配方覆盖包使用。
- `/viscript_recipe reload` 只重新读取本模组的 `.recipe` 文件并应用覆盖，不执行完整数据包 reload。
- `/viscript_recipe reload delta` 只向客户端发送实际变化的配方，并增量更新 JEI；普通重载会同步完整配方包和配方书，`reload full` 还会同步标签包。
- 提供 JEI 展示模式，可以只加载并展示 ViScriptRecipe 提供的配方，方便整合包作者检查当前配方包。
- 支持 17 个配方模组的专用编辑 UI 与导入器，包括 Iron's Spells、Ice and Fire、Farmer's Delight、Create、Extended Crafting、Ars Nouveau、Kaleidoscope Cookery、Avaritia、Fungal Infection: Spore、L_Ender's Cataclysm、Touhou Little Maid、Goety、Mystical Agriculture、Industrial Foregoing、Alloy Smelter、Mekanism 和 Confluence: Otherworld。

## 基本信息

- Minecraft：`1.21.1`
- Mod Loader：NeoForge `21+`
- Java：`21`
- 必需依赖：LDLib2
- 内置依赖：ViScriptLib 会随本模组打包
- 配方文件目录：`ldlib2/assets/viscript_recipe/recipes/*.recipe`
- 配置文件：`config/viscript_recipe_config.toml`

## 兼容模组清单

ViScriptRecipe 自带原版配方编辑器，并按模组是否安装动态注册下表中的联动类型。可选模组没有安装时，不会出现在编辑器分类、配方类型列表或导入器中；安装后无需额外开关即可启用对应支持。

### 必需与基础组件

| 组件 | Mod ID | 关系 | 说明 |
| --- | --- | --- | --- |
| Minecraft | `minecraft` | 必需 | 当前目标版本为 `1.21.1`。 |
| NeoForge | `neoforge` | 必需 | 当前开发与运行目标为 NeoForge `21.1.x`。 |
| LowDragLib2 | `ldlib2` | 必需 | 提供编辑器 UI、持久化和菜单基础设施。 |
| ViScriptLib | `viscript_lib` | 随模组提供 | ViScriptRecipe 会将 ViScriptLib 作为内置依赖打包。 |
| Just Enough Items | `jei` | 可选 | 提供 JEI 风格的工作区贴图、配方预览和增量同步目标；未安装时编辑器使用内置贴图。 |

### 配方联动模组

下表是代码中实际注册的全部可选 `RecipeCompatModules`。每个模组都包含对应的配方编辑类型；已实现导入器的模组还支持从当前世界已加载的原生配方创建编辑条目。

| 模组 | Mod ID | 编辑器/导入器 |
| --- | --- | --- |
| Iron's Spells 'n Spellbooks | `irons_spellbooks` | 支持 |
| Ice and Fire CE | `iceandfire` | 支持 |
| Farmer's Delight | `farmersdelight` | 支持 |
| Create | `create` | 支持 |
| Extended Crafting | `extendedcrafting` | 支持 |
| Ars Nouveau | `ars_nouveau` | 支持 |
| Kaleidoscope Cookery | `kaleidoscope_cookery` | 支持 |
| Re-Avaritia / Avaritia | `avaritia` | 支持 |
| Fungal Infection: Spore | `spore` | 支持 |
| L_Ender's Cataclysm | `cataclysm` | 支持 |
| Touhou Little Maid | `touhou_little_maid` | 支持 |
| Goety | `goety` | 支持 |
| Mystical Agriculture | `mysticalagriculture` | 支持 |
| Industrial Foregoing | `industrialforegoing` | 支持 |
| Alloy Smelter | `alloy_smelter` | 支持 |
| Mekanism | `mekanism` | 支持 |
| Confluence: Otherworld | `confluence` | 支持 |

这里的“支持”指本模组已经注册了对应的编辑器类别、配方数据模型和原生配方构造逻辑；“导入器”只对能够无损映射到当前编辑器数据模型的原生配方启用。某些模组的 JEI 动态展示页、Data Map 或运行时 synthetic 配方不是 `RecipeManager` 配方，因此不会被错误地列为可上传的 `.recipe` 类型。

## 指令

| 指令 | 作用 |
| --- | --- |
| `/viscript_recipe editor` | 打开配方编辑器。 |
| `/viscript_recipe editor <file>` | 打开或创建指定 `.recipe` 文件，文件名支持补全。 |
| `/viscript_recipe reload` | 重新读取并应用本模组 `.recipe` 配方文件。 |
| `/viscript_recipe reload delta` | 只同步新增、修改和删除的配方，并局部更新 JEI；不刷新原版配方书和标签。 |
| `/viscript_recipe reload full` | 在普通重载基础上额外同步标签包，帮助 JEI 重建依赖标签的配方显示。 |
| `/viscript_recipe status` | 查看上一次加载的文件数、条目数、成功/跳过/失败数量。 |

编辑器可以在单人世界、局域网世界和专用服务器中打开。多人服务器上的编辑器会读取服务端配方文件和注册表数据，并可将 `.recipe` 文件直接上传到服务器的 `ldlib2/assets/viscript_recipe/recipes` 目录；使用 `/viscript_recipe reload delta`、`/viscript_recipe reload` 或 `/viscript_recipe reload full` 应用修改。

## 配方文件与操作

每个 `.recipe` 文件可以包含多个配方条目。每个条目都有自己的启用状态、配方 ID、配方类型和操作模式。

| 操作 | 作用 |
| --- | --- |
| `add` | 新增配方；如果 ID 已存在，会替换同名配方。 |
| `replace` | 替换配方；如果 ID 不存在，会作为新增配方应用。 |
| `remove` | 移除配方。 |

配方 ID 使用标准 `namespace:path` 格式。编辑器中导入已加载配方时，也使用这个 ID 查找当前世界 RecipeManager 中的配方。

## 编辑器能力

- 左侧是配方列表，可以按工作站分类查看、选择、新增、删除和复制配方。
- 中间是可视化工作区，会根据配方类型显示对应工作台、输入槽、输出槽、流体槽和箭头。
- 右侧是属性面板，用来编辑配方 ID、操作模式、具体参数、数量、时间、热量、概率、能量消耗等。
- 支持直接点击槽位编辑物品或流体，也支持在属性面板中精细修改。
- 支持物品标签；Create 的流体输入支持流体标签，标签输入会在槽位中轮换预览匹配物品或流体。
- 支持 Farmer's Delight 砧板工具动作输入，例如斧头、刀具等工具能力。
- 支持配方导入：输入或搜索已加载配方 ID，若本模组兼容该类型，会自动生成可编辑条目。
- 支持复制到下方：基于当前配方克隆一个新条目，并自动生成 `_copy` 后缀 ID，适合做少量修改。

## 重载与 JEI

`/viscript_recipe reload` 会重新读取 `ldlib2/assets/viscript_recipe/recipes` 下的 `.recipe` 文件，然后把结果应用到当前 RecipeManager。它不会触发完整数据包 reload，因此不会额外重载其它模组的战利品表、标签生成器或数据包监听器。

安装 Create 时，重载会同时刷新 Create 的配方查找缓存，避免机器侧继续命中已经删除或替换前的旧配方。

`/viscript_recipe reload delta` 会比较本次与上一次由 ViScriptRecipe 管理的最终配方状态，只向在线玩家发送新增、修改和删除的配方。客户端会局部替换 RecipeManager 索引，并通过 JEI 公开运行时接口隐藏旧页面、加入新页面，不触发标准完整配方包所引发的原版配方书、搜索树和 JEI 全量重启。这个模式不会刷新原版配方书或标签，适合编辑期间频繁预览普通配方内容修改。

如果展示模式已开启、变化数量过多、配方无法安全编码，或者客户端发现配方版本不一致，增量模式会自动回退或请求一次普通完整配方同步。JEI 分类使用专用展示对象而不能安全局部替换时，只重建 JEI，不重新传输服务器全部配方。Create 自动酿造和 Iron's Spells 黑暗铁砧属于这种专用展示类型。

普通重载会向在线玩家发送新的配方包，并同步配方书。它不会同步标签包，因此卡顿更少，适合只改配方内容的开发流程。

`/viscript_recipe reload full` 会在普通重载基础上额外发送标签包，用来帮助 JEI 感知标签相关变化并刷新界面。这个模式会比普通重载更重，通常只在新增、删除或修改标签相关输入后需要使用。

普通重载与未发生回退的增量重载使用相同的加载统计，只通过提示开头的“已重载”或“已增量重载”区分同步模式。例如：

```text
已重载 ViScriptRecipe 配方：文件 1，条目 2，启用 2，应用 2，跳过 0，失败 0，当前配方 9839。
已增量重载 ViScriptRecipe 配方：文件 1，条目 2，启用 2，应用 2，跳过 0，失败 0，当前配方 9839。
```

| 统计项 | 含义 |
| --- | --- |
| 文件 | 本次成功读取并反序列化的 `.recipe` 文件数量。 |
| 条目 | 上述文件中包含的全部配方条目数量，包括未启用的条目。 |
| 启用 | 已开启、会参与本次应用流程的条目数量。 |
| 应用 | 已成功执行 `add`、`replace` 或 `remove` 操作的条目数量。 |
| 跳过 | 未启用，或因当前操作条件不满足而未应用的条目数量。 |
| 失败 | 因配方 ID、配方类型、参数或构建过程错误而应用失败的条目数量；详细原因会写入日志。 |
| 当前配方 | 应用完成后 RecipeManager 中的配方总数；通常包含原版、其它模组以及 ViScriptRecipe 管理的配方。展示模式开启时，只包含最终保留的展示配方。 |

这些字段描述的是 `.recipe` 文件的读取和应用结果，不是发送给客户端的增量变化数量。增量成功提示不再显示删除数、新增或更新数、内部配方版本以及“原版配方书未刷新”尾注，但其同步行为没有改变：`reload delta` 仍然不会刷新原版配方书或标签。增量模式触发完整同步回退时，会改为显示回退原因和当前配方总数。

配置项：

| 配置项 | 默认值 | 作用 |
| --- | --- | --- |
| `recipes.showcase_only_viscript_recipes` | `false` | 展示模式。开启后会清空基础配方，只加载 ViScriptRecipe 的 `.recipe` 配方，方便检查配方包。 |

## 支持的原版配方

| 工作站 | 支持的配方类型 |
| --- | --- |
| 工作台 | `minecraft:crafting_shaped`、`minecraft:crafting_shapeless` |
| 熔炉 | `minecraft:smelting` |
| 高炉 | `minecraft:blasting` |
| 烟熏炉 | `minecraft:smoking` |
| 营火 | `minecraft:campfire_cooking` |
| 切石机 | `minecraft:stonecutting` |
| 锻造台 | `minecraft:smithing_transform` |

原版有序、无序、切石和锻造转换配方支持解锁提示配置。烧炼类配方支持经验值和处理时间。

## 支持的联动配方

联动配方只会在对应模组已安装时注册到编辑器中。下面的类型 ID 以代码中已注册的 `RecipeEditorType` 为准，可作为 `.recipe` 条目的 `type` 对照。

### Iron's Spells 'n Spellbooks (`irons_spellbooks`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 炼金锅 | `irons_spellbooks:alchemist_cauldron_fill`、`irons_spellbooks:alchemist_cauldron_empty`、`irons_spellbooks:alchemist_cauldron_brew` |
| 奥术铁砧 | `irons_spellbooks:arcane_anvil_transform` |

炼金锅支持物品与流体输入、返回物品、副产物、输出流体和声音设置。奥术铁砧配方通过本模组的菜单覆盖逻辑生效，不直接写入原版 RecipeManager。

### Ice and Fire CE (`iceandfire`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 龙钢锻炉 | `iceandfire:dragonforge` |

龙钢锻炉支持火龙、冰龙、雷龙三种龙息类型，并可编辑输入、血液/材料、输出和锻造时间。

### Farmer's Delight (`farmersdelight`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 厨锅 | `farmersdelight:cooking` |
| 砧板 | `farmersdelight:cutting` |

厨锅支持多材料、容器、输出和烹饪参数。砧板支持输入材料、工具动作、多个输出、输出概率和声音设置。

### Create (`create`)

| 工作站或处理方式 | 支持的配方类型 |
| --- | --- |
| 粉碎轮 | `create:crushing` |
| 石磨 | `create:milling` |
| 动力锯 | `create:cutting`、`create:block_cutting` |
| 动力冲压机 | `create:auto_packing`、`create:pressing`、`create:compacting` |
| 砂纸 | `create:sandpaper_polishing` |
| 鼓风机 | `create:blasting`、`create:smoking`、`create:splashing`、`create:haunting` |
| 注液器 | `create:filling` |
| 分液池 | `create:emptying` |
| 动力搅拌器 | `create:mixing`、`create:automatic_shapeless`、`create:automatic_brewing` |
| 机械手 | `create:deploying` |
| 手动右键应用 | `create:item_application` |
| 动力合成器 | `create:mechanical_crafting` |
| 序列组装 | `create:sequenced_assembly` |

Create 处理配方支持物品输入、流体输入、物品输出、流体输出、处理时间、热量需求和保留手持物品等参数。带流体输入的 Create 配方可以使用具体流体或流体标签，当前包括 `create:filling`、`create:mixing`、`create:compacting`、`create:automatic_brewing`，以及序列组装中的注液步骤；`create:emptying` 的流体是输出，仍使用具体流体。动力搅拌、动力压缩和自动无序配方支持在单个物品槽中设置数量，保存时会展开为多个 Create `Ingredient`。`create:block_cutting` 会根据多个输出派生多个配方 ID。序列组装支持部署、冲压、切削和注液步骤。

### Mekanism / 通用机械 (`mekanism`)

| 工作站或处理方式 | 支持的配方类型 |
| --- | --- |
| 物品处理 | `mekanism:crushing`、`mekanism:enriching`、`mekanism:smelting`、`mekanism:combining`、`mekanism:sawing` |
| 化学品处理 | `mekanism:chemical_infusing`、`mekanism:activating`、`mekanism:centrifuging`、`mekanism:chemical_conversion`、`mekanism:oxidizing`、`mekanism:pigment_extracting`、`mekanism:pigment_mixing` |
| 流体和化学品处理 | `mekanism:separating`、`mekanism:washing`、`mekanism:evaporating`、`mekanism:condensentrating`、`mekanism:decondensentrating` |
| 物品 + 化学品机器 | `mekanism:crystallizing`、`mekanism:dissolution`、`mekanism:compressing`、`mekanism:purifying`、`mekanism:injecting`、`mekanism:nucleosynthesizing`、`mekanism:metallurgic_infusing`、`mekanism:painting` |
| 其他 | `mekanism:energy_conversion`、`mekanism:reaction` |

通用机械的工作区采用与 JEI 相同的“输入 → 箭头 → 输出”阅读方向；物品输入继续使用现有物品/物品标签编辑和 Shift+左键拖拽复制。流体、流体标签、化学品、化学品标签均使用注册表搜索补全；化学品候选显示本地化名称，ID 仍可作为搜索词。各类型会按原生 Codec 显示数量、每刻消耗、时长、能量和概率等实际参数；旋转式冷凝机的冷凝与反冷凝方向分别对应 `mekanism:condensentrating` 和 `mekanism:decondensentrating`。

导入器当前无损支持单一化学品与化学品标签。Mekanism 的复合、差集、交集化学品原料表达式不会被静默降级：导入时会明确拒绝，需手动重建或等待专用 UI 支持。JEI 中的营养液化器、锅炉和 SPS 是动态展示分类，不是普通 `RecipeManager` 配方，因此不会被伪装成可上传的配方条目。

### Mystical Agriculture / 神秘农业 (`mysticalagriculture`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 注魔祭坛 | `mysticalagriculture:infusion` |
| 觉醒祭坛 | `mysticalagriculture:awakening` |
| 附魔器 | `mysticalagriculture:enchanter` |
| 种子再处理器 | `mysticalagriculture:reprocessor` |
| 灵魂提取器 | `mysticalagriculture:soul_extraction` |
| 灵魂刷怪笼 | `mysticalagriculture:soulium_spawner` |

注魔祭坛支持中心输入、基座材料、结果和组件转移；觉醒祭坛额外支持四个觉醒精华槽。附魔器支持带数量的材料与附魔 ID 自动补全，其附魔书结果由附魔参数派生。种子再处理器支持输入和结果。灵魂提取器支持生物灵魂类型自动补全与灵魂数量，其灵魂罐为只读派生预览。灵魂刷怪笼支持带数量的输入，以及包含实体 ID 和权重的实体列表；实体 ID 可自动补全，JEI 槽位中的刷怪蛋同样是派生预览。

上述六类均提供专用 JEI 风格工作区和已加载配方导入。神秘农业 JEI 中的 Crux/生长基座信息页来自作物注册表，不是由 Recipe Codec 和 RecipeManager 管理的配方，因此不会作为可编辑配方类型注册。

### Industrial Foregoing / 工业先锋 (`industrialforegoing`)

| 工作站或处理方式 | 支持的配方类型 |
| --- | --- |
| 材料石工作厂粉碎 | `industrialforegoing:crusher` |
| 溶解室 | `industrialforegoing:dissolution_chamber` |
| 流体提取机 | `industrialforegoing:fluid_extractor` |
| 激光钻矿物 | `industrialforegoing:laser_drill_ore` |
| 激光钻流体 | `industrialforegoing:laser_drill_fluid` |
| 材料石工作厂生成 | `industrialforegoing:stonework_generate` |

粉碎配方支持输入与输出 Ingredient。溶解室支持最多八个物品输入、流体或流体标签输入、处理时间，以及可分别启用的物品和流体输出。流体提取机支持输入、输出方块及方块状态、破坏概率、输出流体和默认配方标记。激光钻矿物与流体配方支持催化剂、输出数量或流体量、实体 ID/实体标签条件，以及深度、权重、群系和维度黑白名单。材料石工作厂生成配方支持输出，以及水和岩浆的需求量与消耗量。

这六类均提供对应 JEI 布局的可视化工作区和已加载配方导入。流体参数支持具体流体或流体标签；方块、实体、实体标签、群系标签和维度条件均使用自动补全。导入器只接受原生 Codec 能无损表达的单一流体或流体标签原料，遇到其它自定义流体原料类型时会明确拒绝，而不会静默替换数据。

### Alloy Smelter / 合金冶炼炉 (`alloy_smelter`)

开发依赖使用 Alloy Smelter `1.2.1`（Minecraft `1.21.1` NeoForge）。该模组只有一个原生 `RecipeManager` 配方类型：

| 工作站 | 支持的配方类型 |
| --- | --- |
| 合金冶炼炉 | `alloy_smelter:smelting` |

JEI 会按照配方的 `requiredTier` 将同一配方类型分成一级、二级和三级三个分类；这不是三个不同的配方 Codec。编辑器保留 JEI 中最多五个有序材料槽、输出 `ItemStack`、材料独立数量、熔炼时间 `smeltingTime`、每 tick 燃料消耗 `fuelPerTick` 和所需熔炉等级 `requiredTier`。输入数量使用 Alloy Smelter 的 `Material.count` 字段保存，不会错误地把数量写进 Ingredient 的物品栈。

Alloy Smelter 的配方由原生 Serializer 构造，导入器只接受实际的 `SmeltingRecipe`，不会把 JEI 分类或多方块等级标签伪装成额外配方类型。

### Confluence: Otherworld / 汇流来世 (`confluence`)

编辑器覆盖汇流来世 JEI 中由 `RecipeManager` 管理的十五类配方：微光嬗变、天磨、祭坛、地狱熔炉、重型工作台、炼药桌、制箭台、烹饪锅、锯木机、固化机、困难模式砧、困难模式熔炉、织布机、染缸和水晶球。每类都使用汇流来世的官方中文工作站名称和对应 JEI 布局，并提供已加载配方导入。

数量原料会在独立槽位属性中保存原料和数量；重型工作台、锯木机、困难模式砧与织布机分别保留有序/无序模式，固化机保留原生有序模式并支持 4×4 以内的图案尺寸。天磨、重型工作台和水晶球支持 Codec 实际提供的生物群系、附近方块/流体、搜索半径、状态谓词和墓地条件；烹饪锅的容器、热源方块、状态属性和方块实体 SNBT 也可点击单独编辑。微光嬗变支持多结果列表、输入数量和官方游戏阶段枚举补全。

提炼机、叶绿提炼机、泰拉药剂展示和盔甲套装奖励来自 Data Map 或运行时 JEI synthetic 分类，不是普通 `RecipeManager` 配方，因此不会伪装成可上传的 `.recipe` 类型。

### Extended Crafting (`extendedcrafting`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 合成核心 | `extendedcrafting:combination` |
| 合成台 | `extendedcrafting:shaped_table`、`extendedcrafting:shapeless_table`、`extendedcrafting:ultimate_singularity` |
| 量子压缩机 | `extendedcrafting:compressor_recipe` |
| 末影合成器 | `extendedcrafting:shaped_ender_crafter`、`extendedcrafting:shapeless_ender_crafter` |
| 通量合成器 | `extendedcrafting:shaped_flux_crafter`、`extendedcrafting:shapeless_flux_crafter` |

Extended Crafting 合成台在编辑器里统一为一个工作台，通过配方数据中的层级和尺寸字段选择 `3x3`、`5x5`、`7x7`、`9x9`。旧的基础、高级、精英、终极类型 ID 会被兼容归一到当前合成台类型。

### Ars Nouveau (`ars_nouveau`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 附魔装置 | `ars_nouveau:enchanting_apparatus`、`ars_nouveau:armor_upgrade`、`ars_nouveau:enchantment` |
| 灌注室 | `ars_nouveau:imbuement` |
| 抄写台 | `ars_nouveau:glyph` |
| 粉碎 | `ars_nouveau:crush` |

附魔装置支持试剂、基座物品、魔源消耗和结果；灌注室支持输入、基座物品、魔源和结果；抄写台支持法术输入与经验消耗；粉碎支持多输出、概率和最大数量。

### Kaleidoscope Cookery (`kaleidoscope_cookery`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 炒锅 | `kaleidoscope_cookery:pot` |
| 汤锅 | `kaleidoscope_cookery:stockpot` |
| 石磨 | `kaleidoscope_cookery:millstone` |
| 砧板 | `kaleidoscope_cookery:chopping_board` |
| 蒸笼 | `kaleidoscope_cookery:steamer` |
| 茶壶 | `kaleidoscope_cookery:teapot` |

森罗物语配方支持各工作站专属参数，例如汤底、容器、翻炒次数、蒸制时间、茶汤流体、气泡颜色和模型 ID。

### Avaritia / Re-Avaritia (`avaritia`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 合成台 | `avaritia:shaped_table`、`avaritia:shapeless_table`、`avaritia:no_consume_catalyst_shaped`、`avaritia:infinity_catalyst`、`avaritia:eternal_singularity`、`avaritia:full_matter_cluster` |
| 中子压缩机 | `avaritia:compressor` |
| 终极锻造台 | `avaritia:extreme_smithing` |

Avaritia 合成台在编辑器里统一为一个工作台，通过配方数据中的层级和尺寸字段选择幽匿、下界、末地、终极合成台规格。旧的幽匿、下界、末地、终极类型 ID 会被兼容归一到当前合成台类型。无限催化剂、永恒奇点和完整物质团是模组自带的特殊无序合成类型，编辑器允许修改材料和固定结果数量；无限催化剂与完整物质团还支持配方分组。

### L_Ender's Cataclysm / 灾变 (`cataclysm`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 机械融合铁砧 | `cataclysm:weapon_fusion` |
| 紫水晶祭坛 | `cataclysm:amethyst_bless` |

机械融合铁砧支持基础装备、融合材料和结果；紫水晶祭坛支持祭品、祝福时间和结果。两类配方均提供专用工作区与导入器。

### Fungal Infection: Spore / 真菌感染：孢子 (`spore`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 孢子手术台 | `spore:surgery`、`spore:grafting` |

手术配方提供 `4x4` 的 16 个材料槽；嫁接配方提供 3 个材料槽。两者都使用孢子手术台专用编辑 UI，并支持结果编辑和从已加载配方导入。

### Touhou Little Maid / 车万女仆 (`touhou_little_maid`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 祭坛 | `touhou_little_maid:altar_recipe` |

东方女仆祭坛支持材料、灵力消耗、结果、显示翻译键和输出实体类型。输出实体使用自动补全选择；普通物品输出使用 `minecraft:item`，也可选择女仆、闪电等祭坛原生支持的实体类型。

### Goety / 诡厄巫法 (`goety`)

| 工作站 | 支持的配方类型 |
| --- | --- |
| 诅咒注入器 | `goety:cursed_infuser_recipes` |
| 黑暗祭坛 | `goety:ritual` |
| 死灵火盆 | `goety:brazier` |
| 瓦解聚晶 | `goety:pulverize` |
| 女巫坩埚 | `goety:brewing` |

黑暗祭坛支持激活物、基座材料、灵魂消耗、仪式行为、召唤、献祭、转化、定位结构、附魔和研究等可选条件。女巫坩埚支持药水效果自动补全，并可选择不限制实体、实体标签或单个实体；后两种模式会显示相应的自动补全输入框。定位结构在界面中显示熟悉的结构 ID，但保存时会映射为 Goety Codec 所需的结构标签。

## 导入已加载配方

编辑器支持输入配方 ID 导入当前世界已经加载的配方。导入时会按已注册的导入器判断配方是否兼容；兼容时自动生成对应类型的 `RecipeEntry`，不兼容时会显示错误提示。

当前导入器覆盖原版配方，以及已安装联动模组中的 Iron's Spells、Ice and Fire、Farmer's Delight、Create、Extended Crafting、Ars Nouveau、Kaleidoscope Cookery、Avaritia、Fungal Infection: Spore、L_Ender's Cataclysm、Touhou Little Maid、Goety、Mystical Agriculture、Industrial Foregoing、Alloy Smelter、Mekanism 和 Confluence: Otherworld 的上述已实现类型。导入优先使用对应模组的专用导入器，而不是按 JSON 字段猜测配方结构。

部分复杂自定义材料表达可能无法导入，例如导入器暂不认识的自定义 Ingredient 或 FluidIngredient。遇到这种情况时，仍然可以手动在编辑器中重新创建配方。

## 注意事项

- 配方编辑器主要面向整合包作者和服务器管理员；多人服务器中只有具备 `/viscript_recipe` 指令权限的玩家可以打开编辑器。
- 联动模组没有安装时，对应工作站和配方类型不会出现在编辑器中。
- `.recipe` 文件是本模组自己的配方覆盖文件，不是原版 JSON 数据包文件。
- 展示模式 `recipes.showcase_only_viscript_recipes` 会清空基础配方后再应用 `.recipe`，更适合调试和展示，不建议在不了解效果时直接用于正式整合包。
- 如果只改配方内容并主要通过 JEI 检查，优先使用 `/viscript_recipe reload delta`；需要同步原版配方书时使用 `/viscript_recipe reload`；新增、删除或修改标签后使用 `/viscript_recipe reload full`。

## 开发者文档

后续新增模组配方联动时，应沿用现有的数据模型、类型注册、原生配方工厂、导入器、槽位聚焦属性面板和 JEI 双贴图适配结构；兼容模组清单以 `src/main/java/com/viscript_recipe/compat/RecipeCompatModules.java` 和 `src/main/resources/META-INF/neoforge.mods.toml` 中的实际注册为准。
