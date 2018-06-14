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
import org.codehaus.jackson.map.ObjectMapper;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.SpontaneousEventType;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Helper class for delivering spontaneous events to their listeners.
 */
class EventListenerHolder implements
    ChromeCastSpontaneousEventListener,
    ChromeCastConnectionEventListener,
    ChromeCastRawMessageListener {

    private final ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();
    private final Set<ChromeCastSpontaneousEventListener> eventListeners =
            new CopyOnWriteArraySet<ChromeCastSpontaneousEventListener>();
    private final Set<ChromeCastConnectionEventListener> eventListenersConnection =
            new CopyOnWriteArraySet<ChromeCastConnectionEventListener>();
    private final Set<ChromeCastRawMessageListener> rawMessageListeners =
            new CopyOnWriteArraySet<ChromeCastRawMessageListener>();

    EventListenerHolder() {}

    public void registerListener(ChromeCastSpontaneousEventListener listener) {
        if (listener != null) {
            this.eventListeners.add(listener);
        }
    }

    public void unregisterListener(ChromeCastSpontaneousEventListener listener) {
        if (listener != null) {
            this.eventListeners.remove(listener);
        }
    }

    public void deliverEvent(JsonNode json) throws IOException {
        if (json == null || this.eventListeners.isEmpty()) {
            return;
        }

        final StandardResponse resp = json.has("responseType") ? this.jsonMapper.readValue(json, StandardResponse.class)
                : null;

        /*
         * The documentation only mentions MEDIA_STATUS as being a possible spontaneous event.
         * Though RECEIVER_STATUS has also been observed.
         * If others are observed, they should be added here.
         * see: https://developers.google.com/cast/docs/reference/messages#MediaMess
         */
        if (resp instanceof StandardResponse.MediaStatus) {
            for (final MediaStatus ms : ((StandardResponse.MediaStatus) resp).statuses) {
                spontaneousEventReceived(new ChromeCastSpontaneousEvent(SpontaneousEventType.MEDIA_STATUS, ms));
            }
        } else if (resp instanceof StandardResponse.Status) {
            spontaneousEventReceived(new ChromeCastSpontaneousEvent(SpontaneousEventType.STATUS,
                    ((StandardResponse.Status) resp).status));
        } else if (resp instanceof StandardResponse.Close) {
            spontaneousEventReceived(new ChromeCastSpontaneousEvent(SpontaneousEventType.CLOSE, new Object()));
        } else {
            spontaneousEventReceived(new ChromeCastSpontaneousEvent(SpontaneousEventType.UNKNOWN, json));
        }
    }

    public void deliverAppEvent(AppEvent event) throws IOException {
        spontaneousEventReceived(new ChromeCastSpontaneousEvent(SpontaneousEventType.APPEVENT, event));
    }

    @Override
    public void spontaneousEventReceived(ChromeCastSpontaneousEvent event) {
        for (ChromeCastSpontaneousEventListener listener : this.eventListeners) {
            listener.spontaneousEventReceived(event);
        }
    }

    public void registerConnectionListener(ChromeCastConnectionEventListener listener) {
        if (listener != null) {
            this.eventListenersConnection.add(listener);
        }
    }

    public void unregisterConnectionListener(ChromeCastConnectionEventListener listener) {
        if (listener != null) {
            this.eventListenersConnection.remove(listener);
        }
    }

    public void deliverConnectionEvent(boolean connected) {
        connectionEventReceived(new ChromeCastConnectionEvent(connected));
    }

    @Override
    public void connectionEventReceived(ChromeCastConnectionEvent event) {
        for (ChromeCastConnectionEventListener listener : this.eventListenersConnection) {
            listener.connectionEventReceived(event);
        }
    }

    public void registerRawMessageListener(ChromeCastRawMessageListener listener) {
        if (listener != null) {
            this.rawMessageListeners.add(listener);
        }
    }

    public void unregisterRawMessageListener(ChromeCastRawMessageListener listener) {
        if (listener != null) {
            this.rawMessageListeners.remove(listener);
        }
    }

    @Override
    public void rawMessageReceived(ChromeCastRawMessage message, Long requestId) {
        for (ChromeCastRawMessageListener listener : this.rawMessageListeners) {
            listener.rawMessageReceived(message, requestId);
        }
    }
}
