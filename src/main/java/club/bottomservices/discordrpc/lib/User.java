package club.bottomservices.discordrpc.lib;

/**
 * Represents the user from the connected discord client
 */
public class User {
    public String id;
    public String username;
    public String discriminator;
    public String avatar;

    // For deserialization
    User() {
    }
}
