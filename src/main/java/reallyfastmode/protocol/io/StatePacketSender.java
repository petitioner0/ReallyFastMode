package reallyfastmode.protocol.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/** Sends each encoded state packet as one non-blocking loopback datagram. */
public final class StatePacketSender {
    private static final Logger LOGGER = LogManager.getLogger(StatePacketSender.class.getName());
    private static final String PORT_PROPERTY = "reallyfastmode.state.port";
    private static final String DEFAULT_PORT = "18776";
    private static final StatePacketSender INSTANCE = open();

    private final DatagramChannel channel;
    private final InetSocketAddress destination;

    private StatePacketSender(DatagramChannel channel, InetSocketAddress destination) {
        this.channel = channel;
        this.destination = destination;
    }

    public static void send(byte[] packet) {
        if (INSTANCE == null) {
            return;
        }
        try {
            if (INSTANCE.channel.send(ByteBuffer.wrap(packet), INSTANCE.destination) == 0) {
                LOGGER.warn("The local UDP socket could not send the state packet to {}.", INSTANCE.destination);
            }
        } catch (IOException exception) {
            LOGGER.warn("Unable to send the state packet to {}.", INSTANCE.destination, exception);
        }
    }

    private static StatePacketSender open() {
        DatagramChannel channel = null;
        try {
            int port = Integer.parseInt(System.getProperty(PORT_PROPERTY, DEFAULT_PORT));
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException(PORT_PROPERTY + " must be between 1 and 65535.");
            }
            InetSocketAddress destination = new InetSocketAddress("127.0.0.1", port);
            channel = DatagramChannel.open(StandardProtocolFamily.INET);
            channel.configureBlocking(false);
            return new StatePacketSender(channel, destination);
        } catch (IOException | IllegalArgumentException exception) {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException closeException) {
                    exception.addSuppressed(closeException);
                }
            }
            LOGGER.error("Unable to initialize the local state packet sender.", exception);
            return null;
        }
    }
}
