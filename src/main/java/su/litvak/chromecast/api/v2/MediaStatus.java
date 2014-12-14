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

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Current media player status - which media is played, volume, time position, etc.
 */
public class MediaStatus {
    /**
     * Playback status
     */
    public enum PlayerState { IDLE, BUFFERING, PLAYING, PAUSED }

    public final long mediaSessionId;
    public final int playbackRate;
    public final PlayerState playerState;
    public final float currentTime;
    public final int supportedMediaCommands;
    public final Volume volume;
    public final Media media;
    public final String idleReason;

    MediaStatus(@JsonProperty("mediaSessionId") long mediaSessionId,
                       @JsonProperty("playbackRate") int playbackRate,
                       @JsonProperty("playerState") PlayerState playerState,
                       @JsonProperty("currentTime") float currentTime,
                       @JsonProperty("supportedMediaCommands") int supportedMediaCommands,
                       @JsonProperty("volume") Volume volume,
                       @JsonProperty("media") Media media,
                       @JsonProperty("idleReason") String idleReason) {
        this.mediaSessionId = mediaSessionId;
        this.playbackRate = playbackRate;
        this.playerState = playerState;
        this.currentTime = currentTime;
        this.supportedMediaCommands = supportedMediaCommands;
        this.volume = volume;
        this.media = media;
        this.idleReason = idleReason;
    }
}
