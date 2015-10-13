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
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Current media player status - which media is played, volume, time position, etc.
 */
public class MediaStatus {
    /**
     * Playback status
     */
    public enum PlayerState { IDLE, BUFFERING, PLAYING, PAUSED }
    public enum RepeatState { REPEAT_OFF, REPEAT_ON }

    public final float currentTime;
    public final int playbackRate;
    public final int supportedMediaCommands;
    public final PlayerState playerState;
    public final Media media;


    public Volume volume;

    public long mediaSessionId = -1L;

    RepeatState repeatMode = RepeatState.REPEAT_OFF;

    @JsonIgnore
    public int currentItemId;

    @JsonIgnore
    public int items;

    @JsonIgnore
    public String customData;

    MediaStatus(       @JsonProperty("currentTime") float currentTime,
                       @JsonProperty("playbackRate") int playbackRate,
                       @JsonProperty("supportedMediaCommands") int supportedMediaCommands,
                       @JsonProperty("playerState") PlayerState playerState,
                       @JsonProperty("media") Media media,
                       @JsonProperty("mediaSessionId") long mediaSessionId,
                       @JsonProperty("volume") Volume volume,
                       @JsonProperty("repeatMode") RepeatState repeatMode


                ) {
        this.currentTime = currentTime;
        this.playbackRate = playbackRate;
        this.supportedMediaCommands = supportedMediaCommands;
        this.playerState = playerState;
        this.media = media;
        this.mediaSessionId = mediaSessionId;
        this.volume = volume;
        this.repeatMode = repeatMode;
    }
}
