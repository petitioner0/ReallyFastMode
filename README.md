# ReallyFastMode

一个用于《杀戮尖塔》（Slay the Spire）的实验性 Mod。开启快速模式后，它会跳过战斗中的大部分纯视觉动画和等待时间，同时尽量保留原版结算流程、回调与游戏状态变化。

项目还提供了一组游戏内 Java 战斗 API，可以绕过鼠标拖拽和 Hitbox 点击，以语义命令完成出牌、使用药水和选择卡牌。

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

### 原子状态读取 API

`reallyfastmode.access` 提供只读的游戏状态入口：

```java
int hp = PlayerAccess.hp();
int energy = PlayerAccess.energy();
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

Deck 编码基础 `cost`、升级状态和三个瓶装标志；hand、draw pile、discard pile、exhaust pile 编码 `costForTurn` 和升级状态。空牌堆编码为零数量 block，X 费保持 `-1`，未知卡牌使用 wire ID `432`。精确列顺序和位宽见 [`describe/protocol/card-soa.md`](describe/protocol/card-soa.md)。

### Monster SoA 协议

`MonsterColumnsBuilder` 将调用方给出的怪物列表捕获为 SoA，`MonsterBlockWriter` 再按固定列顺序写入 MSB-first、big-endian bit stream：

```java
MonsterColumns monsters = MonsterColumnsBuilder.build(MonsterAccess.monsters());
BitWriter out = new BitWriter(128);
MonsterBlockWriter.write(out, monsters);
byte[] block = out.toByteArray();
```

捕获层要求输入列表非空并保持其顺序，不决定环境、存活状态或目标可用性；没有怪物时应由上层省略 Monster block，而不是调用 Builder。当前布局最多编码 16 个怪物；Monster、Power、Intent wire ID 和每列精确位宽记录在 [`describe/protocol/monster-soa.md`](describe/protocol/monster-soa.md)。`ProtocolSlots` 只作为具体 SoA 类型的字段顺序参考，不使用 `Object[]` 承载协议数据。

## 运行环境

- 《杀戮尖塔》`12-18-2022`
- Java 8
- Maven
- ModTheSpire `3.30.0`
- BaseMod `5.56.0`

## 设计边界

为了减少加速对玩法结算的影响，项目遵循以下原则：

- 只精确匹配已确认的原版具体类，不自动跳过未知的 Mod 子类。
- 涉及玩法结算的 Action 至少运行一次原版更新逻辑，而不是直接丢弃。
- 洗牌和抽牌保留原版随机数、逐卡回调、遗物回调及能力回调。
- 带有玩法副作用的视觉 Effect 不会作为纯动画过滤。
- 跨幕和返回主菜单使用的全局场景转场暂时保留。
- 战斗 API 只在战斗空闲、玩家可操作且没有其他界面阻塞时接受出牌、药水和结束回合命令。
