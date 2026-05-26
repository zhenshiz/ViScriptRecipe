# ViScriptRecipe 可视化配方编辑使用说明

## 结论

当前项目已经可以实现一部分 KubeJS 和数据包常见的配方修改能力：通过可视化编辑器创建 `.recipe` 文件，然后用 ViScriptRecipe 自己的轻量重载指令对最终配方表执行新增、替换和删除。

它不是生成原版数据包 JSON，也不是 KubeJS 脚本事件。它的工作方式是：等数据包和 KubeJS 的配方 JSON 先完成加载与解析后，ViScriptRecipe 再读取自己的 `.recipe` 文件，把条目应用到 `RecipeManager` 中。

因此它适合做：

- 按配方 ID 新增工作台有序配方。
- 按配方 ID 新增工作台无序配方。
- 按配方 ID 替换已有配方。
- 按配方 ID 删除已有配方。
- 使用物品或物品标签作为原料。
- 编辑结果物品和结果数量。

暂时不适合做：

- 像 KubeJS 那样按输出、输入、模组、类型等条件批量筛选删除。
- 写任意 JS 逻辑、阶段系统、动态回调、特殊原料动作。
- 输出原版 datapack JSON 文件。
- 编辑熔炉、切石机、锻造等非工作台配方。
- 在 Minecraft 1.21.1 上实际应用 `minecraft:crafting_transmute`、`minecraft:crafting_dye`、`minecraft:crafting_imbue`。这些类型目前能保存，但运行时会跳过并记录错误。

## 运行时加载顺序

`.recipe` 文件会从下面的目录读取：

```text
<游戏目录>/ldlib2/assets/viscript_recipe/recipes
```

开发环境通常对应：

```text
run/ldlib2/assets/viscript_recipe/recipes
```

专用服务器则是：

```text
<server root>/ldlib2/assets/viscript_recipe/recipes
```

加载时会递归扫描所有以 `.recipe` 结尾的文件，并按相对路径排序。多个文件或多个条目修改同一个配方 ID 时，后应用的条目会覆盖前面的结果；删除条目会从当前配方表中移除该 ID。

完整资源重载时，ViScriptRecipe 会在 `RecipeManager.apply` 的尾部记录一份基础配方表，也就是数据包和 KubeJS 已经参与后、ViScriptRecipe 自己还没有打补丁前的结果。之后执行 `/viscript_recipe reload` 时，只会重新读取 `.recipe` 文件，并从这份基础配方表重新应用补丁，不会触发 Minecraft 的完整 `/reload`。

最终顺序仍可能受其他也在重载尾部修改 `RecipeManager` 的模组影响。

## 打开编辑器

当前编辑器只允许在单人或集成服务器中打开：

```mcfunction
/viscript_recipe editor
```

需要 2 级权限。专用服务器上不能直接打开 GUI；应在客户端或开发环境中编辑 `.recipe` 文件，再把文件复制到服务器的 `ldlib2/assets/viscript_recipe/recipes` 目录，然后执行 `/viscript_recipe reload`。

## 轻量重载和查询

重新读取并应用 `.recipe` 文件：

```mcfunction
/viscript_recipe reload
```

查看最近一次应用结果：

```mcfunction
/viscript_recipe status
```

`reload` 指令只处理 ViScriptRecipe 的 `.recipe` 文件，并会把新的配方列表同步给在线玩家；不会像 Minecraft 原版 `/reload` 那样重载所有数据包、标签、战利品表、函数和 KubeJS server scripts。

注意：如果数据包或 KubeJS 脚本本身发生了变化，仍然需要先执行一次完整 `/reload`，让 Minecraft 和 KubeJS 重新生成基础配方表。之后只改 `.recipe` 文件时，再用 `/viscript_recipe reload` 即可。

## 基本使用流程

1. 进入单人世界或集成服务器。
2. 执行 `/viscript_recipe editor`。
3. 在左侧配方列表中添加有序或无序工作台配方。
4. 设置配方 ID，例如 `minecraft:stick` 或 `my_pack:copper_plate`。
5. 选择操作：
   - `Add`：新增配方。如果 ID 已存在，会按替换处理并在日志中警告。
   - `Replace`：替换配方。如果 ID 不存在，会按新增处理并在日志中警告。
   - `Remove`：删除该 ID 的配方。
6. 在中间 3x3 工作台区域放入虚拟原料，在右侧结果槽设置输出。
7. 如需标签原料，选中对应原料槽，在右侧把原料类型改为 `Item Tag`，填写标签 ID，例如 `minecraft:planks`。
8. 点击右下角保存按钮，保存为 `.recipe` 文件。
9. 执行 `/viscript_recipe reload`，让修改应用到服务器配方表。

## 有序配方

有序配方对应 `minecraft:crafting_shaped`。编辑器会根据 3x3 网格自动裁剪外圈空行和空列，并把相同原料合并为同一个 pattern 符号。

可编辑内容：

- 配方 ID。
- 操作类型。
- 组名。
- 配方书分类。
- 是否显示解锁通知。
- 3x3 原料布局。
- 物品或标签原料。
- 结果物品和数量。

注意：如果某个槽使用标签或多值原料，工作台格子只显示第一个可预览物品。只改结果时不会破坏这类复杂原料；如果重新拖动物品到原料槽，槽内容会被改写成普通物品原料。

## 无序配方

无序配方对应 `minecraft:crafting_shapeless`。编辑器会把 3x3 区域中非空原料按槽顺序收集成列表。

可编辑内容：

- 配方 ID。
- 操作类型。
- 组名。
- 配方书分类。
- 原料列表。
- 物品或标签原料。
- 结果物品和数量。

注意：当前 1.21.1 运行时构造无序配方时没有使用 `show_notification` 字段，所以这个字段对无序配方没有实际效果。

## 删除配方

如果只是删除已有配方：

1. 新建任意条目。
2. 把配方 ID 设置为要删除的目标，例如 `minecraft:stick`。
3. 把操作改为 `Remove`。
4. 保存并执行 `/viscript_recipe reload`。

删除条目不需要填写原料或结果。

## 和 KubeJS / 数据包的关系

可以把 ViScriptRecipe 理解为一个“可视化的最终配方表补丁层”。

与数据包相似的地方：

- 修改可以通过 `/viscript_recipe reload` 单独重新应用。
- 可以新增或替换指定 ID 的配方。
- 可以使用物品 ID 和物品标签。

与数据包不同的地方：

- 文件是 LDLib2 NBT 格式的 `.recipe`，不是 `data/<namespace>/recipes/*.json`。
- 不支持原版 recipe JSON 的全部字段和全部 recipe serializer。
- KubeJS 不会把这些 `.recipe` 文件当作脚本或数据包 JSON 处理。

与 KubeJS 相似的地方：

- 可以删除配方。
- 可以替换已有配方。
- 可以在数据包和 KubeJS 配方加载后影响最终配方表。

与 KubeJS 不同的地方：

- 只能按明确 ID 操作，不能按条件批量过滤。
- 没有 JS 逻辑、循环、函数、事件回调。
- 不能直接使用 KubeJS 的自定义配方构造器和原料动作。
- 当前没有把可视化编辑结果导出为 KubeJS 脚本。

## 排错

如果保存后没有生效：

- 确认 `.recipe` 文件位于 `ldlib2/assets/viscript_recipe/recipes` 下。
- 确认文件名以 `.recipe` 结尾。
- 执行 `/viscript_recipe reload`。
- 如果数据包或 KubeJS 脚本也变了，先执行一次完整 `/reload`，再执行 `/viscript_recipe reload`。
- 检查日志是否有 `Reloaded ViScriptRecipe overrides`。
- 检查日志是否有 `Failed to apply recipe override`。
- 标签原料必须是已存在的物品标签，例如 `minecraft:planks`。未知标签会导致该条目应用失败。
- `Remove` 删除不存在的 ID 会记录警告，不会产生实际修改。

## 当前实现位置

- 编辑器命令：`src/main/java/com/viscript_recipe/command/ViScriptRecipeCommands.java`
- 编辑器项目类型和保存路径：`src/main/java/com/viscript_recipe/gui/editor/RecipeProjectType.java`
- 可视化工作台视图：`src/main/java/com/viscript_recipe/gui/editor/CraftingWorkbenchView.java`
- 编辑器数据同步控制：`src/main/java/com/viscript_recipe/gui/editor/RecipeEditorController.java`
- `.recipe` 文件加载：`src/main/java/com/viscript_recipe/recipe/RecipeFileLoader.java`
- 运行时应用新增、替换、删除：`src/main/java/com/viscript_recipe/recipe/RecipeOverrideManager.java`
- 配方数据模型：`src/main/java/com/viscript_recipe/data/recipe`
