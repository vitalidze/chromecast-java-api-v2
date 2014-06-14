package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

abstract class Request extends Message {
    @JsonProperty
    Long requestId;

    static class Status extends Request {}

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

    static class Stop extends Request {
        @JsonProperty
        final String sessionId;

        Stop(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    static class Load extends Request {
        @JsonProperty
        final String sessionId;
        @JsonProperty
        final Media media;
        @JsonProperty
        final boolean autoplay;
        @JsonProperty
        final long currentTime;
        @JsonProperty
        final Object customData;

        Load(String sessionId, Media media, boolean autoplay, long currentTime, final Map<String, String> customData) {
            this.sessionId = sessionId;
            this.media = media;
            this.autoplay = autoplay;
            this.currentTime = currentTime;

            this.customData = customData == null ? null : new Object() {
                @JsonProperty
                Map<String, String> payload = customData;
            };
        }
    }

    abstract static class MediaRequest extends Request {
        @JsonProperty
        final long mediaSessionId;
        @JsonProperty
        final String sessionId;

        MediaRequest(long mediaSessionId, String sessionId) {
            this.mediaSessionId = mediaSessionId;
            this.sessionId = sessionId;
        }
    }

    static class Play extends MediaRequest {
        Play(long mediaSessionId, String sessionId) {
            super(mediaSessionId, sessionId);
        }
    }

    static class Pause extends MediaRequest {
        Pause(long mediaSessionId, String sessionId) {
            super(mediaSessionId, sessionId);
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

    static Stop stop(String sessionId) {
        return new Stop(sessionId);
    }

    static Load load(String sessionId, Media media, boolean autoplay, long currentTime, Map<String, String> customData) {
        return new Load(sessionId, media, autoplay, currentTime, customData);
    }

    static Play play(String sessionId, long mediaSessionId) {
        return new Play(mediaSessionId, sessionId);
    }

    static Pause pause(String sessionId, long mediaSessionId) {
        return new Pause(mediaSessionId, sessionId);
    }
}
