package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;

public class Status {
    final Volume volume;
    final List<Application> applications;
    final boolean activeInput;

    Status(@JsonProperty("volume") Volume volume,
           @JsonProperty("applications") List<Application> applications,
           @JsonProperty("isActiveInput") boolean activeInput) {
        this.volume = volume;
        this.applications = applications == null ? Collections.<Application>emptyList() : applications;
        this.activeInput = activeInput;
    }

    public Application getRunningApp() {
        return applications.isEmpty() ? null : applications.get(0);
    }
}
