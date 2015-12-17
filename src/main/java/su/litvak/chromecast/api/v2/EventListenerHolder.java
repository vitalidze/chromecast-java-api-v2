package su.litvak.chromecast.api.v2;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class EventListenerHolder implements ChromeCastEventListener {

	private final ObjectMapper jsonMapper = new ObjectMapper();
	private final Set<ChromeCastEventListener> eventListeners = new CopyOnWriteArraySet<ChromeCastEventListener>();

	public EventListenerHolder () {}

	public void registerListener (final ChromeCastEventListener listener) {
		if (listener != null) {
			this.eventListeners.add(listener);
		}
	}

	public void unregisterListener (final ChromeCastEventListener listener) {
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
				onSpontaneousMediaStatus(ms);
			}
		}
		else if (resp instanceof StandardResponse.Status) {
			onSpontaneousStatus(((StandardResponse.Status) resp).status);
		}
		else {
			onUnidentifiedSpontaneousEvent(json);
		}
	}

	@Override
	public void onSpontaneousMediaStatus (final MediaStatus mediaStatus) {
		for (final ChromeCastEventListener listener : this.eventListeners) {
			listener.onSpontaneousMediaStatus(mediaStatus);
		}
	}

	@Override
	public void onSpontaneousStatus (Status status) {
		for (final ChromeCastEventListener listener : this.eventListeners) {
			listener.onSpontaneousStatus(status);
		}
	}

	@Override
	public void onUnidentifiedSpontaneousEvent (final JsonNode event) {
		for (final ChromeCastEventListener listener : this.eventListeners) {
			listener.onUnidentifiedSpontaneousEvent(event);
		}
	}

}
