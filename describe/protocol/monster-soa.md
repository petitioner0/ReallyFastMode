# Monster SoA Wire Format

## Responsibility

Monster 编码链只负责将调用方提供的非空 `List<AbstractMonster>` 捕获为稳定的语义列，并把这些列编码到 bit stream。它不读取当前房间、不筛选存活怪物，也不负责 packet header、presence mask 或 transport。没有怪物时，上层应省略 Monster block；向 Builder 传入 null 或空列表会直接报错。

```text
List<AbstractMonster>
→ MonsterColumnsBuilder
→ MonsterColumns
→ MonsterBlockWriter
→ BitWriter
→ byte[]
```

## Bit convention

- byte 内按 most-significant bit first 写入。
- 多字节整数为 big-endian。
- signed 字段使用 two's-complement。
- block 结束时不主动做字节对齐；只有 `BitWriter.toByteArray()` 返回独立 byte array 时，末字节尚未使用的低位保持为零。

## Layout

令 `N = monster_count`，`P = sum(power_entry_count)`。Power 按怪物输入顺序连续扁平化；第 `i` 个怪物拥有的 Power 区间由前面怪物的 `power_entry_count` 前缀和确定。

| 顺序 | 字段 | 数量 | 编码 |
|---|---|---:|---|
| 1 | `monster_count` | 1 | 5-bit unsigned，当前限制 0–16 |
| 2 | `instance_id` | N | 4-bit unsigned |
| 3 | `monster_wire_id` | N | 8-bit unsigned |
| 4 | `hp` | N | 12-bit unsigned |
| 5 | `max_hp` | N | 12-bit unsigned |
| 6 | `block` | N | 16-bit unsigned |
| 7 | `power_entry_count` | N | 5-bit unsigned，单只怪物限制 0–31 |
| 8 | `power_wire_id` | P | 12-bit unsigned；当前固定为 0 |
| 9 | `power_amount` | P | 12-bit signed，范围 -2048–2047 |
| 10 | `intent_wire_id` | N | 5-bit unsigned；当前固定为 17 |
| 11 | `intent_damage` | N | 8-bit unsigned |
| 12 | `intent_multi_amount` | N | 8-bit unsigned |

所有字段在开始写 block 前完成结构和范围校验；越界会抛出 `IllegalArgumentException`，不会截断、饱和或留下半写入 block。

## Intent normalization

- 非攻击意图编码为 `intent_damage=0, intent_multi_amount=0`。
- 单段攻击编码为当前每段伤害和 `intent_multi_amount=1`。
- 多段攻击编码为当前每段伤害和原版的真实攻击段数。
- 当前尚未分配 Intent wire ID，因此所有 Intent（包括 null）暂时编码为 UNKNOWN `17`。

## Wire IDs and TODOs

- `VanillaMonsterCatalog` 当前固定原版 Monster wire ID `0–65`，未知 Monster 使用尾值 `66`。
- Power catalog 尚未定义；本阶段保留 Power 的列表形状与 `amount`，但所有 `power_wire_id` 使用临时 UNKNOWN `0`。
- Power 和 Intent catalog 落地时，需要同步确定占位值迁移和 packet 协议版本兼容策略。

## Bounds and omitted state

当前协议假设一场战斗不会初始化超过 16 个怪物，HP/maxHP 不超过 4095，单段意图伤害不超过 255。协议不包含 Monster 类型、通用 `DamageInfo`、死亡、逃跑、half-dead 或可选中状态；调用方负责选择传给 Builder 的怪物集合。
