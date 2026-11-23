package reallyfastmode.cli;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Standalone Java entry point. The client needs only this JAR and a JRE. */
public final class GameCli {
    private GameCli() {
    }

    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {
        if (args.length == 0 || (args.length == 1 && "--help".equals(args[0]))) {
            printHelp();
            return 0;
        }

        InetSocketAddress address;
        try {
            CliCommand.parse(args);
            address = CliWire.address();
        } catch (IllegalArgumentException exception) {
            System.err.println("invalid_argument\t" + exception.getMessage());
            return 2;
        }

        try (Socket socket = new Socket()) {
            socket.connect(address, 1500);
            socket.setSoTimeout(CliWire.COMMAND_TIMEOUT_MS + 2000);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            CliWire.writeRequest(out, args);
            CliWire.Reply reply = CliWire.readReply(in);
            String message = reply.message.replace('\r', ' ').replace('\n', ' ');
            String output = message.isEmpty() ? reply.code : reply.code + "\t" + message;
            (reply.success ? System.out : System.err).println(output);
            return reply.success ? 0 : 1;
        } catch (IllegalArgumentException exception) {
            System.err.println("invalid_argument\t" + exception.getMessage());
            return 2;
        } catch (IOException exception) {
            System.err.println("connection_error\tCLI at " + address.getHostString() + ":"
                + address.getPort() + ": " + exception.getMessage());
            return 2;
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar ReallyFastMode.jar <method> [arguments ...]");
        for (CliCommand command : CliCommand.values()) {
            System.out.println("  " + command.usage);
        }
    }
}
