package reallyfastmode.codegen;

import java.nio.file.Paths;

/** Generates all non-card vanilla ID catalogs in one run. */
public final class VanillaEntityCatalogGenerator {
    private VanillaEntityCatalogGenerator() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            throw new IllegalArgumentException("VanillaEntityCatalogGenerator does not accept arguments");
        }

        int potionCount = VanillaPotionCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaCatalog/VanillaPotionCatalog.java"
        ));
        int monsterCount = VanillaMonsterCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaCatalog/VanillaMonsterCatalog.java"
        ));
        int powerCount = VanillaPowerCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaCatalog/VanillaPowerCatalog.java"
        ));
        int relicCount = VanillaRelicCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaCatalog/VanillaRelicCatalog.java"
        ));
        int eventCount = VanillaEventCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaCatalog/VanillaEventCatalog.java"
        ));
        System.out.println(
            "Generated vanilla catalogs with " + potionCount + " potions, "
                + monsterCount + " monsters, " + powerCount + " powers, and "
                + relicCount + " relics, and " + eventCount + " events."
        );
    }
}
