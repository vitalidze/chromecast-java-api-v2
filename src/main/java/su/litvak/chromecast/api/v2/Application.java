package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;

public class Application {
    public final String id;
    public final String name;
    public final String sessionId;
    public final String statusText;
    public final String transportId;
    public final List<Namespace> namespaces;

    public Application(@JsonProperty("appId") String id,
                       @JsonProperty("displayName") String name,
                       @JsonProperty("sessionId") String sessionId,
                       @JsonProperty("statusText") String statusText,
                       @JsonProperty("transportId") String transportId,
                       @JsonProperty("namespaces") List<Namespace> namespaces) {
        this.id = id;
        this.name = name;
        this.sessionId = sessionId;
        this.statusText = statusText;
        this.transportId = transportId;
        this.namespaces = namespaces == null ? Collections.<Namespace>emptyList() : namespaces;
    }
}
