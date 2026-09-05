package reallyfastmode.codegen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/** One-shot generator for {@code VanillaPowerCatalog}. */
public final class VanillaPowerCatalogGenerator {
    private static final Path DEFAULT_OUTPUT = Paths.get(
        "src/main/java/reallyfastmode/protocol/VanillaPowerCatalog.java"
    );

    private VanillaPowerCatalogGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length == 0 ? DEFAULT_OUTPUT : Paths.get(args[0]);
        int count = generate(output);
        System.out.println("Generated " + output + " with " + count + " vanilla powers.");
    }

    static int generate(Path output) throws Exception {
        return AsmVanillaCatalogGenerator.generate(
            VanillaPowerCatalogGenerator.class.getClassLoader(),
            "com/megacrit/cardcrawl/powers/",
            "com/megacrit/cardcrawl/powers/AbstractPower",
            Collections.<String>emptySet(),
            "POWER_ID",
            output,
            "VanillaPowerCatalog",
            "VanillaPowerCatalogGenerator",
            "powerId",
            "powerIdToWireId"
        );
    }
}
