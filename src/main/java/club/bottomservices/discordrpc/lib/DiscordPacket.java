package club.bottomservices.discordrpc.lib;

import com.google.gson.JsonObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Represents a packet of data in communication with discord
 */
public record DiscordPacket(DiscordPacket.OpCode opCode, JsonObject json) {
    /**
     * @return A byte array containing the contents of this packet
     */
    public byte[] toBytes() {
        byte[] jsonBytes = json.toString().getBytes(StandardCharsets.UTF_8);
        int jsonSize = jsonBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(jsonSize + 2 * Integer.BYTES);
        buffer.putInt(Integer.reverseBytes(opCode.ordinal()))
                .putInt(Integer.reverseBytes(jsonSize))
                .put(jsonBytes);
        return buffer.array();
    }

    /**
     * Opcodes used in communication with discord, the ordering of these is important
     */
    public enum OpCode {
        HANDSHAKE,
        MESSAGE,
        CLOSE
    }
}
