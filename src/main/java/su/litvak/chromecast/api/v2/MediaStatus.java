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

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Current media player status - which media is played, volume, time position, etc.
 */
public class MediaStatus {
    /**
     * Playback status
     */
    public enum PlayerState { IDLE, BUFFERING, PLAYING, PAUSED }

    /**
     * https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.repeatMode
     */
    public enum RepeatMode { REPEAT_OFF, REPEAT_ALL, REPEAT_SINGLE, REPEAT_ALL_AND_SHUFFLE }

    public final long mediaSessionId;
    public final int playbackRate;
    public final PlayerState playerState;
    public final int currentItemId;
    public final float currentTime;
    public final List<Item> items;
    public final int supportedMediaCommands;
    public final Volume volume;
    public final Media media;
    public final RepeatMode repeatMode;
    public final String idleReason;

    MediaStatus(@JsonProperty("mediaSessionId") long mediaSessionId,
                       @JsonProperty("playbackRate") int playbackRate,
                       @JsonProperty("playerState") PlayerState playerState,
                       @JsonProperty("currentItemId") int currentItemId,
                       @JsonProperty("currentTime") float currentTime,
                       @JsonProperty("items") List<Item> items,
                       @JsonProperty("supportedMediaCommands") int supportedMediaCommands,
                       @JsonProperty("volume") Volume volume,
                       @JsonProperty("media") Media media,
                       @JsonProperty("repeatMode") RepeatMode repeatMode,
                       @JsonProperty("idleReason") String idleReason) {
        this.mediaSessionId = mediaSessionId;
        this.playbackRate = playbackRate;
        this.playerState = playerState;
        this.currentItemId = currentItemId;
        this.currentTime = currentTime;
        this.items = items != null ? Collections.unmodifiableList(items) : null;
        this.supportedMediaCommands = supportedMediaCommands;
        this.volume = volume;
        this.media = media;
        this.repeatMode = repeatMode;
        this.idleReason = idleReason;
    }
}
