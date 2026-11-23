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
- `selectCards`：按当前界面选择手牌、网格卡牌、卡牌奖励或商店卡牌，战斗外也可调用。
- `skipCardSelection`：在原版允许跳过当前卡牌奖励时返回上一界面。
- 所有命令都返回 `CombatApiResult`，其中包含成功状态、错误码和说明消息。

标识规则：

- 卡牌使用当前实例的 `AbstractCard.uuid` 字符串。
- 敌人优先使用 `monster:<index>`；当原始怪物 ID 唯一时，也可以使用原始 ID。
- 药水使用玩家药水栏的零基槽位。

可以通过 `CombatIdentifiers.cardId(card)` 和 `CombatIdentifiers.targetId(monster)` 生成对应标识。

> 战斗 API 会直接读取并修改《杀戮尖塔》的全局状态，因此必须在游戏渲染线程调用。目前项目没有提供 HTTP、WebSocket 或其他外部传输层。

### 战斗内外选牌 API

```java
CombatApi.selectCards(firstCardUuid, secondCardUuid); // 当前网格允许选两张时
CombatApi.selectCards(rewardCardUuid); // 领取当前卡牌奖励
CombatApi.selectCards(shopCardUuid); // 在商店购买一张卡牌
ShopApi.openCardRemoval(); // 打开商店删牌网格
CombatApi.selectCards(deckCardUuid); // 选择删牌并由商店扣费
```

- 营火、事件和遗物打开的网格沿用原版候选和数量规则。选择提交后，由原版升级、变化、删牌或遗物流程读取结果。
- 无参数的 `selectCards()` 可确认原版允许的零张选择、单张卡牌预览或直接发放整组卡牌的界面。跳过卡牌奖励使用 `skipCardSelection()`。
- 进入商店房后，原版初始化商品，Mod 随即打开商人界面。可以照常关闭界面并离开房间。
- 购买时从 `ShopAccess.cards()` 获取当前商品的卡牌 UUID。每次购买一张，金币不足时返回 `insufficient_gold`，结算和补货走原版商店逻辑。

### 整局命令 API

`RunApi` 独立于 `CombatApi`，负责整局生命周期命令。可在主菜单指定种子、`RunID`、进阶等级和角色。

```java
RunApiResult result = RunApi.startRun(seed, runId);
RunApiResult ascensionResult = RunApi.startRun(seed, runId, ascensionLevel);
RunApiResult characterResult = RunApi.startRun(seed, runId, ascensionLevel, characterId);
```

- 仅当没有正在运行或正在启动的 Run 时接受命令；否则返回 `run_already_active`。
- 游戏尚未进入可用主菜单状态时返回 `game_not_ready`。
- `characterId` 的 `0`、`1`、`2`、`3` 分别对应铁甲战士、静默猎手、故障机器人和观者。其他值返回 `invalid_character_id`。
- 四参数版本接受 `0`–`20` 级进阶，`0` 表示普通难度。两参数和三参数版本默认使用铁甲战士，分别支持普通难度和 `1`–`20` 级进阶。进阶越界返回 `invalid_ascension_level`。
- 每日挑战、自定义挑战和无尽模式始终关闭。
- 跳过主菜单淡出、第一幕标题转场和开局淡入；不会跳过涅奥奖励等会改变玩法结果的交互。
- 原版 `AbstractDungeon` 会被注入公开的 `int RunID` 字段及 `getRunID()`；同一局跨幕创建的新 dungeon 实例保持相同 `RunID`。
- `runId` 由调用方分配，范围为 `0–65535`。越界返回 `invalid_run_id`。可通过 `GameAccess.runId()` 读取当前局标识。
- `RunID` 写入当前对局存档并在读档时恢复。手动新局和缺少该字段的旧存档使用默认值 `0`。
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

### 地图节点选择 API

```java
MapApi.selectNode(x, y);
```

- 入口为 `reallyfastmode.api.MapApi.MapApi.selectNode(int x, int y)`。普通节点使用零基坐标，先列 `x`、后行 `y`；唯一允许的负坐标是 Boss 节点 `(-1, 15)`。
- 必须在游戏渲染线程、地图选择界面且当前房间已完成时调用。目标必须属于 `MapAccess.availableMapNodes()`。普通节点按首层、地图连线或飞靴 / Flight 判定可达性。
- 坐标无效、节点不存在或不可达直接抛出 `IllegalArgumentException`；当前不可选或已有节点等待进入时抛出 `IllegalStateException`。校验失败不修改状态。
- 普通节点保留首次选房标记、飞靴消耗和问号房计数。API 将 `animWaitTimer` 设为负值，由原版 `MapRoomNode.update` 记录路径并启动切房。
- 完成前三幕各自的地图末排房间，或完成第四幕精英房后，可以用 `selectNode(-1, 15)` 进入 Boss 房。该分支按原版 Boss 图标的规则记录路径并启动转场。

### 事件选项 API

```java
EventApi.selectOption(0); // 选择最下面的选项
```

- 入口为 `reallyfastmode.api.EventApi.EventApi.selectOption(int optionIndex)`，从下往上按 `0, 1, 2, ...` 编号，禁用项同样占用编号。
- 例如从上到下为「选项 A、禁用项、选项 B」，则传 `0` 选择 B、传 `1` 抛异常、传 `2` 选择 A。
- 负数、越界或禁用项直接抛出 `IllegalArgumentException`；没有可操作事件对话框、存在覆盖界面或已有选择等待处理时抛出 `IllegalStateException`。校验失败不修改状态。
- 支持 `GenericEventDialog` 和 `RoomEventDialog`，按 `总数 - 1 - optionIndex` 转换到原版列表下标，提交 `selectedOption` / `waitForInput`，由当前事件后续更新处理效果。
- 必须在游戏渲染线程调用；`EventAccess.options()` 和事件协议仍保持原版从上到下的顺序，调用 API 时需转换编号。

### 开箱与奖励 API

```java
RewardApi.openChest();
RewardApi.claimReward(0); // 尝试领取通用奖励页最上面的一项
RewardApi.claimBossRelic(0); // 选择 Boss 遗物列表中的第一件
```

- `openChest()` 在没有覆盖界面时打开当前房间的普通宝箱或 Boss 宝箱。原版开箱逻辑生成奖励并显示对应界面，领取由后续命令提交。
- `claimBossRelic(int relicIndex)` 按 `RewardAccess.bossRelicChoices()` 的零基序号提交 Boss 遗物选择。原版处理初始遗物替换、选择记录和界面关闭。索引无效时抛出 `IllegalArgumentException`，当前无法选择或已有选择待处理时抛出 `IllegalStateException`。
- 入口为 `reallyfastmode.api.RewardApi.RewardApi.claimReward(int rewardIndex)`，按当前奖励界面从上到下以 `0, 1, 2, ...` 编号。
- 负数或越界直接抛出 `IllegalArgumentException`；奖励界面不可操作或已有一次领取等待处理时抛出 `IllegalStateException`。校验失败不修改状态。
- API 只把对应 `RewardItem.isDone` 设置为 `true`，等同于原版 `RewardItem.update()` 对一次点击的提交结果。下一次 `CombatRewardScreen.update()` 会调用原版 `claimReward()`。
- 奖励是否移除完全采用原版返回值：药水栏满时原版重置该奖励并显示提示；卡牌奖励打开原版选牌界面；金币、遗物和钥匙也走原版实现。
- 这些入口必须在游戏渲染线程调用。`claimReward()` 正常返回表示领取尝试已排队，是否领取成功由下一次游戏更新决定。

### 继续操作 API

```java
ProceedApi.proceed();
```

- 入口为 `reallyfastmode.api.ProceedApi.ProceedApi.proceed()`，必须在游戏渲染线程调用。当前处于奖励页或已完成的房间，且原版「继续」按钮可操作时接受命令。
- 正常返回表示操作已排队。下一次按钮更新会执行原版流程，包括打开地图、推进 Boss 宝箱、跨幕或进入胜利房。若房间或界面已切换，本次操作失效。
- 当前不可继续或已有操作待处理时抛出 `IllegalStateException`。

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

### 状态包头

状态包使用 4 字节包头。前 2 字节是无符号 `run_id`，后 2 字节是块存在掩码，两个字段都用大端序。掩码的第 `n` 位对应 `ProtocolSlots` 中编号为 `n` 的槽位，`0` 表示缺席，`1` 表示存在。正文按槽位编号递增写入存在的块。

全量地图与当前地图节点共用第 11 位。该槽位正文的首位是类型标识。`0` 表示全量地图，紧接 `isAct4`，前三幕共 745 位，第四幕共 2 位。`1` 表示当前节点，紧接 4 位 X 和 4 位 Y，共 9 位。各块按位连续写入。

```java
int mask = PacketHeaderProtocol.blockBit(ProtocolSlots.GAME)
    | PacketHeaderProtocol.blockBit(ProtocolSlots.PLAYER);
PacketHeaderWriter.write(out, GameAccess.runId(), mask);
```

### Java 解码参考实现

接收端可以用任意语言，按二进制协议定义的包头、槽位顺序、各块位布局和 ID 映射自行解码，并选择自己的数据结构、记录生命周期和错误处理方式。本仓库的 [`PacketDecoder`](src/main/java/reallyfastmode/protocol/read/PacketDecoder.java) 和 [`PacketRecord`](src/main/java/reallyfastmode/protocol/read/PacketRecord.java) 是 Java 环境下的参考实现。以下 API 说明适用于这份实现。

`PacketDecoder.tryReadHeader(packet, expectedRunId)` 只检查 4 字节包头。包头缺失、不足 4 字节或 `run_id` 与预期不一致时返回 `null`。通过时返回 `Header`，可用 `hasBlock(slot)` 查询块是否存在，并从原数组的 `bodyOffsetBytes()` 位置继续读正文。

`PacketDecoder.decode(packet, expectedRunId)` 支持全部 16 个槽位。它从 bit 偏移 `32` 开始，按槽位顺序读取掩码中置位的块，全部解码成功后返回末尾 bit 偏移。包头校验失败或字段值无效时抛出 `IllegalArgumentException`，块数据截断抛出 `BufferUnderflowException`。

每个通过包头校验的包都有独立的 `PacketRecord`，`runId` 来自包头。通过 `game()`、`player()`、`combat()`、`deck()`、`hand()`、`drawPile()`、`discardPile()` 和 `exhaustPile()` 读取对应快照。缺席块返回 `null`，存在但为空的牌堆返回 `size` 为 `0` 的快照。`PacketDecoder.latestRecord()` 返回最近一次完整解码的记录，首次成功前返回 `null`。解码抛出异常时保留上一份记录。

解码记录将游戏 ID 还原为字符串（如 `Exordium`、`Lightning`）。未知或未收录的 ID 返回 `Unknown`。数值展开为 Java `int` 或 `int[]`，单比特标记读作 `0` 或 `1`，Power 层数和卡牌费用保留符号。

四种战斗牌堆使用同一布局，依次读取数量、卡牌 ID 列、`costForTurn` 列和升级标记列。瓶装标记只在 `deck()` 中读取。

通过 `monsters()`、`potions()` 和 `selection()` 读取怪物、药水和选牌快照。怪物的 Power 列按怪物顺序拼接，每只怪物的条目数见 `powerEntryCount[]`。药水空槽读取为 `Empty`。

Map 槽位的完整地图通过 `map()` 读取，当前坐标通过 `currentMapSelection()` 读取。完整地图的节点按 `y * 7 + x` 排列，连接展开为左、中、右三个 `0` / `1` 列。无翡翠钥匙时坐标为 `(-1, -1)`。Act 4 标记的 `isAct4` 为 `1`，节点和连接列为空。当前坐标的 `(0, -1)` 表示尚未选房间，`(-1, 15)` 表示 Boss 房。

`map().graph()` 返回地图的静态有向图。用 `entryNodes()` 获取入口，`nodeAt(x, y)` 查找房间，`successors()` 读取某个房间的出边。节点携带坐标、房间类型和 `hasEmeraldKey`。前三幕按包内连线建图，并将有效末排节点接到 `(-1, 15)` 的 Boss。Act 4 按原版固定布局建图，休息点、商店、精英和 Boss 依次相连，胜利房间位于 `(3, 4)`。

通过 `rewards()`、`event()`、`shop()` 和 `rest()` 读取奖励、事件、商店和休息点。奖励的遗物、药水和金额列各自按对应奖励的出现顺序排列，`GOLD` 和 `STOLEN_GOLD` 共用金额列。事件选项用 `0` / `1` 标记可用性。商店的 `purgeCost` 是把编码值按每单位 25 金币换算后的金额，精度为 25 金币。休息点的 `availableOptions` 按掩码位从低到高列出可用选项。

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
