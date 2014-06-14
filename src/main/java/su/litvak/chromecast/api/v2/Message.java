package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(name = "PING", value = Message.Ping.class),
               @JsonSubTypes.Type(name = "PONG", value = Message.Pong.class),
               @JsonSubTypes.Type(name = "CONNECT", value = Message.Connect.class),
               @JsonSubTypes.Type(name = "GET_STATUS", value = Request.Status.class),
               @JsonSubTypes.Type(name = "GET_APP_AVAILABILITY", value = Request.AppAvailability.class),
               @JsonSubTypes.Type(name = "LAUNCH", value = Request.Launch.class),
               @JsonSubTypes.Type(name = "STOP", value = Request.Stop.class),
               @JsonSubTypes.Type(name = "LOAD", value = Request.Load.class),
               @JsonSubTypes.Type(name = "PLAY", value = Request.Play.class),
               @JsonSubTypes.Type(name = "PAUSE", value = Request.Pause.class),
               @JsonSubTypes.Type(name = "SET_VOLUME", value = Request.SetVolume.class)})
abstract class Message {
    static class Ping extends Message {}
    static class Pong extends Message {}

    @JsonSerialize
    static class Origin {}

    static class Connect extends Message {
        @JsonProperty
        final Origin origin = new Origin();
    }

    public static Ping ping() {
        return new Ping();
    }

    public static Pong pong() {
        return new Pong();
    }

    public static Connect connect() {
        return new Connect();
    }
}
