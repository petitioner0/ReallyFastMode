package reallyfastmode.protocol.build;

import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption;
import reallyfastmode.access.RestAccess;
import reallyfastmode.protocol.RestProtocol;

import java.util.List;
import java.util.Objects;

/** Captures currently usable campfire options in their displayed order. */
public final class RestSnapshotBuilder {
    private RestSnapshotBuilder() {
    }

    public static RestSnapshot build() {
        return buildFromSource(new StsSource());
    }

    static RestSnapshot buildFromSource(Source source) {
        Objects.requireNonNull(source, "source");

        int optionEntryCount = source.optionEntryCount();
        if (optionEntryCount < 0) {
            throw new IllegalArgumentException("optionEntryCount=" + optionEntryCount);
        }

        boolean[] usability = new boolean[optionEntryCount];
        int usableCount = 0;
        for (int i = 0; i < optionEntryCount; i++) {
            usability[i] = source.usable(i);
            if (usability[i]) {
                usableCount++;
            }
        }

        RestSnapshot result = new RestSnapshot(usableCount);
        int resultIndex = 0;
        for (int i = 0; i < optionEntryCount; i++) {
            if (!usability[i]) {
                continue;
            }
            int wireId = source.optionWireId(i);
            requireUnsigned("optionsWireId[" + resultIndex + "]", wireId,
                RestProtocol.OPTION_WIRE_ID_BITS);
            result.optionsWireId[resultIndex++] = wireId;
        }
        return result;
    }

    private static void requireUnsigned(String name, int value, int bits) {
        int max = (1 << bits) - 1;
        if (value < 0 || value > max) {
            throw new IllegalArgumentException(name + "=" + value + " outside 0-" + max);
        }
    }

    interface Source {
        int optionEntryCount();

        int optionWireId(int optionIndex);

        boolean usable(int optionIndex);
    }

    private static final class StsSource implements Source {
        private final List<AbstractCampfireOption> options;

        private StsSource() {
            this.options = RestAccess.options();
        }

        @Override
        public int optionEntryCount() {
            return options.size();
        }

        @Override
        public int optionWireId(int optionIndex) {
            return RestAccess.wireId(option(optionIndex));
        }

        @Override
        public boolean usable(int optionIndex) {
            return RestAccess.usable(option(optionIndex));
        }

        private AbstractCampfireOption option(int index) {
            return Objects.requireNonNull(options.get(index), "options[" + index + "]");
        }
    }
}
