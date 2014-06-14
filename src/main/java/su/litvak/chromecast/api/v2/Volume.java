package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

public class Volume {
    @JsonProperty
    public final float level;
    @JsonProperty
    public final boolean muted;

    public Volume(@JsonProperty("level") float level,
                  @JsonProperty("muted") boolean muted) {
        this.level = level;
        this.muted = muted;
    }
}
