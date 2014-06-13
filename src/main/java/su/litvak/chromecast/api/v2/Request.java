package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

abstract class Request extends Message {
    @JsonProperty
    Long requestId;

    static class Status extends Request {
    }

    static class AppAvailability extends Request {
        @JsonProperty
        final String[] appId;

        AppAvailability(String... appId) {
            this.appId = appId;
        }
    }

    static class Launch extends Request {
        @JsonProperty
        final String appId;

        Launch(String appId) {
            this.appId = appId;
        }
    }

    static Status status() {
        return new Status();
    }

    static AppAvailability appAvailability(String... appId) {
        return new AppAvailability(appId);
    }

    static Launch launch(String appId) {
        return new Launch(appId);
    }
}
