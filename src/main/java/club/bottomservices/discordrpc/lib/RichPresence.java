package club.bottomservices.discordrpc.lib;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record RichPresence(@Nullable String state,
                           @Nullable String details,
                           @Nullable Timestamps timestamps,
                           @Nullable Assets assets,
                           @Nullable Party party,
                           @Nullable Secrets secrets,
                           @Nullable List<Button> buttons) {

    public record Button(@Nonnull String label, @Nonnull String url) {
    }

    public record Timestamps(@Nullable Long start, @Nullable Long end) {
    }

    public record Assets(@Nullable @SerializedName("large_image") String largeImage,
                         @Nullable @SerializedName("large_text") String largeText,
                         @Nullable @SerializedName("small_image") String smallImage,
                         @Nullable @SerializedName("small_text") String smallText) {
    }

    public record Party(@Nonnull String id, int[] size) {
    }

    public record Secrets(@Nullable String join, @Nullable String spectate, @Nullable String match) {
    }

    public static class Builder {
        public String state = null;
        public String details = null;

        public Long start = null;
        public Long end = null;

        public String largeImage = null;
        public String largeText = null;
        public String smallImage = null;
        public String smallText = null;

        public String  id = null;
        public Integer size = null;
        public Integer max = null;

        public String join = null;
        public String spectate = null;
        public String match = null;

        public List<String> buttons = null;
        public List<String> buttonUrls = null;

        public Builder setText(@Nullable String details, @Nullable String state) {
            this.state = state;
            this.details = details;
            return this;
        }

        /**
         * Unix timestamps (such as from {@link System#currentTimeMillis()} divided by 1000)
         * @return This builder
         */
        public Builder setTimestamps(@Nullable Long start, @Nullable Long end) {
            this.start = start;
            this.end = end;
            return this;
        }

        /**
         * Nullability note: The built {@link RichPresence} instance's assets'
         * largeImage will only be null if its largeText is also null, and vice versa,
         * this applies equivalently to the small values.
         * Never use empty strings as parameters here, always null.
         * @param largeImage Key of the large image used by your discord application
         * @param largeText Hover text of the large image
         * @param smallImage Key of the small image used by your discord application
         * @param smallText Hover text of the small image
         * @return This builder
         */
        public Builder setAssets(@Nullable String largeImage,
                                 @Nullable String largeText,
                                 @Nullable String smallImage,
                                 @Nullable String smallText) {
            this.largeImage = largeImage;
            this.largeText = largeText;
            this.smallImage = smallImage;
            this.smallText = smallText;
            return this;
        }

        /**
         * @param id The party id, should only be null when removing party info
         * @return This builder
         */
        public Builder setPartyInfo(@Nullable String id, int size, int max) {
            this.id = id;
            this.size = size;
            this.max = max;
            return this;
        }

        public Builder setSecrets(@Nullable String join, @Nullable String spectate, @Nullable String match) {
            this.join = join;
            this.spectate = spectate;
            this.match = match;
            return this;
        }

        public Builder addButton(@Nonnull String name, @Nonnull String url) {
            if (buttons == null) {
                buttons = new ArrayList<>();
                buttonUrls = new ArrayList<>();
            }
            buttons.add(name);
            buttonUrls.add(url);
            return this;
        }

        /**
         * Builds a {@link RichPresence} from the data in this builder
         * @throws IllegalArgumentException If any of the image keys or texts was an empty string, discord does not accept those
         * @return The built {@link RichPresence}
         */
        public RichPresence build() {
            var timestamps = new Timestamps(start, end);
            Assets assets = null;
            // Bad code, fix later
            if ((largeImage != null && largeText != null) || (smallImage != null && smallText != null)) {
                if ((largeImage != null && (largeImage.isEmpty() || largeText.isEmpty())) // If largeImage isn't null, so isn't largeText
                        || (smallImage != null && (smallImage.isEmpty() || smallText.isEmpty()))) {
                    throw new IllegalArgumentException("RichPresence must not be built with empty image strings");
                }
                assets = new Assets(largeImage, largeText, smallImage, smallText);
            }

            Party party = null;
            if (id != null && size != null && max != null) {
                party = new Party(id, new int[]{size, max});
            }

            Secrets secrets = null;
            if (join != null || spectate != null || match != null) {
                secrets = new Secrets(join, spectate, match);
            }

            List<Button> buttons = new ArrayList<>();
            if (this.buttons != null) {
                for (int i = 0; i < this.buttons.size(); i++) {
                    buttons.add(new Button(this.buttons.get(i), buttonUrls.get(i)));
                }
            }
            buttons = buttons.isEmpty() ? null : buttons;
            return new RichPresence(state, details, timestamps, assets, party, secrets, buttons);
        }
    }
}
