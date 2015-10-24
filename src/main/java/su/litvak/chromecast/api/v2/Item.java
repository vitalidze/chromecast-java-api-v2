package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

public class Item {

    public final boolean autoplay;
    public final Object customData;
    public final Media media;
    public final int itemId;

    public Item (@JsonProperty("autoplay") boolean autoplay,
            @JsonProperty("customData") Object customData,
            @JsonProperty("itemId") int itemId,
            @JsonProperty("media") Media media) {
        this.autoplay = autoplay;
        this.customData = customData;
        this.itemId = itemId;
        this.media = media;
    }

}
