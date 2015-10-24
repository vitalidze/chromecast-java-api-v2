package su.litvak.chromecast.api.v2;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class Item {

    public final boolean autoplay;
    public final Map<String, Object> customData;
    public final Media media;
    public final long id;

    public Item (@JsonProperty("autoplay") boolean autoplay,
            @JsonProperty("customData") Map<String, Object> customData,
            @JsonProperty("itemId") long id,
            @JsonProperty("media") Media media) {
        this.autoplay = autoplay;
        this.customData = customData != null ? Collections.unmodifiableMap(customData) : null;
        this.id = id;
        this.media = media;
    }

    @Override
    public int hashCode () {
        return Arrays.hashCode(new Object[] { this.autoplay, this.customData, this.id, this.media });
    }

    @Override
    public boolean equals (final Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Item)) return false;
        final Item that = (Item) obj;
        return this.autoplay == that.autoplay &&
                this.customData == null ? that.customData == null : this.customData.equals(that.customData) &&
                this.id == that.id &&
                this.media == null ? that.media == null : this.media.equals(that.media);
    }

    @Override
    public String toString () {
        return String.format("Item{%s, %s}", this.id, this.media);
    }

}
