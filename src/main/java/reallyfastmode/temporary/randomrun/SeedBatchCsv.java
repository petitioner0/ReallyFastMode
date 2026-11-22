package reallyfastmode.temporary.randomrun;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.helpers.SeedHelper;
import com.megacrit.cardcrawl.random.Random;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Persistent seed queue for the temporary random-run harness. */
final class SeedBatchCsv {
    static final int SEED_COUNT = 100;
    static final long FIXED_RANDOM_SEED = 0x52464D5345454453L;

    private static final String HEADER =
        "seed,completed,elapsed_ms,process_cpu_time_ms";
    private static final String LEGACY_HEADER = "seed,completed";
    private static final String PERCENTAGE_HEADER =
        "seed,completed,elapsed_ms,process_cpu_percent";
    private static final String DATA_DIRECTORY = "temporary-random-run";
    private static final String DATA_FILE = "seeds.csv";

    private final Path path;
    private final List<Entry> entries;

    private SeedBatchCsv(Path path, List<Entry> entries) {
        this.path = path;
        this.entries = entries;
    }

    static SeedBatchCsv openDefault() throws IOException {
        Path modDirectory = Paths.get(
            SpireConfig.makeFilePath("ReallyFastMode", "config")
        ).getParent();
        if (modDirectory == null) {
            throw new IOException("Unable to resolve the ReallyFastMode config directory.");
        }
        return open(modDirectory.resolve(DATA_DIRECTORY).resolve(DATA_FILE));
    }

    static SeedBatchCsv open(Path path) throws IOException {
        if (Files.exists(path)) {
            ReadResult readResult = readAndValidate(path);
            SeedBatchCsv batch = new SeedBatchCsv(path, readResult.entries);
            if (readResult.requiresMigration) {
                batch.writeAtomically();
            }
            return batch;
        }

        List<Entry> generated = new ArrayList<Entry>(SEED_COUNT);
        for (String seed : generateSeeds()) {
            generated.add(new Entry(seed, false, null, null));
        }
        SeedBatchCsv batch = new SeedBatchCsv(path, generated);
        batch.writeAtomically();
        return batch;
    }

    static List<String> generateSeeds() {
        Random random = new Random(Long.valueOf(FIXED_RANDOM_SEED));
        Set<String> uniqueSeeds = new LinkedHashSet<String>();
        while (uniqueSeeds.size() < SEED_COUNT) {
            long numericSeed = SeedHelper.generateUnoffensiveSeed(random);
            String seed = SeedHelper.getString(numericSeed);
            if (isCanonicalSeed(seed)) {
                uniqueSeeds.add(seed);
            }
        }
        return new ArrayList<String>(uniqueSeeds);
    }

    int firstIncompleteIndex() {
        for (int index = 0; index < entries.size(); ++index) {
            if (!entries.get(index).completed) {
                return index;
            }
        }
        return -1;
    }

    String seedAt(int index) {
        return entries.get(index).seed;
    }

    int size() {
        return entries.size();
    }

    boolean isCompleted(int index) {
        return entries.get(index).completed;
    }

    Long elapsedMillisAt(int index) {
        return entries.get(index).elapsedMillis;
    }

    Long processCpuTimeMillisAt(int index) {
        return entries.get(index).processCpuTimeMillis;
    }

    Path path() {
        return path;
    }

    void markCompleted(int index, long elapsedMillis, long processCpuTimeMillis)
        throws IOException {
        validateMetrics(elapsedMillis, processCpuTimeMillis);
        Entry entry = entries.get(index);
        if (entry.completed) {
            return;
        }

        entry.completed = true;
        entry.elapsedMillis = Long.valueOf(elapsedMillis);
        entry.processCpuTimeMillis = Long.valueOf(processCpuTimeMillis);
        try {
            writeAtomically();
        } catch (IOException exception) {
            entry.completed = false;
            entry.elapsedMillis = null;
            entry.processCpuTimeMillis = null;
            throw exception;
        }
    }

    private static ReadResult readAndValidate(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        boolean legacyFormat = !lines.isEmpty() && LEGACY_HEADER.equals(lines.get(0));
        boolean percentageFormat =
            !lines.isEmpty() && PERCENTAGE_HEADER.equals(lines.get(0));
        if (lines.size() != SEED_COUNT + 1
            || !(HEADER.equals(lines.get(0)) || legacyFormat || percentageFormat)) {
            throw new IOException(
                "Seed CSV must contain header '" + HEADER + "' and exactly "
                    + SEED_COUNT + " data rows: " + path
            );
        }

        Set<String> seen = new LinkedHashSet<String>();
        List<Entry> result = new ArrayList<Entry>(SEED_COUNT);
        for (int lineIndex = 1; lineIndex < lines.size(); ++lineIndex) {
            String[] columns = lines.get(lineIndex).split(",", -1);
            int expectedColumns = legacyFormat ? 2 : 4;
            if (columns.length != expectedColumns
                || !isCanonicalSeed(columns[0])
                || !(columns[1].isEmpty() || "1".equals(columns[1]))) {
                throw new IOException("Invalid seed CSV row " + (lineIndex + 1) + ": " + path);
            }
            if (!seen.add(columns[0])) {
                throw new IOException("Duplicate seed in CSV row " + (lineIndex + 1) + ": " + path);
            }
            boolean completed = "1".equals(columns[1]);
            Long elapsedMillis = null;
            Long processCpuTimeMillis = null;
            if (!legacyFormat && (!columns[2].isEmpty() || !columns[3].isEmpty())) {
                if (!completed || columns[2].isEmpty()) {
                    throw new IOException(
                        "Invalid seed CSV metrics in row " + (lineIndex + 1) + ": " + path
                    );
                }
                try {
                    elapsedMillis = Long.valueOf(columns[2]);
                    if (percentageFormat) {
                        validateLegacyPercentage(columns[3]);
                    } else if (!columns[3].isEmpty()) {
                        processCpuTimeMillis = Long.valueOf(columns[3]);
                    }
                    validateOptionalMetrics(elapsedMillis, processCpuTimeMillis);
                } catch (IllegalArgumentException exception) {
                    throw new IOException(
                        "Invalid seed CSV metrics in row " + (lineIndex + 1) + ": " + path,
                        exception
                    );
                }
            }
            result.add(new Entry(
                columns[0],
                completed,
                elapsedMillis,
                processCpuTimeMillis
            ));
        }
        return new ReadResult(result, legacyFormat || percentageFormat);
    }

    private static void validateMetrics(long elapsedMillis, long processCpuTimeMillis) {
        if (elapsedMillis < 0L || processCpuTimeMillis < 0L) {
            throw new IllegalArgumentException("Invalid run performance metrics.");
        }
    }

    private static void validateOptionalMetrics(
        Long elapsedMillis,
        Long processCpuTimeMillis
    ) {
        if (elapsedMillis != null && elapsedMillis.longValue() < 0L
            || processCpuTimeMillis != null && processCpuTimeMillis.longValue() < 0L) {
            throw new IllegalArgumentException("Invalid run performance metrics.");
        }
    }

    private static void validateLegacyPercentage(String value) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Missing legacy process CPU percentage.");
        }
        double percentage = Double.parseDouble(value);
        if (Double.isNaN(percentage)
            || Double.isInfinite(percentage)
            || percentage < 0.0D
            || percentage > 100.0D) {
            throw new IllegalArgumentException("Invalid legacy process CPU percentage.");
        }
    }

    private static boolean isCanonicalSeed(String seed) {
        if (seed == null
            || seed.isEmpty()
            || seed.length() > SeedHelper.SEED_DEFAULT_LENGTH
            || !seed.equals(SeedHelper.sterilizeString(seed))) {
            return false;
        }
        try {
            return seed.equals(SeedHelper.getString(SeedHelper.getLong(seed)));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private void writeAtomically() throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent == null) {
            throw new IOException("Seed CSV has no parent directory: " + path);
        }
        Files.createDirectories(parent);

        List<String> lines = new ArrayList<String>(entries.size() + 1);
        lines.add(HEADER);
        for (Entry entry : entries) {
            String elapsedMillis = entry.elapsedMillis == null
                ? ""
                : entry.elapsedMillis.toString();
            String processCpuTimeMillis = entry.processCpuTimeMillis == null
                ? ""
                : entry.processCpuTimeMillis.toString();
            lines.add(
                entry.seed + "," + (entry.completed ? "1" : "") + ","
                    + elapsedMillis + "," + processCpuTimeMillis
            );
        }

        Path temporary = Files.createTempFile(parent, "seeds-", ".tmp");
        boolean moved = false;
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try {
                Files.move(
                    temporary,
                    path.toAbsolutePath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(
                    temporary,
                    path.toAbsolutePath(),
                    StandardCopyOption.REPLACE_EXISTING
                );
            }
            moved = true;
        } finally {
            if (!moved) {
                Files.deleteIfExists(temporary);
            }
        }
    }

    private static final class Entry {
        private final String seed;
        private boolean completed;
        private Long elapsedMillis;
        private Long processCpuTimeMillis;

        private Entry(
            String seed,
            boolean completed,
            Long elapsedMillis,
            Long processCpuTimeMillis
        ) {
            this.seed = seed;
            this.completed = completed;
            this.elapsedMillis = elapsedMillis;
            this.processCpuTimeMillis = processCpuTimeMillis;
        }
    }

    private static final class ReadResult {
        private final List<Entry> entries;
        private final boolean requiresMigration;

        private ReadResult(List<Entry> entries, boolean requiresMigration) {
            this.entries = entries;
            this.requiresMigration = requiresMigration;
        }
    }
}
