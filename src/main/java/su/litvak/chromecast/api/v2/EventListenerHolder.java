package su.litvak.chromecast.api.v2;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.MediaStatusSpontaneousEvent;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.StatusSpontaneousEvent;
import su.litvak.chromecast.api.v2.ChromeCastSpontaneousEvent.UnknownSpontaneousEvent;

class EventListenerHolder implements ChromeCastSpontaneousEventListener {

	private final ObjectMapper jsonMapper = new ObjectMapper();
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
				spontaneousEventReceived(new MediaStatusSpontaneousEvent(ms));
			}
		}
		else if (resp instanceof StandardResponse.Status) {
			spontaneousEventReceived(new StatusSpontaneousEvent(((StandardResponse.Status) resp).status));
		}
		else {
			spontaneousEventReceived(new UnknownSpontaneousEvent(json));
		}
	}

	@Override
	public void spontaneousEventReceived (final ChromeCastSpontaneousEvent event) {
		for (final ChromeCastSpontaneousEventListener listener : this.eventListeners) {
			listener.spontaneousEventReceived(event);
		}
	}

}
