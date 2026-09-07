package reallyfastmode.codegen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/** One-shot generator for {@code VanillaMonsterCatalog}. */
public final class VanillaMonsterCatalogGenerator {
    private static final Path DEFAULT_OUTPUT = Paths.get(
        "src/main/java/reallyfastmode/protocol/VanillaCatalog/VanillaMonsterCatalog.java"
    );

    private VanillaMonsterCatalogGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length == 0 ? DEFAULT_OUTPUT : Paths.get(args[0]);
        int count = generate(output);
        System.out.println("Generated " + output + " with " + count + " vanilla monsters.");
    }

    static int generate(Path output) throws Exception {
        return AsmVanillaCatalogGenerator.generate(
            VanillaMonsterCatalogGenerator.class.getClassLoader(),
            "com/megacrit/cardcrawl/monsters/",
            "com/megacrit/cardcrawl/monsters/AbstractMonster",
            Collections.<String>emptySet(),
            "ID",
            output,
            "VanillaMonsterCatalog",
            "VanillaMonsterCatalogGenerator",
            "monsterId",
            "monsterIdToWireId"
        );
    }
}
