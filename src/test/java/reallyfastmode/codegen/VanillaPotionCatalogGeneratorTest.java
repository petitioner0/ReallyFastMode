package reallyfastmode.codegen;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VanillaPotionCatalogGeneratorTest {
    @Test
    public void appendsFixedEmptyAndUnknownSentinels() throws Exception {
        Path output = Files.createTempFile("VanillaPotionCatalog", ".java");
        try {
            assertEquals(45, VanillaPotionCatalogGenerator.generate(output));

            String source = new String(Files.readAllBytes(output), StandardCharsets.UTF_8);
            assertTrue(source.contains("package reallyfastmode.protocol.VanillaCatalog;"));
            assertTrue(source.contains("WEAK_POTION(42, \"Weak Potion\"),"));
            assertTrue(source.contains("EMPTY(43, \"Empty\"),"));
            assertTrue(source.contains("UNKNOWN(44, \"Unknown\");"));
        } finally {
            Files.deleteIfExists(output);
        }
    }
}
