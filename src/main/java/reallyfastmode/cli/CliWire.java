package reallyfastmode.cli;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

/** Local framing for one CLI invocation and its result. */
final class CliWire {
    static final int MAX_ARGUMENTS = 1024;
    static final int COMMAND_TIMEOUT_MS = 10000;
    private static final int MAX_ARGUMENT_LENGTH = 1024;
    private static final String PORT_PROPERTY = "reallyfastmode.cli.port";
    private static final String DEFAULT_PORT = "18775";

    private CliWire() {
    }

    static InetSocketAddress address() {
        String value = System.getProperty(PORT_PROPERTY, DEFAULT_PORT);
        int port = Integer.parseInt(value);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(PORT_PROPERTY + " must be between 1 and 65535.");
        }
        return new InetSocketAddress("127.0.0.1", port);
    }

    static void writeRequest(DataOutputStream out, String[] args) throws IOException {
        out.writeLong(System.currentTimeMillis() + COMMAND_TIMEOUT_MS);
        out.writeInt(args.length);
        for (String arg : args) {
            out.writeUTF(checkedArgument(arg));
        }
        out.flush();
    }

    static Request readRequest(DataInputStream in) throws IOException {
        long deadline = in.readLong();
        int count = in.readInt();
        if (count < 1 || count > MAX_ARGUMENTS) {
            throw new IllegalArgumentException("Invalid command argument count.");
        }
        String[] args = new String[count];
        for (int i = 0; i < count; i++) {
            args[i] = checkedArgument(in.readUTF());
        }
        return new Request(args, deadline);
    }

    private static String checkedArgument(String arg) {
        if (arg.length() > MAX_ARGUMENT_LENGTH) {
            throw new IllegalArgumentException("Command argument is too long.");
        }
        return arg;
    }

    static void writeReply(DataOutputStream out, Reply reply) throws IOException {
        out.writeBoolean(reply.success);
        out.writeUTF(reply.code);
        out.writeUTF(reply.message == null ? "" : reply.message);
        out.flush();
    }

    static Reply readReply(DataInputStream in) throws IOException {
        return new Reply(in.readBoolean(), in.readUTF(), in.readUTF());
    }

    static final class Request {
        final String[] args;
        final long deadline;

        Request(String[] args, long deadline) {
            this.args = args;
            this.deadline = deadline;
        }
    }

    static final class Reply {
        final boolean success;
        final String code;
        final String message;

        Reply(boolean success, String code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
        }

        static Reply ok() {
            return new Reply(true, "ok", "");
        }

        static Reply error(String code, String message) {
            return new Reply(false, code, message);
        }
    }
}
