package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

public class Media {
    @JsonProperty("contentId")
    public final String url;
    @JsonProperty
    public final String contentType;
    @JsonProperty
    public String streamType = "buffered";
    @JsonProperty
    public Double duration;

    public Media(@JsonProperty("contentId") String url,
                 @JsonProperty("contentType") String contentType) {
        this.url = url;
        this.contentType = contentType;
    }
}
