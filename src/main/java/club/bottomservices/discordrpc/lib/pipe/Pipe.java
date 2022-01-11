package club.bottomservices.discordrpc.lib.pipe;

import club.bottomservices.discordrpc.lib.DiscordPacket;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

/**
 * Interface for abstracting the platform dependent nature of discord pipes
 * @see UnixPipe
 */
public interface Pipe extends Closeable {
    /**
     * Adds a nonce to a {@link JsonObject},
     * and writes a {@link DiscordPacket} constructed from it and an opcode to the underlying pipe through {@link Pipe#write(byte[])}
     */
    default void send(@Nonnull DiscordPacket.OpCode opCode, @Nonnull JsonObject json) throws IOException {
        json.addProperty("nonce", UUID.randomUUID().toString());
        write(new DiscordPacket(opCode, json).toBytes());
    }

    /**
     * Writes the given data to the underlying pipe
     */
    void write(byte[] data) throws IOException;

    /**
     * Reads a {@link DiscordPacket} from the underlying pipe, this method blocks until enough data has been read
     */
    @Nullable
    DiscordPacket read() throws IOException;
}
