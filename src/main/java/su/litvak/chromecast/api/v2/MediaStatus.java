package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

public class MediaStatus {
    final long mediaSessionId;
    final int playbackRate;
    final String playerState;
    final float currentTime;
    final int supportedMediaCommands;
    final Volume volume;
    final Media media;

    public MediaStatus(@JsonProperty("mediaSessionId") long mediaSessionId,
                       @JsonProperty("playbackRate") int playbackRate,
                       @JsonProperty("playerState") String playerState,
                       @JsonProperty("currentTime") float currentTime,
                       @JsonProperty("supportedMediaCommands") int supportedMediaCommands,
                       @JsonProperty("volume") Volume volume,
                       @JsonProperty("media") Media media) {
        this.mediaSessionId = mediaSessionId;
        this.playbackRate = playbackRate;
        this.playerState = playerState;
        this.currentTime = currentTime;
        this.supportedMediaCommands = supportedMediaCommands;
        this.volume = volume;
        this.media = media;
    }
}
