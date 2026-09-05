package reallyfastmode.codegen;

import java.nio.file.Paths;

/** Generates the vanilla potion, monster, power, and relic ID catalogs in one run. */
public final class VanillaEntityCatalogGenerator {
    private VanillaEntityCatalogGenerator() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            throw new IllegalArgumentException("VanillaEntityCatalogGenerator does not accept arguments");
        }

        int potionCount = VanillaPotionCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaPotionCatalog.java"
        ));
        int monsterCount = VanillaMonsterCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaMonsterCatalog.java"
        ));
        int powerCount = VanillaPowerCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaPowerCatalog.java"
        ));
        int relicCount = VanillaRelicCatalogGenerator.generate(Paths.get(
            "src/main/java/reallyfastmode/protocol/VanillaRelicCatalog.java"
        ));
        System.out.println(
            "Generated vanilla catalogs with " + potionCount + " potions, "
                + monsterCount + " monsters, " + powerCount + " powers, and "
                + relicCount + " relics."
        );
    }
}
