package su.litvak.chromecast.api.v2;

import org.codehaus.jackson.annotate.JsonProperty;

public class MediaStatus {
    public enum PlayerState { IDLE, BUFFERING, PLAYING, PAUSED }

    public final long mediaSessionId;
    public final int playbackRate;
    public final PlayerState playerState;
    public final float currentTime;
    public final int supportedMediaCommands;
    public final Volume volume;
    public final Media media;

    MediaStatus(@JsonProperty("mediaSessionId") long mediaSessionId,
                       @JsonProperty("playbackRate") int playbackRate,
                       @JsonProperty("playerState") PlayerState playerState,
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
