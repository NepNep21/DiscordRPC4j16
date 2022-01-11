package club.bottomservices.discordrpc.lib.exceptions;

/**
 * Exception signaling that an attempt to disconnect or otherwise interact with a closed connection has been made
 * @see DiscordException
 */
public class NotConnectedException extends DiscordException {
    public NotConnectedException(String message) {
        super(message);
    }
}
