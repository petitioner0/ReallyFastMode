package reallyfastmode.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Shared ASM implementation for one-shot vanilla ID catalog generators. */
final class AsmVanillaCatalogGenerator {
    private static final int REQUIRED_ID_ACCESS = Opcodes.ACC_PUBLIC
        | Opcodes.ACC_STATIC
        | Opcodes.ACC_FINAL;

    private AsmVanillaCatalogGenerator() {
    }

    static int generate(
        ClassLoader loader,
        String packagePath,
        String baseClassName,
        Set<String> excludedClassNames,
        String constantFieldName,
        Path output,
        String catalogClassName,
        String generatorClassName,
        String idFieldName,
        String mapFieldName
    ) throws IOException {
        return generate(
            loader,
            packagePath,
            baseClassName,
            excludedClassNames,
            constantFieldName,
            output,
            catalogClassName,
            generatorClassName,
            idFieldName,
            mapFieldName,
            Collections.<String>emptyList(),
            null,
            null
        );
    }

    static int generate(
        ClassLoader loader,
        String packagePath,
        String baseClassName,
        Set<String> excludedClassNames,
        String constantFieldName,
        Path output,
        String catalogClassName,
        String generatorClassName,
        String idFieldName,
        String mapFieldName,
        List<String> trailingIds
    ) throws IOException {
        return generate(
            loader,
            packagePath,
            baseClassName,
            excludedClassNames,
            constantFieldName,
            output,
            catalogClassName,
            generatorClassName,
            idFieldName,
            mapFieldName,
            trailingIds,
            null,
            null
        );
    }

    static int generate(
        ClassLoader loader,
        String packagePath,
        String baseClassName,
        Set<String> excludedClassNames,
        String constantFieldName,
        Path output,
        String catalogClassName,
        String generatorClassName,
        String idFieldName,
        String mapFieldName,
        String lookupClassName,
        String lookupIdFieldName
    ) throws IOException {
        return generate(
            loader,
            packagePath,
            baseClassName,
            excludedClassNames,
            constantFieldName,
            output,
            catalogClassName,
            generatorClassName,
            idFieldName,
            mapFieldName,
            Collections.<String>emptyList(),
            lookupClassName,
            lookupIdFieldName
        );
    }

    private static int generate(
        ClassLoader loader,
        String packagePath,
        String baseClassName,
        Set<String> excludedClassNames,
        String constantFieldName,
        Path output,
        String catalogClassName,
        String generatorClassName,
        String idFieldName,
        String mapFieldName,
        List<String> trailingIds,
        String lookupClassName,
        String lookupIdFieldName
    ) throws IOException {
        List<String> ids = discoverIds(
            loader,
            packagePath,
            baseClassName,
            excludedClassNames,
            constantFieldName
        );
        appendTrailingIds(ids, trailingIds, idFieldName);
        validateEnumNames(ids, idFieldName);
        writeCatalog(
            output,
            ids,
            catalogClassName,
            generatorClassName,
            idFieldName,
            mapFieldName,
            lookupClassName,
            lookupIdFieldName
        );
        return ids.size();
    }

    private static void appendTrailingIds(
        List<String> ids,
        List<String> trailingIds,
        String idFieldName
    ) {
        if (trailingIds == null) {
            throw new NullPointerException("trailingIds");
        }
        Set<String> seen = new HashSet<String>(ids);
        for (String id : trailingIds) {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException(idFieldName + " trailing ID is empty");
            }
            if (!seen.add(id)) {
                throw new IllegalArgumentException("duplicate trailing ID '" + id + "'");
            }
            ids.add(id);
        }
    }

    private static List<String> discoverIds(
        ClassLoader loader,
        String packagePath,
        String baseClassName,
        Set<String> excludedClassNames,
        String constantFieldName
    ) throws IOException {
        URL packageUrl = loader.getResource(packagePath);
        if (packageUrl == null || !"jar".equals(packageUrl.getProtocol())) {
            throw new IllegalStateException("Expected " + packagePath + " to come from the game JAR");
        }

        Map<String, ClassMetadata> classes = new HashMap<String, ClassMetadata>();
        JarURLConnection connection = (JarURLConnection) packageUrl.openConnection();
        connection.setUseCaches(false);
        try (JarFile jar = connection.getJarFile()) {
            for (JarEntry entry : Collections.list(jar.entries())) {
                if (!isCandidateClass(entry.getName(), packagePath)) {
                    continue;
                }
                ClassMetadata metadata = readMetadata(jar, entry, constantFieldName);
                classes.put(metadata.name, metadata);
            }
        }

        Set<String> ids = new LinkedHashSet<String>();
        for (ClassMetadata metadata : classes.values()) {
            if (metadata.name.equals(baseClassName)
                || excludedClassNames.contains(metadata.name)
                || (metadata.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE)) != 0
                || !isSubclassOf(metadata, baseClassName, classes)) {
                continue;
            }
            if (metadata.id == null || metadata.id.isEmpty()) {
                throw new IllegalStateException(
                    metadata.name.replace('/', '.')
                        + " has no constant public static final String " + constantFieldName
                );
            }
            ids.add(metadata.id);
        }

        List<String> sortedIds = new ArrayList<String>(ids);
        Collections.sort(sortedIds);
        return sortedIds;
    }

    private static boolean isCandidateClass(String entryName, String packagePath) {
        return entryName.startsWith(packagePath)
            && entryName.endsWith(".class")
            && entryName.indexOf('$') < 0;
    }

    private static ClassMetadata readMetadata(
        JarFile jar,
        JarEntry entry,
        final String constantFieldName
    ) throws IOException {
        final ClassMetadata metadata = new ClassMetadata();
        try (InputStream input = jar.getInputStream(entry)) {
            ClassReader reader = new ClassReader(input);
            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(
                    int version,
                    int access,
                    String name,
                    String signature,
                    String superName,
                    String[] interfaces
                ) {
                    metadata.access = access;
                    metadata.name = name;
                    metadata.superName = superName;
                }

                @Override
                public FieldVisitor visitField(
                    int access,
                    String name,
                    String descriptor,
                    String signature,
                    Object value
                ) {
                    if (constantFieldName.equals(name)
                        && "Ljava/lang/String;".equals(descriptor)
                        && (access & REQUIRED_ID_ACCESS) == REQUIRED_ID_ACCESS
                        && value instanceof String) {
                        metadata.id = (String) value;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return metadata;
    }

    private static boolean isSubclassOf(
        ClassMetadata candidate,
        String baseClassName,
        Map<String, ClassMetadata> classes
    ) {
        Set<String> visited = new HashSet<String>();
        String current = candidate.superName;
        while (current != null && visited.add(current)) {
            if (baseClassName.equals(current)) {
                return true;
            }
            ClassMetadata parent = classes.get(current);
            current = parent == null ? null : parent.superName;
        }
        return false;
    }

    private static void validateEnumNames(List<String> ids, String idFieldName) {
        Map<String, String> names = new HashMap<String, String>();
        for (String id : ids) {
            String enumName = enumName(id, idFieldName);
            String previous = names.put(enumName, id);
            if (previous != null) {
                throw new IllegalStateException(
                    "IDs '" + previous + "' and '" + id
                        + "' both map to enum constant " + enumName
                );
            }
        }
    }

    private static void writeCatalog(
        Path output,
        List<String> ids,
        String catalogClassName,
        String generatorClassName,
        String idFieldName,
        String mapFieldName,
        String lookupClassName,
        String lookupIdFieldName
    ) throws IOException {
        Path absoluteOutput = output.toAbsolutePath().normalize();
        Path parent = absoluteOutput.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(absoluteOutput, StandardCharsets.UTF_8)) {
            writer.write("package reallyfastmode.protocol.VanillaCatalog;\n\n");
            writer.write("import java.util.Collections;\n");
            writer.write("import java.util.LinkedHashMap;\n");
            writer.write("import java.util.Map;\n\n");
            writer.write("/** Generated by " + generatorClassName + ". Do not edit by hand. */\n");
            writer.write("public enum " + catalogClassName + " {\n");
            for (int wireId = 0; wireId < ids.size(); wireId++) {
                String id = ids.get(wireId);
                writer.write("    " + enumName(id, idFieldName) + "(" + wireId + ", \"");
                writer.write(escapeJava(id));
                writer.write(wireId + 1 == ids.size() ? "\");\n\n" : "\"),\n");
            }
            writer.write("    public static final Map<String, Integer> " + mapFieldName + ";\n");
            if (lookupClassName != null) {
                writer.write("    public static final int UNKNOWN_WIRE_ID = values().length;\n");
            }
            writer.write("\n");
            writer.write("    static {\n");
            writer.write("        Map<String, Integer> ids = new LinkedHashMap<String, Integer>();\n");
            writer.write("        for (" + catalogClassName + " value : values()) {\n");
            writer.write("            ids.put(value." + idFieldName + ", value.wireId);\n");
            writer.write("        }\n");
            writer.write("        " + mapFieldName + " = Collections.unmodifiableMap(ids);\n");
            writer.write("    }\n\n");
            if (lookupClassName != null) {
                writer.write("    public static int wireId(" + lookupClassName + " value) {\n");
                writer.write("        " + lookupClassName
                    + " checkedValue = java.util.Objects.requireNonNull(value, \"value\");\n");
                writer.write("        Integer wireId = " + mapFieldName
                    + ".get(checkedValue." + lookupIdFieldName + ");\n");
                writer.write("        return wireId == null ? UNKNOWN_WIRE_ID : wireId;\n");
                writer.write("    }\n\n");
            }
            writer.write("    public final int wireId;\n");
            writer.write("    public final String " + idFieldName + ";\n\n");
            writer.write("    " + catalogClassName + "(int wireId, String " + idFieldName + ") {\n");
            writer.write("        this.wireId = wireId;\n");
            writer.write("        this." + idFieldName + " = " + idFieldName + ";\n");
            writer.write("    }\n");
            writer.write("}\n");
        }
    }

    private static String enumName(String id, String idFieldName) {
        StringBuilder name = new StringBuilder();
        boolean previousWasUnderscore = false;
        for (int i = 0; i < id.length(); i++) {
            char character = id.charAt(i);
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
            throw new IllegalArgumentException(idFieldName + " has no enum-name character: " + id);
        }
        if (Character.isDigit(name.charAt(0))) {
            name.insert(0, '_');
        }
        return name.toString().toUpperCase(Locale.ROOT);
    }

    private static String escapeJava(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class ClassMetadata {
        private int access;
        private String name;
        private String superName;
        private String id;
    }
}
