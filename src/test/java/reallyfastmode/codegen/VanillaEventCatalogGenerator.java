package reallyfastmode.codegen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/** One-shot generator for {@code VanillaEventCatalog}. */
public final class VanillaEventCatalogGenerator {
    private static final Path DEFAULT_OUTPUT = Paths.get(
        "src/main/java/reallyfastmode/protocol/VanillaEventCatalog.java"
    );

    private VanillaEventCatalogGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length == 0 ? DEFAULT_OUTPUT : Paths.get(args[0]);
        int count = generate(output);
        System.out.println("Generated " + output + " with " + count + " vanilla events.");
    }

    static int generate(Path output) throws Exception {
        return AsmVanillaCatalogGenerator.generate(
            VanillaEventCatalogGenerator.class.getClassLoader(),
            "com/megacrit/cardcrawl/events/",
            "com/megacrit/cardcrawl/events/AbstractEvent",
            Collections.<String>emptySet(),
            "ID",
            output,
            "VanillaEventCatalog",
            "VanillaEventCatalogGenerator",
            "eventId",
            "eventIdToWireId"
        );
    }
}
