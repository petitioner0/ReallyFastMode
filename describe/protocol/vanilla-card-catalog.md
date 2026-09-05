# Vanilla ID Catalogs

## Responsibility

为原版卡牌、药水、怪物、Power 和遗物 ID 提供按字典序固定分配的零基 `wireId`，供协议层使用。目录以生成的 Java enum 固化，不在游戏启动时动态扫描。

## Entry

- 查询：`VanillaCardCatalog.cardIdToWireId`
- 查询：`VanillaPotionCatalog.potionIdToWireId`
- 查询：`VanillaMonsterCatalog.monsterIdToWireId`
- 查询：`VanillaPowerCatalog.powerIdToWireId`
- 查询：`VanillaRelicCatalog.relicIdToWireId`
- Windows 一次性重新生成：`mvn -Pdefault,card-catalog-codegen test-compile exec:java`
- Windows 一次性重新生成药水、怪物、Power 和遗物目录：`mvn -Pdefault,entity-catalog-codegen test-compile exec:java`
- macOS/Linux 将 `default` 换成对应的 `mac`/`linux` profile

## Implementation

```text
VanillaCardCatalogGenerator
→ 扫描 desktop-1.0.jar 的 com.megacrit.cardcrawl.cards.*
→ 筛选 AbstractCard 的具体零参子类
→ 从每个卡类的 class 文件读取 `public static final String ID` 常量
→ 按唯一 cardID 去重、字典序排序并分配 wireId
→ 覆盖生成 reallyfastmode.protocol.VanillaCardCatalog
→ 构造不可修改的 cardIdToWireId Map
```

```text
VanillaEntityCatalogGenerator
→ VanillaPotionCatalogGenerator / VanillaMonsterCatalogGenerator / VanillaPowerCatalogGenerator / VanillaRelicCatalogGenerator
→ AsmVanillaCatalogGenerator 使用 ASM 扫描对应包中的 class 元数据
→ 按继承链筛选 AbstractPotion / AbstractMonster / AbstractPower / AbstractRelic 的具体子类
→ 读取 public static final String POTION_ID / ID / POWER_ID 常量
→ 按唯一 ID 去重、字典序排序并分配 wireId
→ 覆盖生成四个 Vanilla*Catalog enum
→ 各自构造不可修改的 *IdToWireId Map
```

## Dependencies

- Slay the Spire `desktop-1.0.jar`
- Maven profile `card-catalog-codegen`、`entity-catalog-codegen`（仅生成时使用）
- ASM 9.6（test scope，仅生成时使用）

读取 class 常量避免了仅为取 ID 而加载游戏类或启动本地化、UI、贴图和 OpenGL。药水目录包含库存占位对象 `PotionSlot`，确保 `PotionAccess.potions()` 返回的每个原版对象都能映射。
