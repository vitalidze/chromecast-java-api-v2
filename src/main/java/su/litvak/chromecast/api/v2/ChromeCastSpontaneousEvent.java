package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.JsonNode;

public class ChromeCastSpontaneousEvent {

    public enum SpontaneousEventType {

        /**
         * Data type will be {@link MediaStatus}.
         */
        MEDIA_STATUS(MediaStatus.class),

        /**
         * Data type will be {@link Status}.
         */
        STATUS(Status.class),

        /**
         * Data type will be {@link org.codehaus.jackson.JsonNode}.
         */
        UNKNOWN(JsonNode.class);

        private final Class<?> dataClass;

        private SpontaneousEventType (Class<?> dataClass) {
            this.dataClass = dataClass;
        }

        public Class<?> getDataClass () {
            return this.dataClass;
        }
    }

    private final SpontaneousEventType type;
    private final Object data;

    public ChromeCastSpontaneousEvent (final SpontaneousEventType type, final Object data) {
        if (!type.getDataClass().isAssignableFrom(data.getClass())) {
            throw new IllegalArgumentException("Data type " + data.getClass() + " does not match type for event " + this.type.getDataClass());
        }
        this.type = type;
        this.data = data;
    }

    public SpontaneousEventType getType () {
        return this.type;
    }

    public Object getData () {
        return this.data;
    }

    public <T> T getData (Class<T> cls) {
        if (!cls.isAssignableFrom(this.type.getDataClass())) {
            throw new IllegalArgumentException("Requested type " + cls + " does not match type for event " + this.type.getDataClass());
        }
        return cls.cast(this.data);
    }

}
