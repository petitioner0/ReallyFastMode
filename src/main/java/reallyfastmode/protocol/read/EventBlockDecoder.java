package reallyfastmode.protocol.read;

import reallyfastmode.protocol.EventProtocol;
import reallyfastmode.protocol.io.BitReader;

final class EventBlockDecoder {
    private EventBlockDecoder() {
    }

    static PacketRecord.Event read(BitReader in) {
        String eventId = WireIds.lookup(WireIds.EVENTS, in.readBits(EventProtocol.EVENT_WIRE_ID_BITS));
        int count = in.readBits(EventProtocol.OPTIONS_ENTRY_COUNT_BITS);
        int[] optionsApplicability = new int[count];
        for (int i = 0; i < optionsApplicability.length; i++) {
            optionsApplicability[i] = in.readBits(EventProtocol.OPTIONS_APPLICABILITY_BITS);
        }
        return new PacketRecord.Event(eventId, optionsApplicability);
    }
}
