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

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.SpontaneousEventType;

class EventListenerHolder implements ChromeCastSpontaneousEventListener {

	private final ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();
	private final Set<ChromeCastSpontaneousEventListener> eventListeners = new CopyOnWriteArraySet<ChromeCastSpontaneousEventListener>();

	public EventListenerHolder () {}

	public void registerListener (final ChromeCastSpontaneousEventListener listener) {
		if (listener != null) {
			this.eventListeners.add(listener);
		}
	}

	public void unregisterListener (final ChromeCastSpontaneousEventListener listener) {
		if (listener != null) {
			this.eventListeners.remove(listener);
		}
	}

	public void deliverEvent (final JsonNode json) throws IOException {
		if (json == null) return;
		if (this.eventListeners.size() < 1) return;

		final StandardResponse resp = this.jsonMapper.readValue(json, StandardResponse.class);

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
		}
		else if (resp instanceof StandardResponse.Status) {
			spontaneousEventReceived(new ChromeCastSpontaneousEvent(SpontaneousEventType.STATUS, ((StandardResponse.Status) resp).status));
		}
		else {
			spontaneousEventReceived(new ChromeCastSpontaneousEvent(SpontaneousEventType.UNKNOWN, json));
		}
	}

	public void deliverAppEvent (final AppEvent event) throws IOException {
		spontaneousEventReceived(new ChromeCastSpontaneousEvent(SpontaneousEventType.APPEVENT, event));
	}

	@Override
	public void spontaneousEventReceived (final ChromeCastSpontaneousEvent event) {
		for (final ChromeCastSpontaneousEventListener listener : this.eventListeners) {
			listener.spontaneousEventReceived(event);
		}
	}

}
