package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.JsonNode;

public interface ChromeCastEventListener {

	void onSpontaneousMediaStatus (MediaStatus mediaStatus);
	void onSpontaneousStatus (Status status);

	/**
	 * Called for spontaneous events who's type is not know.
	 */
	void onUnidentifiedSpontaneousEvent (JsonNode event);

}
