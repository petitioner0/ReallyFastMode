package reallyfastmode.protocol.read;

import reallyfastmode.protocol.RestProtocol;
import reallyfastmode.protocol.VanillaCatalog.VanillaRestOptionCatalog;
import reallyfastmode.protocol.io.BitReader;

import java.util.Arrays;

final class RestBlockDecoder {
    private static final String[] OPTIONS = new String[RestProtocol.AVAILABLE_OPTIONS_MASK_BITS];

    static {
        Arrays.fill(OPTIONS, WireIds.UNKNOWN_ID);
        for (VanillaRestOptionCatalog option : VanillaRestOptionCatalog.values()) {
            if (option != VanillaRestOptionCatalog.UNKNOWN) {
                OPTIONS[option.wireId] = option.name();
            }
        }
    }

    private RestBlockDecoder() {
    }

    static PacketRecord.Rest read(BitReader in) {
        int mask = in.readBits(RestProtocol.AVAILABLE_OPTIONS_MASK_BITS);
        String[] availableOptions = new String[Integer.bitCount(mask)];
        int index = 0;
        for (int wireId = 0; wireId < OPTIONS.length; wireId++) {
            if ((mask & (1 << wireId)) != 0) {
                availableOptions[index++] = OPTIONS[wireId];
            }
        }
        return new PacketRecord.Rest(availableOptions);
    }
}
