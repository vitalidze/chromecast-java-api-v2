package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.Map;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property = "responseType")
@JsonSubTypes({@JsonSubTypes.Type(name = "PING", value = Response.Ping.class),
               @JsonSubTypes.Type(name = "PONG", value = Response.Pong.class),
               @JsonSubTypes.Type(name = "RECEIVER_STATUS", value = Response.Status.class),
               @JsonSubTypes.Type(name = "GET_APP_AVAILABILITY", value = Response.AppAvailability.class),
               @JsonSubTypes.Type(name = "INVALID_REQUEST", value = Response.Invalid.class),
               @JsonSubTypes.Type(name = "MEDIA_STATUS", value = Response.MediaStatus.class),
               @JsonSubTypes.Type(name = "CLOSE", value = Response.Close.class)})
abstract class Response {
    @JsonProperty
    Long requestId;

    static class Ping extends Response {}
    static class Pong extends Response {}
    static class Close extends Response {}

    static class Invalid extends Response {
        final String reason;

        Invalid(@JsonProperty("reason") String reason) {
            this.reason = reason;
        }
    }

    static class Status extends Response {
        final su.litvak.chromecast.api.v2.Status status;

        Status(@JsonProperty("status") su.litvak.chromecast.api.v2.Status status) {
            this.status = status;
        }
    }

    static class MediaStatus extends Response {
        final su.litvak.chromecast.api.v2.MediaStatus statuses[];

        MediaStatus(@JsonProperty("status") su.litvak.chromecast.api.v2.MediaStatus status[]) {
            this.statuses = status;
        }
    }

    static class AppAvailability extends Response {
        @JsonProperty
        Map<String, String> availability;
    }
}
