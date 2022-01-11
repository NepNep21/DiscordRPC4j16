package club.bottomservices.discordrpc.lib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Listener for discord events
 */
public interface EventListener {
    /**
     * Fired when discord finishes connecting the client
     * @param user The {@link User} of the connected client
     */
    default void onReady(@Nonnull DiscordRPCClient client, @Nonnull User user) {}

    /**
     * Fired when an error occurs, may be followed by {@link EventListener#onClose(DiscordRPCClient)}
     * @param exception {@link IOException} that caused this event, null if from an ERROR discord event
     * @param event {@link ErrorEvent} containing information about an ERROR discord event, null if from a library {@link IOException}
     */
    default void onError(@Nonnull DiscordRPCClient client, @Nullable IOException exception, @Nullable ErrorEvent event) {}

    /**
     * Fired when the connection is closed, may follow {@link EventListener#onError(DiscordRPCClient, IOException, ErrorEvent)}
     */
    default void onClose(@Nonnull DiscordRPCClient client) {}

    default void onActivityJoin(@Nonnull DiscordRPCClient client, @Nonnull String secret) {}
    default void onActivitySpectate(@Nonnull DiscordRPCClient client, @Nonnull String secret) {}
    default void onActivityJoinRequest(@Nonnull DiscordRPCClient client, @Nonnull User user) {}
}
