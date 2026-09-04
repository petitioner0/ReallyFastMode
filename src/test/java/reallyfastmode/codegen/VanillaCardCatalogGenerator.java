package reallyfastmode.codegen;

import com.megacrit.cardcrawl.cards.AbstractCard;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * One-shot generator for {@code VanillaCardCatalog}.
 *
 * <p>Run from the repository root with:</p>
 *
 * <pre>{@code
 * mvn -Pdefault,card-catalog-codegen test-compile exec:java
 * }</pre>
 */
public final class VanillaCardCatalogGenerator {
    private static final String CARD_PACKAGE_PATH = "com/megacrit/cardcrawl/cards/";
    private static final Path DEFAULT_OUTPUT = Paths.get(
        "src/main/java/reallyfastmode/protocol/VanillaCardCatalog.java"
    );

    private VanillaCardCatalogGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length == 0 ? DEFAULT_OUTPUT : Paths.get(args[0]);
        ClassLoader loader = VanillaCardCatalogGenerator.class.getClassLoader();

        List<CardDefinition> cards = discoverCards(loader);
        writeCatalog(output, cards);
        System.out.println("Generated " + output + " with " + cards.size() + " vanilla cards.");
    }

    private static List<CardDefinition> discoverCards(ClassLoader loader) throws Exception {
        URL packageUrl = loader.getResource(CARD_PACKAGE_PATH);
        if (packageUrl == null || !"jar".equals(packageUrl.getProtocol())) {
            throw new IllegalStateException("Expected " + CARD_PACKAGE_PATH + " to come from the game JAR");
        }

        List<CardDefinition> cards = new ArrayList<CardDefinition>();
        Set<String> cardIds = new HashSet<String>();
        JarURLConnection connection = (JarURLConnection) packageUrl.openConnection();
        connection.setUseCaches(false);
        try (JarFile jar = connection.getJarFile()) {
            for (JarEntry entry : Collections.list(jar.entries())) {
                String entryName = entry.getName();
                if (!isCandidateClass(entryName)) {
                    continue;
                }

                String className = entryName.substring(0, entryName.length() - ".class".length())
                    .replace('/', '.');
                Class<?> candidate = Class.forName(className, false, loader);
                if (candidate == AbstractCard.class
                    || !AbstractCard.class.isAssignableFrom(candidate)
                    || Modifier.isAbstract(candidate.getModifiers())) {
                    continue;
                }

                String cardId = readConstantCardId(jar, entry);
                if (cardId == null || cardId.isEmpty()) {
                    throw new IllegalStateException(
                        className + " has no constant public static final String ID"
                    );
                }
                if (!cardIds.add(cardId)) {
                    // A few vanilla helper/option classes intentionally reuse a playable card ID.
                    continue;
                }
                cards.add(new CardDefinition(cardId));
            }
        }

        Collections.sort(cards, new Comparator<CardDefinition>() {
            @Override
            public int compare(CardDefinition left, CardDefinition right) {
                return left.cardId.compareTo(right.cardId);
            }
        });
        validateEnumNames(cards);
        return cards;
    }

    private static boolean isCandidateClass(String entryName) {
        return entryName.startsWith(CARD_PACKAGE_PATH)
            && entryName.endsWith(".class")
            && entryName.indexOf('$') < 0;
    }

    private static String readConstantCardId(JarFile jar, JarEntry entry) throws IOException {
        try (DataInputStream input = new DataInputStream(jar.getInputStream(entry))) {
            if (input.readInt() != 0xCAFEBABE) {
                throw new IOException("Invalid class file: " + entry.getName());
            }
            input.readUnsignedShort(); // minor version
            input.readUnsignedShort(); // major version
            Object[] constants = readConstantPool(input);

            input.readUnsignedShort(); // class access
            input.readUnsignedShort(); // this class
            input.readUnsignedShort(); // super class
            int interfaceCount = input.readUnsignedShort();
            for (int i = 0; i < interfaceCount; i++) {
                input.readUnsignedShort();
            }

            int fieldCount = input.readUnsignedShort();
            for (int i = 0; i < fieldCount; i++) {
                int access = input.readUnsignedShort();
                String name = (String) constants[input.readUnsignedShort()];
                String descriptor = (String) constants[input.readUnsignedShort()];
                int attributeCount = input.readUnsignedShort();
                for (int j = 0; j < attributeCount; j++) {
                    String attributeName = (String) constants[input.readUnsignedShort()];
                    int length = input.readInt();
                    if ("ID".equals(name)
                        && "Ljava/lang/String;".equals(descriptor)
                        && "ConstantValue".equals(attributeName)
                        && Modifier.isPublic(access)
                        && Modifier.isStatic(access)
                        && Modifier.isFinal(access)) {
                        if (length != 2) {
                            throw new IOException("Invalid ConstantValue in " + entry.getName());
                        }
                        int valueIndex = input.readUnsignedShort();
                        return resolveStringConstant(constants, valueIndex);
                    }
                    skipFully(input, length);
                }
            }
            return null;
        }
    }

    private static Object[] readConstantPool(DataInputStream input) throws IOException {
        int count = input.readUnsignedShort();
        Object[] constants = new Object[count];
        for (int i = 1; i < count; i++) {
            int tag = input.readUnsignedByte();
            switch (tag) {
                case 1: // Utf8
                    constants[i] = input.readUTF();
                    break;
                case 3: // Integer
                case 4: // Float
                    input.readInt();
                    break;
                case 5: // Long
                case 6: // Double
                    input.readLong();
                    i++;
                    break;
                case 7: // Class
                case 8: // String
                case 16: // MethodType
                    constants[i] = new ConstantReference(input.readUnsignedShort());
                    break;
                case 9: // Fieldref
                case 10: // Methodref
                case 11: // InterfaceMethodref
                case 12: // NameAndType
                case 18: // InvokeDynamic
                    input.readUnsignedShort();
                    input.readUnsignedShort();
                    break;
                case 15: // MethodHandle
                    input.readUnsignedByte();
                    input.readUnsignedShort();
                    break;
                default:
                    throw new IOException("Unsupported constant-pool tag " + tag);
            }
        }
        return constants;
    }

    private static String resolveStringConstant(Object[] constants, int index) throws IOException {
        Object constant = constants[index];
        if (!(constant instanceof ConstantReference)) {
            throw new IOException("ConstantValue is not a String constant");
        }
        Object value = constants[((ConstantReference) constant).index];
        if (!(value instanceof String)) {
            throw new IOException("String constant does not reference UTF-8 text");
        }
        return (String) value;
    }

    private static void skipFully(DataInputStream input, int byteCount) throws IOException {
        int remaining = byteCount;
        while (remaining > 0) {
            int skipped = input.skipBytes(remaining);
            if (skipped == 0) {
                input.readByte();
                skipped = 1;
            }
            remaining -= skipped;
        }
    }

    private static void validateEnumNames(List<CardDefinition> cards) {
        Map<String, String> names = new HashMap<String, String>();
        for (CardDefinition card : cards) {
            String enumName = enumName(card.cardId);
            String previous = names.put(enumName, card.cardId);
            if (previous != null) {
                throw new IllegalStateException(
                    "cardIDs '" + previous + "' and '" + card.cardId
                        + "' both map to enum constant " + enumName
                );
            }
        }
    }

    private static void writeCatalog(Path output, List<CardDefinition> cards) throws IOException {
        Path absoluteOutput = output.toAbsolutePath().normalize();
        Path parent = absoluteOutput.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
            absoluteOutput,
            StandardCharsets.UTF_8
        )) {
            writer.write("package reallyfastmode.protocol;\n\n");
            writer.write("import java.util.Collections;\n");
            writer.write("import java.util.LinkedHashMap;\n");
            writer.write("import java.util.Map;\n\n");
            writer.write("/** Generated by VanillaCardCatalogGenerator. Do not edit by hand. */\n");
            writer.write("public enum VanillaCardCatalog {\n");
            for (int wireId = 0; wireId < cards.size(); wireId++) {
                CardDefinition card = cards.get(wireId);
                writer.write("    ");
                writer.write(enumName(card.cardId));
                writer.write("(");
                writer.write(Integer.toString(wireId));
                writer.write(", \"");
                writer.write(escapeJava(card.cardId));
                writer.write("\")");
                writer.write(wireId + 1 == cards.size() ? ";\n\n" : ",\n");
            }
            writer.write("    public static final Map<String, Integer> cardIdToWireId;\n\n");
            writer.write("    static {\n");
            writer.write("        Map<String, Integer> ids = new LinkedHashMap<String, Integer>();\n");
            writer.write("        for (VanillaCardCatalog card : values()) {\n");
            writer.write("            ids.put(card.cardId, card.wireId);\n");
            writer.write("        }\n");
            writer.write("        cardIdToWireId = Collections.unmodifiableMap(ids);\n");
            writer.write("    }\n\n");
            writer.write("    public final int wireId;\n");
            writer.write("    public final String cardId;\n\n");
            writer.write("    VanillaCardCatalog(int wireId, String cardId) {\n");
            writer.write("        this.wireId = wireId;\n");
            writer.write("        this.cardId = cardId;\n");
            writer.write("    }\n");
            writer.write("}\n");
        }
    }

    private static String enumName(String cardId) {
        StringBuilder name = new StringBuilder();
        boolean previousWasUnderscore = false;
        for (int i = 0; i < cardId.length(); i++) {
            char character = cardId.charAt(i);
            if (character >= 'a' && character <= 'z'
                || character >= 'A' && character <= 'Z'
                || character >= '0' && character <= '9') {
                name.append(Character.toUpperCase(character));
                previousWasUnderscore = false;
            } else if (!previousWasUnderscore) {
                name.append('_');
                previousWasUnderscore = true;
            }
        }
        while (name.length() > 0 && name.charAt(name.length() - 1) == '_') {
            name.setLength(name.length() - 1);
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("cardID does not contain an enum-name character: " + cardId);
        }
        if (Character.isDigit(name.charAt(0))) {
            name.insert(0, '_');
        }
        return name.toString().toUpperCase(Locale.ROOT);
    }

    private static String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class ConstantReference {
        private final int index;

        private ConstantReference(int index) {
            this.index = index;
        }
    }

    private static final class CardDefinition {
        private final String cardId;

        private CardDefinition(String cardId) {
            this.cardId = cardId;
        }
    }
}
