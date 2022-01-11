package club.bottomservices.discordrpc.lib;

/**
 * Represents an ERROR event from discord
 */
public class ErrorEvent {
    public int code;
    public String message;

    // For deserialization
    ErrorEvent() {
    }
}
