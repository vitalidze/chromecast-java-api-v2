package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

public class Volume {
    public final float level;
    public final boolean muted;

    public Volume(@JsonProperty("level") float level,
                  @JsonProperty("muted") boolean muted) {
        this.level = level;
        this.muted = muted;
    }
}
