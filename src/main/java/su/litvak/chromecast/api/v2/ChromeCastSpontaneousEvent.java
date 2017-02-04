/*
 * Copyright 2014 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.JsonNode;

public class ChromeCastSpontaneousEvent {

    public enum SpontaneousEventType {

        /**
         * Data type will be {@link Boolean}.
         */
        CONNECTION_STATUS(Boolean.class),

        /**
         * Data type will be {@link MediaStatus}.
         */
        MEDIA_STATUS(MediaStatus.class),

        /**
         * Data type will be {@link Status}.
         */
        STATUS(Status.class),

        /**
         * Data type will be {@link AppEvent}.
         */
        APPEVENT(AppEvent.class),

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
            throw new IllegalArgumentException("Data type " + data.getClass() + " does not match type for event " + type.getDataClass());
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
