package reallyfastmode.cli;

import com.badlogic.gdx.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reallyfastmode.cli.CliWire.Reply;
import reallyfastmode.cli.CliWire.Request;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Receives local CLI requests without doing socket I/O on the render thread. */
public final class GameCliServer {
    private static final Logger LOGGER = LogManager.getLogger(GameCliServer.class.getName());

    private GameCliServer() {
    }

    public static void start(Application application) {
        Thread thread = new Thread(() -> listen(application), "ReallyFastMode CLI");
        thread.setDaemon(true);
        thread.start();
    }

    private static void listen(Application application) {
        try (ServerSocket listener = new ServerSocket()) {
            listener.bind(CliWire.address());
            LOGGER.info("CLI listening on {}", listener.getLocalSocketAddress());
            while (!Thread.currentThread().isInterrupted()) {
                handle(listener.accept(), application);
            }
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.error("Unable to run the local CLI listener.", exception);
        }
    }

    private static void handle(Socket connection, Application application) {
        try (Socket socket = connection) {
            socket.setSoTimeout(2000);
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            Reply reply;
            try {
                reply = invokeOnRenderThread(CliWire.readRequest(in), application);
            } catch (IllegalArgumentException exception) {
                reply = Reply.error("invalid_argument", exception.getMessage());
            }
            CliWire.writeReply(out, reply);
        } catch (IOException exception) {
            LOGGER.debug("CLI connection ended before a reply could be sent.", exception);
        }
    }

    private static Reply invokeOnRenderThread(Request request, Application application) {
        long now = System.currentTimeMillis();
        long deadline = Math.min(request.deadline, now + CliWire.COMMAND_TIMEOUT_MS);
        if (deadline <= now) {
            return timeout();
        }

        FutureTask<Reply> task = new FutureTask<>(() -> System.currentTimeMillis() < deadline
            ? GameCommandDispatcher.invoke(request.args)
            : timeout());
        try {
            application.postRunnable(task);
            return task.get(deadline - now, TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            return timeout();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Reply.error("game_unavailable", "The game stopped accepting commands.");
        } catch (ExecutionException | RuntimeException exception) {
            LOGGER.error("CLI command failed.", exception);
            return Reply.error("internal_error", "The command failed unexpectedly.");
        } finally {
            task.cancel(false);
        }
    }

    private static Reply timeout() {
        return Reply.error("game_timeout", "Timed out waiting for the game. Check the game state before retrying.");
    }
}
