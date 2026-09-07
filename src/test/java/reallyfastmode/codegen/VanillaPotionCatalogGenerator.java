package reallyfastmode.codegen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

/** One-shot generator for {@code VanillaPotionCatalog}. */
public final class VanillaPotionCatalogGenerator {
    private static final Path DEFAULT_OUTPUT = Paths.get(
        "src/main/java/reallyfastmode/protocol/VanillaCatalog/VanillaPotionCatalog.java"
    );

    private VanillaPotionCatalogGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length == 0 ? DEFAULT_OUTPUT : Paths.get(args[0]);
        int count = generate(output);
        System.out.println("Generated " + output + " with " + count + " vanilla potions.");
    }

    static int generate(Path output) throws Exception {
        return AsmVanillaCatalogGenerator.generate(
            VanillaPotionCatalogGenerator.class.getClassLoader(),
            "com/megacrit/cardcrawl/potions/",
            "com/megacrit/cardcrawl/potions/AbstractPotion",
            Collections.<String>emptySet(),
            "POTION_ID",
            output,
            "VanillaPotionCatalog",
            "VanillaPotionCatalogGenerator",
            "potionId",
            "potionIdToWireId",
            Arrays.asList("Empty", "Unknown")
        );
    }
}
