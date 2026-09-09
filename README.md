# ReallyFastMode

一个用于《杀戮尖塔》（Slay the Spire）的实验性 Mod。开启快速模式后，它会跳过战斗中的大部分纯视觉动画和等待时间，同时尽量保留原版结算流程、回调与游戏状态变化。

项目还提供了一组游戏内 Java API，可以绕过鼠标拖拽和 Hitbox 点击，以语义命令完成出牌、使用药水、选择卡牌和选择休息点操作。

## 功能

### 快速模式

- 跳过纯等待和纯展示 Action。
- 加速出牌、伤害、格挡、治疗、抽牌、洗牌、回合切换及战斗结束后的奖励衔接。
- 跳过卡牌飞行动画、伤害数字、粒子、攻击动作和屏幕震动。
- 跳过普通房间之间的淡入淡出。
- 保留结算相关的原版 `update()`、卡牌移动、遗物/能力回调和洗牌随机结果。
- 关闭快速模式时保持原版行为。

快速模式不会修改游戏的全局 tick，也不会自动跳过 Mod 自定义 Action 或 Effect。卡牌拖拽、目标选择和其他玩家输入流程仍由原版游戏处理。

### 战斗命令 API

当前提供以下游戏内 Java API：

```java
CombatApiResult playResult = CombatApi.playCard(cardId, targetId);
CombatApiResult potionResult = CombatApi.usePotion(potionSlot, targetId);
CombatApiResult endTurnResult = CombatApi.endTurn();
CombatApiResult selectionResult = CombatApi.selectCards(cardIds);
CombatApiResult skipResult = CombatApi.skipCardSelection();
```

- `playCard`：从当前手牌中定位卡牌并加入原版出牌队列。
- `usePotion`：使用指定药水槽中的药水，并执行原版遗物回调和药水移除流程。
- `endTurn`：触发原版结束回合按钮流程，排队执行玩家回合收尾和敌方回合。
- `selectCards`：处理战斗中的手牌选择、网格选择和部分卡牌奖励选择界面；空数组只表示确认一个原版允许的零张选择。
- `skipCardSelection`：仅在原版将当前战斗卡牌奖励选择标记为可跳过时执行 Skip。
- 所有命令都返回 `CombatApiResult`，其中包含成功状态、错误码和说明消息。

标识规则：

- 卡牌使用当前实例的 `AbstractCard.uuid` 字符串。
- 敌人优先使用 `monster:<index>`；当原始怪物 ID 唯一时，也可以使用原始 ID。
- 药水使用玩家药水栏的零基槽位。

可以通过 `CombatIdentifiers.cardId(card)` 和 `CombatIdentifiers.targetId(monster)` 生成对应标识。

> 战斗 API 会直接读取并修改《杀戮尖塔》的全局状态，因此必须在游戏渲染线程调用。目前项目没有提供 HTTP、WebSocket 或其他外部传输层。

### 整局命令 API

`RunApi` 独立于 `CombatApi`，负责整局生命周期命令。当前可以从主菜单直接启动指定种子和 `RunID` 的铁甲战士普通难度对局：

```java
RunApiResult result = RunApi.startRun(seed, runId);
RunApiResult ascensionResult = RunApi.startRun(seed, runId, ascensionLevel);
```

- 仅当没有正在运行或正在启动的 Run 时接受命令；否则返回 `run_already_active`。
- 游戏尚未进入可用主菜单状态时返回 `game_not_ready`。
- 角色固定为铁甲战士；两参数版本关闭进阶，三参数版本开启指定的 `1`–`20` 级进阶，其他值返回 `invalid_ascension_level`。
- 每日挑战、自定义挑战和无尽模式始终关闭。
- 跳过主菜单淡出、第一幕标题转场和开局淡入；不会跳过涅奥奖励等会改变玩法结果的交互。
- 原版 `AbstractDungeon` 会被注入公开的 `int RunID` 字段及 `getRunID()`；同一局跨幕创建的新 dungeon 实例保持相同 `RunID`。
- 未通过 `RunApi` 启动的对局使用默认 `RunID` `0`。也可通过 `GameAccess.runId()` 读取当前局标识。
- 与战斗 API 相同，必须在游戏渲染线程调用；命令返回成功表示开局已排入原版下一次游戏更新。

### 休息点选择 API

```java
RestApi.selectOption(VanillaRestOptionCatalog.REST);
```

- 入口为 `reallyfastmode.api.RestApi.RestApi.selectOption`，只接收一个 `VanillaRestOptionCatalog` 枚举参数。
- 支持 `REST`（休息）、`SMITH`（升级）、`DIG`（挖掘）、`LIFT`（举重）、`TOKE`（删牌）、`RECALL`（回忆）。
- 从当前营火的真实选项列表中精确匹配类型，直接调用原版 `useOption()`，然后设置 `somethingSelected = true`。
- `null`、`UNKNOWN`、当前不存在或 `usable == false` 的选项直接抛出 `IllegalArgumentException`；不在可操作休息点、已选择、存在其他界面或正在转场时抛出 `IllegalStateException`。校验失败不修改状态。
- 必须在游戏渲染线程调用。正常返回表示已触发选项；治疗、奖励、升级或删牌选择等后续流程继续由原版处理。

### 原子状态读取 API

`reallyfastmode.access` 提供只读的游戏状态入口：

```java
int hp = PlayerAccess.hp();
int energy = PlayerAccess.energy();
int runId = GameAccess.runId();
List<AbstractCard> hand = CardAccess.hand();
List<AbstractMonster> monsters = MonsterAccess.monsters();
int monsterInstanceId = MonsterAccess.instanceId(monsters.get(0));
byte monsterInstanceId4Bit = MonsterAccess.instanceId4Bit(monsters.get(0));
List<AbstractCard> candidates = SelectionAccess.candidates();
List<MapRoomNode> nodes = MapAccess.availableMapNodes();
```

读取层按领域分为 `GameAccess`、`PlayerAccess`、`CombatAccess`、`CardAccess`、`MonsterAccess`、`PotionAccess`、`SelectionAccess`、`MapAccess`、`RewardAccess`、`EventAccess`、`ShopAccess` 和 `RestAccess`。

- 数据始终来自当前 `AbstractDungeon`、Player、Room、Screen 或 Action，不缓存完整状态，也不提供历史记录。
- 集合是不可修改的浅拷贝，其中的卡牌、怪物等奖励对象仍是当前真实 STS 对象。
- 每个已初始化怪物拥有房间内实例 ID；`MonsterAccess.instanceId4Bit(monster)` 使用高四位恒为零的 `byte` 容器返回 `0`–`15`，第 17 个怪物初始化时直接报错。
- 读取层不生成 JSON 或 packet，不处理 transport，也不决定下一步行动。
- 所有读取都必须在游戏渲染线程调用；返回的原版对象应当视为只读。

### Game 协议

`GameSnapshotBuilder` 捕获一份当前 Game 标量快照，`GameBlockWriter` 按固定顺序将它直接写入 MSB-first、big-endian bit stream：

```java
GameSnapshot game = GameSnapshotBuilder.build();
BitWriter out = new BitWriter(16);
GameBlockWriter.write(out, game);
byte[] block = out.toByteArray();
```

Game block 依次编码 act、floor、进阶等级和三把钥匙，共 17 bit。act 直接读取 `AbstractDungeon.actNum`：原版 `1`–`4` 显式映射为 wire ID `0`–`3`，其他值映射为 UNKNOWN `4`。

### Player 协议

`PlayerSnapshotBuilder` 捕获玩家标量状态以及 Orb、Power、Relic 变长列，`PlayerBlockWriter` 按 `ProtocolSlots.Player` 的顺序编码：

```java
PlayerSnapshot player = PlayerSnapshotBuilder.build();
BitWriter out = new BitWriter(64);
PlayerBlockWriter.write(out, player);
byte[] block = out.toByteArray();
```

`maxOrbs` 直接指定 Orb wire ID 列长度；Power 和 Relic 分别先写入 5-bit 和 8-bit entry count，对应 count 列与其 ID 列等长。Relic count 使用 8-bit unsigned，原版负数哨兵统一编码为 0。未知或 Mod Orb、Power、Relic 使用各自原版目录的尾值。

### Combat 协议

`CombatSnapshotBuilder` 捕获当前 `GameActionManager.turn`，`CombatBlockWriter` 将其编码为一个 8-bit unsigned 值：

```java
CombatSnapshot combat = CombatSnapshotBuilder.build();
BitWriter out = new BitWriter(1);
CombatBlockWriter.write(out, combat);
byte[] block = out.toByteArray();
```

Combat block 固定为 8 bit，允许回合数 `0–255`。

### Potion 协议

`PotionSnapshotBuilder` 从 `PlayerAccess.potionSlots()` 获取玩家当前药水槽位总数，并捕获每个槽位的 Potion wire ID：

```java
PotionSnapshot potions = PotionSnapshotBuilder.build();
BitWriter out = new BitWriter(8);
PotionBlockWriter.write(out, potions);
byte[] block = out.toByteArray();
```

槽位数使用 3-bit，每个 Potion wire ID 使用 6-bit。原版目录为 `0–42`，空槽固定为 `43`，未知或 Mod Potion 固定为 `44`。

### Card SoA 协议

`CardColumnsBuilder` 使用同一实现捕获 deck 或战斗牌堆，`CardBlockWriter` 根据快照的 layout 写出对应列：

```java
CardColumns deck = CardColumnsBuilder.buildDeck(CardAccess.masterDeck());
CardColumns hand = CardColumnsBuilder.buildCombat(CardAccess.hand());
BitWriter out = new BitWriter(128);
CardBlockWriter.write(out, deck);
CardBlockWriter.write(out, hand);
byte[] blocks = out.toByteArray();
```

Deck 编码基础 `cost`、升级状态和三个瓶装标志；hand、draw pile、discard pile、exhaust pile 编码 `costForTurn` 和升级状态。空牌堆编码为零数量 block，X 费保持 `-1`，未知卡牌使用 wire ID `432`。

### Monster SoA 协议

`MonsterColumnsBuilder` 将调用方给出的怪物列表捕获为 SoA，`MonsterBlockWriter` 再按固定列顺序写入 MSB-first、big-endian bit stream：

```java
MonsterColumns monsters = MonsterColumnsBuilder.build(MonsterAccess.monsters());
BitWriter out = new BitWriter(128);
MonsterBlockWriter.write(out, monsters);
byte[] block = out.toByteArray();
```

捕获层要求输入列表非空并保持其顺序，不决定环境、存活状态或目标可用性；没有怪物时应由上层省略 Monster block，而不是调用 Builder。当前布局最多编码 16 个怪物。`ProtocolSlots` 只作为具体 SoA 类型的字段顺序参考，不使用 `Object[]` 承载协议数据。

### 零拷贝 packet 输出

packet 层能够预先计算最终字节数时，应当先分配最终数组，再让 `BitWriter` 直接写入该数组：

```java
byte[] packet = new byte[packetByteSize];
BitWriter out = BitWriter.wrap(packet);
// 依次写入 header 和 blocks
// 写完后直接发送 packet，不调用 toByteArray()
```

`wrap` 使用固定容量且不会替换底层数组；写出容量会在该次写入修改数组前抛出异常。目标数组必须是新建或已清零的数组。只有最终长度未知的场景才使用 growable constructor 和 `toByteArray()`。

## 运行环境

- 《杀戮尖塔》`12-18-2022`
- Java 8
- Maven
- ModTheSpire `3.30.0`
- BaseMod `5.56.0`
