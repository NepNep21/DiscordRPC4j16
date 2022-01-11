package club.bottomservices.discordrpc.lib.pipe;

import club.bottomservices.discordrpc.lib.DiscordPacket;
import club.bottomservices.discordrpc.lib.DiscordRPCClient;
import club.bottomservices.discordrpc.lib.exceptions.NoDiscordException;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class WindowsPipe implements Pipe {
    private final DiscordRPCClient client;

    private RandomAccessFile pipe = null;

    public WindowsPipe(DiscordRPCClient client) {
        this.client = client;

        Logger logger = LoggerFactory.getLogger(WindowsPipe.class);
        for (int i = 0; i < 10; i++) {
            var file = new File("\\\\.\\pipe\\discord-ipc-" + i);
            if (file.exists()) {
                try {
                    pipe = new RandomAccessFile(file, "rw");
                    break;
                } catch (FileNotFoundException e) {
                    logger.info("Discord pipe " + i + " is not writable", e);
                }
            }
        }
        if (pipe == null) {
            throw new NoDiscordException("Discord client not found");
        }
    }

    @Override
    public void write(byte[] data) throws IOException {
        pipe.write(data);
    }

    @Nullable
    @Override
    public DiscordPacket read() throws IOException {
        // Necessary to avoid closing lock due to pipe.read() blocking
        while (client.isConnected && pipe.length() == 0) {
            try {
                Thread.sleep(60);
            } catch (InterruptedException ignored) {
            }
        }

        try {
            var opCode = DiscordPacket.OpCode.values()[Integer.reverseBytes(pipe.readInt())];
            var size = Integer.reverseBytes(pipe.readInt());
            byte[] payload = new byte[size];
            pipe.read(payload);
            String data = new String(payload, StandardCharsets.UTF_8);
            return new DiscordPacket(opCode, DiscordRPCClient.GSON.fromJson(data, JsonObject.class));
            // If a disconnection occurred
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        pipe.close();
    }
}
