package club.bottomservices.discordrpc.lib.pipe;

import club.bottomservices.discordrpc.lib.DiscordPacket;
import club.bottomservices.discordrpc.lib.DiscordRPCClient;
import club.bottomservices.discordrpc.lib.exceptions.NoDiscordException;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class UnixPipe implements Pipe {
    private final SocketChannel socket;

    public UnixPipe() {
        Logger logger = LoggerFactory.getLogger(UnixPipe.class);
        try {
            socket = SocketChannel.open(StandardProtocolFamily.UNIX);
        } catch (IOException e) {
            throw new NoDiscordException("Failed to open socket", e);
        }

        String[] locations = {"XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP"};
        String location = null;

        for (var possible : locations) {
            location = System.getenv(possible);
            if (location != null) {
                break;
            }
        }

        if (location == null) {
            location = "/tmp";
        }

        for (int i = 0; i < 10; i++) {
            var file = new File(location + "/discord-ipc-" + i);
            if (file.exists()) {
                try {
                    socket.connect(UnixDomainSocketAddress.of(file.getCanonicalPath()));
                    break;
                } catch (IOException e) {
                    logger.info("IOException while binding socket " + i, e);
                }
            }
        }

        if (!socket.isConnected()) {
            throw new NoDiscordException("Discord client not found");
        }
    }

    @Override
    public void write(byte[] data) throws IOException {
        socket.write(ByteBuffer.wrap(data));
    }

    @Nonnull
    @Override
    public DiscordPacket read() throws IOException {
        var header = ByteBuffer.allocate(2 * Integer.BYTES);
        socket.read(header);

        header.flip();
        var opCode = DiscordPacket.OpCode.values()[Integer.reverseBytes(header.getInt())];
        int size = Integer.reverseBytes(header.getInt());
        var payload = ByteBuffer.allocate(size);
        socket.read(payload);
        return new DiscordPacket(opCode, DiscordRPCClient.GSON.fromJson(new String(payload.flip().array(), StandardCharsets.UTF_8), JsonObject.class));
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
