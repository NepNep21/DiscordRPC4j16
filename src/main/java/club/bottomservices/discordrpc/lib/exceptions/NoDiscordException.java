package club.bottomservices.discordrpc.lib.exceptions;

/**
 * Exception signaling that a connection to a discord pipe could not be established
 * @see DiscordException
 */
public class NoDiscordException extends DiscordException {
    public NoDiscordException(String message) {
        super(message);
    }

    public NoDiscordException(String message, Throwable cause) {
        super(message, cause);
    }
}
