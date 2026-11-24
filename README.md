# ReallyFastMode

一个用于《杀戮尖塔》（Slay the Spire）的实验性 Mod。开启快速模式后，它会跳过战斗中的大部分纯视觉动画和等待时间，同时尽量保留原版结算流程、回调与游戏状态变化。

CLI 和游戏内 Java API 可用于开局、出牌、选牌和推进流程。Mod 通过 UDP 发送游戏状态。

## 功能

### 快速模式

- 跳过纯等待和纯展示 Action。
- 加速出牌、伤害、格挡、治疗、抽牌、洗牌、回合切换及战斗结束后的奖励衔接。
- 跳过卡牌飞行动画、伤害数字、粒子、攻击动作和屏幕震动。
- 跳过普通房间之间的淡入淡出。
- 保留结算相关的原版 `update()`、卡牌移动、遗物/能力回调和洗牌随机结果。
- 关闭快速模式时保持原版行为。

快速模式不会修改游戏的全局 tick，也不会自动跳过 Mod 自定义 Action 或 Effect。卡牌拖拽、目标选择和其他玩家输入流程仍由原版游戏处理。

## CLI 与 Java API

启动加载了 ReallyFastMode 的游戏后，Windows 下推荐在仓库根目录运行 `rfm.bat`。无参数时检查连接，带参数时运行 CLI。脚本依次查找 `target\ReallyFastMode.jar` 和同目录下的 `ReallyFastMode.jar`。

```bat
.\rfm.bat
.\rfm.bat --help
.\rfm.bat selectCards 0 2
```

也可直接用同一份 Mod JAR 运行 CLI。

```sh
java -jar ReallyFastMode.jar --help
java -jar ReallyFastMode.jar playCard 0 monster:0
```

CLI 默认连接本机 TCP `127.0.0.1:18775`。自定义端口时，在游戏 JVM 上设置 `-Dreallyfastmode.cli.port=<port>`，并将 `REALLYFASTMODE_CLI_PORT` 设为同一端口供 `rfm.bat` 使用。直接运行 JAR 时，CLI JVM 也使用该 JVM 参数。退出码 `0` 表示命令已接受，`1` 表示游戏拒绝，`2` 表示参数或连接错误。

游戏内 Mod 可在渲染线程直接调用 [Java API](src/main/java/reallyfastmode/api)。CLI 也在渲染线程执行相应命令。

| 命令 | 位置参数与用途 |
| --- | --- |
| `startRun` | `seed runId [ascensionLevel [characterId]]`，从主菜单开局。对局 ID 范围为 0–65535，角色 ID 0–3 依次为铁甲战士、静默猎手、故障机器人、观者。 |
| `playCard` | `cardIndex [targetId]`，按 `HAND_CARD` 中的零基序号出牌。 |
| `usePotion` | `potionSlot [targetId]`，使用指定零基槽位的药水。 |
| `endTurn` | 结束回合。 |
| `selectCards` | `[cardIndex ...]`，按 `SELECTION` 候选或 `SHOP` 卡牌列表的零基序号选牌。界面允许直接确认时可省略序号。 |
| `skipCardSelection` | 跳过卡牌奖励。 |
| `selectOption` | 事件选项从下往上从 0 编号，禁用项也占编号。营火使用 `REST`、`SMITH`、`DIG`、`LIFT`、`TOKE` 或 `RECALL`。 |
| `selectNode` | `x y`，选择地图节点，Boss 节点为 `-1 15`。 |
| `claimReward` | `rewardIndex`，领取从上往下按零基编号的奖励。 |
| `openChest` | 打开当前宝箱。 |
| `claimBossRelic` | `relicIndex`，按零基序号选择 Boss 遗物。 |
| `proceed` | 执行当前界面的「继续」操作。 |
| `openCardRemoval` | 打开商店删牌界面。 |

`targetId` 可用 `monster:<index>` 指定当前房间怪物列表的零基序号，也可用在列表中唯一的原版怪物 ID。

## 状态包协议

状态包通过 UDP 发往 `127.0.0.1:18776`，每个数据报携带一个完整状态包。游戏 JVM 参数 `-Dreallyfastmode.state.port=<port>` 可更改目的端口。

### 包头

从包起点依次读取 **对局 ID `run_id`（16 位）→ 块存在掩码（16 位）**。掩码最低位编号为 0，置 1 的块按槽位编号从小到大排列，正文从位偏移 32 开始。各字段高位先传，多字节字段为大端序。字段和块连续拼接，最后一个字节剩余的低位补 0。

### 正文

表内从左到右为传输顺序，括号内为位宽。「列」表示先读完该字段的全部元素，再读下一列，位宽按每个元素计算。标注为有符号的字段使用二进制补码，其余数值为无符号。

| 掩码位 | 块 | 字段顺序与位宽 |
| --- | --- | --- |
| 0 | `GAME` | 幕 ID（3）→ 楼层（6）→ 进阶等级（5）→ 红宝石、翡翠、蓝宝石钥匙标志（各 1），共 17 位。幕 1–4 对应 ID 0–3，未知幕为 4。 |
| 1 | `PLAYER` | 生命（10）→ 最大生命（10）→ 格挡（10）→ 能量（8）→ 金币（12）→ 充能球槽数（4）→ 充能球 ID 列（3）→ 能力条目数（5）→ 能力 ID 列（8）→ 能力层数列（12，有符号）→ 遗物条目数（8）→ 遗物 ID 列（8）→ 遗物计数列（8）。各列长度取对应的槽数或条目数。 |
| 2 | `COMBAT` | 回合数（8）。 |
| 3 | `DECK_CARD` | 卡牌数（8）→ 卡牌 ID 列（9）→ 基础费用列（4，有符号）→ 升级标志列（1）→ 瓶装火焰标志列（1）→ 瓶装闪电标志列（1）→ 瓶装旋风标志列（1）。各列长度等于卡牌数。 |
| 4 | `HAND_CARD` | 卡牌数（8）→ 卡牌 ID 列（9）→ 本回合费用列（4，有符号）→ 升级标志列（1）。各列长度等于卡牌数。 |
| 5 | `DRAW_PILE_CARD` | 同 `HAND_CARD`。 |
| 6 | `DISCARD_PILE_CARD` | 同 `HAND_CARD`。 |
| 7 | `EXHAUST_PILE_CARD` | 同 `HAND_CARD`。 |
| 8 | `MONSTER` | 怪物数（5）→ 实例 ID 列（4）→ 怪物类型 ID 列（8）→ 生命列（12）→ 最大生命列（12）→ 格挡列（16）→ 每只怪物的能力条目数列（5）→ 能力 ID 列（8）→ 能力层数列（12，有符号）→ 意图 ID 列（4）→ 意图单次伤害列（8）→ 意图攻击次数列（8）。能力 ID 和层数各按怪物顺序拼接，列长为各怪物的能力条目数之和。其余列长度等于怪物数。 |
| 9 | `POTION` | 药水槽数（3）→ 每槽药水 ID 列（6）。空槽为 43，未知药水为 44。 |
| 10 | `SELECTION` | 候选数（8）→ 最多可选张数（8）→ 候选卡牌 ID 列（9）→ 候选升级标志列（1）。两列长度等于候选数。 |
| 11 | `MAP` | 地图类型（1）→ 全量地图或当前节点，见下方。 |
| 12 | `REWARD` | 奖励数（3）→ 奖励类型 ID 列（3）→ 遗物 ID 列（8）→ 药水 ID 列（6）→ 金币金额列（8）。后三列各按奖励类型列中的出现顺序取值，`GOLD` 和 `STOLEN_GOLD` 共用金额列。 |
| 13 | `EVENT` | 事件 ID（6）→ 选项数（3）→ 从上往下每个选项的可用标志（各 1）。 |
| 14 | `SHOP` | 卡牌数（3）→ 卡牌 ID 列（9）→ 卡牌价格列（9）→ 删牌可用标志（1）→ 删牌价格（4，以 25 金币为单位）→ 遗物数（2）→ 遗物 ID 列（8）→ 遗物价格列（9）→ 药水数（2）→ 药水 ID 列（6）→ 药水价格列（9）。商品列长度等于对应商品数。 |
| 15 | `REST` | 可用营火选项掩码（8）。从最低位起，位 0–5 依次为 `REST`、`SMITH`、`DIG`、`LIFT`、`TOKE`、`RECALL`，位 6 未分配，位 7 表示未知选项。 |

`MAP` 的两种布局如下。

- **类型 0，全量地图。** 接着读取第四幕标志（1）。第四幕到此结束，共 2 位。前三幕继续读取 105 个节点类型 ID（各 4）→ 翡翠钥匙坐标 X、Y（各 4）→ 105 个节点的连线掩码（各 3），共 745 位。两列均按 `y × 7 + x` 排列，连线掩码的位 0、1、2 依次表示向左、中、右连线。钥匙坐标编码为 `(15, 15)` 时表示无钥匙。
- **类型 1，当前节点。** 接着读取 X、Y（各 4），共 9 位。编码 `(0, 15)` 表示尚未选房间，`(15, 15)` 表示 Boss 节点 `(-1, 15)`。

`REWARD` 的类型 ID 0–7 依次为 `CARD`、`GOLD`、`RELIC`、`POTION`、`STOLEN_GOLD`、`EMERALD_KEY`、`SAPPHIRE_KEY`、`UNKNOWN`。其他 ID 映射见[原版目录](src/main/java/reallyfastmode/protocol/VanillaCatalog)和[怪物意图目录](src/main/java/reallyfastmode/protocol/MonsterIntentCatalog.java)。

## 运行环境

- 《杀戮尖塔》`12-18-2022`
- Java 8
- Maven
- ModTheSpire `3.30.0`
- BaseMod `5.56.0`
