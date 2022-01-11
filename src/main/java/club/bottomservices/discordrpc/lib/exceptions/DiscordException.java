package club.bottomservices.discordrpc.lib.exceptions;

/**
 * Exception signaling an error during a discord connection, all discord errors will be thrown as a subclass of this class
 * @see NoDiscordException
 * @see NotConnectedException
 */
public class DiscordException extends RuntimeException {
    public DiscordException(String message) {
        super(message);
    }

    public DiscordException(String message, Throwable cause) {
        super(message, cause);
    }
}
