package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class Status {
    final Volume volume;
    final List<Application> applications;
    final boolean activeInput;

    Status(@JsonProperty("volume") Volume volume,
           @JsonProperty("applications") List<Application> applications,
           @JsonProperty("isActiveInput") boolean activeInput) {
        this.volume = volume;
        this.applications = applications;
        this.activeInput = activeInput;
    }
}
