package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

public class Namespace {
    final String name;

    public Namespace(@JsonProperty("name") String name) {
        this.name = name;
    }
}
