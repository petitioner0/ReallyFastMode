package reallyfastmode.temporary.randomrun;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

/** Wall-clock and process CPU-time measurements for one temporary run. */
final class RunPerformanceMetrics {
    private static final OperatingSystemMXBean OPERATING_SYSTEM = operatingSystemBean();

    private RunPerformanceMetrics() {
    }

    static Snapshot start() {
        long processCpuNanos = OPERATING_SYSTEM.getProcessCpuTime();
        if (processCpuNanos < 0L) {
            throw new IllegalStateException("Process CPU time is unavailable on this JVM.");
        }
        return new Snapshot(
            System.nanoTime(),
            processCpuNanos
        );
    }

    static Result finish(Snapshot start) {
        if (start == null) {
            throw new IllegalStateException("Run performance measurement was not started.");
        }
        long wallNanos = System.nanoTime() - start.wallNanos;
        long processCpuNanos = OPERATING_SYSTEM.getProcessCpuTime() - start.processCpuNanos;
        return summarize(wallNanos, processCpuNanos);
    }

    static Result summarize(long wallNanos, long processCpuNanos) {
        if (wallNanos < 0L || processCpuNanos < 0L) {
            throw new IllegalArgumentException("Invalid run performance measurement.");
        }

        return new Result(
            TimeUnit.NANOSECONDS.toMillis(wallNanos),
            TimeUnit.NANOSECONDS.toMillis(processCpuNanos)
        );
    }

    private static OperatingSystemMXBean operatingSystemBean() {
        java.lang.management.OperatingSystemMXBean bean =
            ManagementFactory.getOperatingSystemMXBean();
        if (!(bean instanceof OperatingSystemMXBean)) {
            throw new IllegalStateException(
                "This JVM does not expose process CPU time through OperatingSystemMXBean."
            );
        }
        return (OperatingSystemMXBean) bean;
    }

    static final class Snapshot {
        private final long wallNanos;
        private final long processCpuNanos;

        private Snapshot(long wallNanos, long processCpuNanos) {
            this.wallNanos = wallNanos;
            this.processCpuNanos = processCpuNanos;
        }
    }

    static final class Result {
        private final long elapsedMillis;
        private final long processCpuTimeMillis;

        private Result(long elapsedMillis, long processCpuTimeMillis) {
            this.elapsedMillis = elapsedMillis;
            this.processCpuTimeMillis = processCpuTimeMillis;
        }

        long elapsedMillis() {
            return elapsedMillis;
        }

        long processCpuTimeMillis() {
            return processCpuTimeMillis;
        }
    }
}
