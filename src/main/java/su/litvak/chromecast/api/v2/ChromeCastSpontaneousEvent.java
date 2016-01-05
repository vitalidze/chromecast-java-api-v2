package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.JsonNode;

public abstract class ChromeCastSpontaneousEvent {

    public enum SpontaneousEventType {
        MEDIA_STATUS, STATUS, UNKNOWN;
    }

    public static class MediaStatusSpontaneousEvent extends ChromeCastSpontaneousEvent {
        public MediaStatusSpontaneousEvent (final MediaStatus data) {
            super(SpontaneousEventType.MEDIA_STATUS, MediaStatus.class, data);
        }

        public MediaStatus getMediaStatus () {
            return getData(MediaStatus.class);
        }
    }

    public static class StatusSpontaneousEvent extends ChromeCastSpontaneousEvent {
        public StatusSpontaneousEvent (final Status data) {
            super(SpontaneousEventType.STATUS, Status.class, data);
        }

        public Status getStatus () {
            return getData(Status.class);
        }
    }

    public static class UnknownSpontaneousEvent extends ChromeCastSpontaneousEvent {
        public UnknownSpontaneousEvent (final JsonNode data) {
            super(SpontaneousEventType.UNKNOWN, JsonNode.class, data);
        }

        public JsonNode getJson () {
            return getData(JsonNode.class);
        }
    }

    private final SpontaneousEventType type;
    private Class<?> dataClass;
    private final Object data;

    public ChromeCastSpontaneousEvent (final SpontaneousEventType type, Class<?> dataClass, final Object data) {
        this.type = type;
        this.dataClass = dataClass;
        this.data = data;
    }

    public SpontaneousEventType getType () {
        return this.type;
    }

    public Object getData () {
        return this.data;
    }

    public <T> T getData (Class<T> cls) {
        if (!cls.isAssignableFrom(this.dataClass)) {
            throw new IllegalArgumentException("Requested type " + cls + " does not match type for event " + this.dataClass);
        }
        return cls.cast(this.data);
    }

}
