package reallyfastmode.codegen;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VanillaEventCatalogGeneratorTest {
    @Test
    public void appendsTheFixedSixBitUnknownSentinel() throws Exception {
        Path output = Files.createTempFile("VanillaEventCatalog", ".java");
        try {
            assertEquals(52, VanillaEventCatalogGenerator.generate(output));

            String source = new String(Files.readAllBytes(output), StandardCharsets.UTF_8);
            assertTrue(source.contains("WORLD_OF_GOOP(51, \"World of Goop\"),"));
            assertTrue(source.contains("UNKNOWN(63, null);"));
            assertTrue(source.contains(
                "return wireId == null ? UNKNOWN.wireId : wireId;"
            ));
        } finally {
            Files.deleteIfExists(output);
        }
    }
}
