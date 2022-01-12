package club.bottomservices.discordrpc.lib;

import club.bottomservices.discordrpc.lib.exceptions.DiscordException;
import club.bottomservices.discordrpc.lib.exceptions.NotConnectedException;
import club.bottomservices.discordrpc.lib.pipe.Pipe;
import club.bottomservices.discordrpc.lib.pipe.UnixPipe;
import club.bottomservices.discordrpc.lib.pipe.WindowsPipe;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.AsynchronousCloseException;

/**
 * The entrypoint of this library
 */
public class DiscordRPCClient {
    /**
     * The {@link Gson} instance used for json (de)serialization by this library
     */
    @Nonnull
    public static final Gson GSON = new Gson();

    private final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    private final String appId;

    private volatile Pipe pipe = null;

    /**
     * Whether a connection to discord is currently open.
     * It is false on construction and may be true after a {@link DiscordRPCClient#connect()} call.
     * Modifying this yourself is probably a bad idea, but it's possible if you find it necessary
     */
    public volatile boolean isConnected = false;
    @Nullable
    public volatile EventListener listeners;

    /**
     * Constructs a new instance of this class
     *
     * @param listeners The event listeners to be used in this client
     * @param appId     Your discord application id
     */
    public DiscordRPCClient(@Nullable EventListener listeners, @Nonnull String appId) {
        this.listeners = listeners;
        this.appId = appId;
    }

    /**
     * Constructs a new instance of this class with no event listeners
     *
     * @param appId     Your discord application id
     */
    public DiscordRPCClient(@Nonnull String appId) {
        this(null, appId);
    }

    private void close() {
        isConnected = false;
        try {
            pipe.close();
            // Agony
            var listeners = this.listeners;
            if (listeners != null) {
                listeners.onClose(this);
            }
        } catch (IOException e) {
            throw new DiscordException("Failed to close DiscordRPC pipe, something may go very wrong", e);
        }
    }

    private void sendMessage(JsonObject message) {
        if (!isConnected) {
            throw new NotConnectedException("Tried to send message");
        }

        try {
            pipe.send(DiscordPacket.OpCode.MESSAGE, message);
        } catch (IOException e) {
            var listeners = this.listeners;
            if (listeners != null) {
                listeners.onError(this, e, null);
            }
        }
    }

    /**
     * Attempts to open a connection to discord.
     * It is the caller's responsibility to check for {@link DiscordRPCClient#isConnected} before calling this.
     *
     * @throws club.bottomservices.discordrpc.lib.exceptions.NoDiscordException if a connection could not be opened
     */
    public void connect() {
        pipe = isWindows ? new WindowsPipe(this) : new UnixPipe();
        try {
            var output = new JsonObject();
            output.addProperty("v", 1);
            output.addProperty("client_id", appId);
            pipe.send(DiscordPacket.OpCode.HANDSHAKE, output);
            isConnected = true;
            new Thread(() -> {
                while (isConnected) {
                    try {
                        DiscordPacket packet = pipe.read();

                        // If a disconnection occurs, windows specific jank
                        if (packet == null) {
                            break;
                        }

                        if (packet.opCode() == DiscordPacket.OpCode.CLOSE) {
                            close();
                            break;
                        }

                        var json = packet.json();

                        JsonElement evtJson = json.get("evt");
                        if (evtJson != null && !evtJson.isJsonNull()) {
                            var data = json.get("data").getAsJsonObject();

                            var listeners = this.listeners;
                            if (listeners != null) {
                                switch (evtJson.getAsString()) {
                                    case "READY" -> listeners.onReady(this, GSON.fromJson(data.get("user"), User.class));
                                    case "ERROR" -> listeners.onError(this, null, GSON.fromJson(data, ErrorEvent.class));
                                    case "ACTIVITY_JOIN" -> listeners.onActivityJoin(this, data.get("secret").getAsString());
                                    case "ACTIVITY_SPECTATE" -> listeners.onActivitySpectate(this, data.get("secret").getAsString());
                                    case "ACTIVITY_JOIN_REQUEST" -> listeners.onActivityJoinRequest(this, GSON.fromJson(data.get("user"), User.class));
                                }
                            }
                        }
                        // Disconnection
                    } catch (AsynchronousCloseException | BufferUnderflowException e) {
                        isConnected = false;
                        break;
                    } catch (IOException e) {
                        var listeners = this.listeners;
                        if (listeners != null) {
                            listeners.onError(this, e, null);
                        }
                        close();
                    }
                }
            }, "DiscordRPC Read Thread").start();
        } catch (IOException e) {
            var listeners = this.listeners;
            if (listeners != null) {
                listeners.onError(this, e, null);
            }
            close();
        }
    }

    /**
     * @param presence The {@link RichPresence} instance to send to discord
     * @see RichPresence.Builder
     */
    public void sendPresence(@Nonnull RichPresence presence) {
        var output = new JsonObject();
        output.addProperty("cmd", "SET_ACTIVITY");

        var args = new JsonObject();
        args.addProperty("pid", ProcessHandle.current().pid());
        args.add("activity", GSON.toJsonTree(presence));
        output.add("args", args);
        sendMessage(output);
    }

    /**
     * Closes a connection to discord
     *
     * @throws NotConnectedException If a connection was not open
     * @throws DiscordException      If closing the pipe fails
     */
    public void disconnect() {
        if (!isConnected) {
            throw new NotConnectedException("Tried to disconnect");
        }

        close();
    }

    /**
     * Responds to an activity join request
     *
     * @param userId   The id of the user to respond to
     * @param accepted Whether this should accept the request
     */
    public void respond(String userId, boolean accepted) {
        var output = new JsonObject();
        output.addProperty("cmd", accepted ? "SEND_ACTIVITY_JOIN_INVITE" : "CLOSE_ACTIVITY_REQUEST");
        output.addProperty("user_id", userId);
        sendMessage(output);
    }
}
