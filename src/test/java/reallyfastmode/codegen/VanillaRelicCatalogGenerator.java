package reallyfastmode.codegen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/** One-shot generator for {@code VanillaRelicCatalog}. */
public final class VanillaRelicCatalogGenerator {
    private static final Path DEFAULT_OUTPUT = Paths.get(
        "src/main/java/reallyfastmode/protocol/VanillaRelicCatalog.java"
    );

    private VanillaRelicCatalogGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length == 0 ? DEFAULT_OUTPUT : Paths.get(args[0]);
        int count = generate(output);
        System.out.println("Generated " + output + " with " + count + " vanilla relics.");
    }

    static int generate(Path output) throws Exception {
        return AsmVanillaCatalogGenerator.generate(
            VanillaRelicCatalogGenerator.class.getClassLoader(),
            "com/megacrit/cardcrawl/relics/",
            "com/megacrit/cardcrawl/relics/AbstractRelic",
            Collections.<String>emptySet(),
            "ID",
            output,
            "VanillaRelicCatalog",
            "VanillaRelicCatalogGenerator",
            "relicId",
            "relicIdToWireId"
        );
    }
}
