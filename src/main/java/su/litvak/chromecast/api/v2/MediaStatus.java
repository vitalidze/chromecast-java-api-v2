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
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Current media player status - which media is played, volume, time position, etc.
 *
 * @see <a href="https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media.MediaStatus">https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media.MediaStatus</a>
 */
public class MediaStatus {
    /**
     * Playback status
     *
     * @see <a href="https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.PlayerState">https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.PlayerState</a>
     */
    public enum PlayerState { IDLE, BUFFERING, PLAYING, PAUSED }

    /**
     * @see <a href="https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.repeatMode">https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.repeatMode</a>
     */
    public enum RepeatMode { REPEAT_OFF, REPEAT_ALL, REPEAT_SINGLE, REPEAT_ALL_AND_SHUFFLE }

    /**
     * <p>The reason for the player to be in IDLE state.</p>
     *
     * <p>Pandora is known to use 'COMPLETED' when the app timesout</p>
     *
     * @see <a href="https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.IdleReason">https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.IdleReason</a>
     */
    public enum IdleReason { CANCELLED, INTERRUPTED, FINISHED, ERROR, COMPLETED}

    public final List<Integer> activeTrackIds;
    public final long mediaSessionId;
    public final int playbackRate;
    public final PlayerState playerState;
    public final Integer currentItemId;
    public final double currentTime;
    public final Map<String, Object> customData;
    public final Integer loadingItemId;
    public final List<Item> items;
    public final Integer preloadedItemId;
    public final int supportedMediaCommands;
    public final Volume volume;
    public final Media media;
    public final RepeatMode repeatMode;
    public final IdleReason idleReason;

    MediaStatus(@JsonProperty("activeTrackIds") List<Integer> activeTrackIds,
                @JsonProperty("mediaSessionId") long mediaSessionId,
                @JsonProperty("playbackRate") int playbackRate,
                @JsonProperty("playerState") PlayerState playerState,
                @JsonProperty("currentItemId") Integer currentItemId,
                @JsonProperty("currentTime") double currentTime,
                @JsonProperty("customData") Map<String, Object> customData,
                @JsonProperty("loadingItemId") Integer loadingItemId,
                @JsonProperty("items") List<Item> items,
                @JsonProperty("preloadedItemId") Integer preloadedItemId,
                @JsonProperty("supportedMediaCommands") int supportedMediaCommands,
                @JsonProperty("volume") Volume volume,
                @JsonProperty("media") Media media,
                @JsonProperty("repeatMode") RepeatMode repeatMode,
                @JsonProperty("idleReason") IdleReason idleReason) {
        this.activeTrackIds = activeTrackIds != null ? Collections.unmodifiableList(activeTrackIds) : null;
        this.mediaSessionId = mediaSessionId;
        this.playbackRate = playbackRate;
        this.playerState = playerState;
        this.currentItemId = currentItemId;
        this.currentTime = currentTime;
        this.customData = customData != null ? Collections.unmodifiableMap(customData) : null;
        this.loadingItemId = loadingItemId;
        this.items = items != null ? Collections.unmodifiableList(items) : null;
        this.preloadedItemId = preloadedItemId;
        this.supportedMediaCommands = supportedMediaCommands;
        this.volume = volume;
        this.media = media;
        this.repeatMode = repeatMode;
        this.idleReason = idleReason;
    }
}
