/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
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
 * Track meta data information
 *
 * @see <a href="https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media.Track">https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media.Track</a>
 */
public class Track {
    /**
     * Media track type
     *
     * @see <a href="https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.TrackType">https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.TrackType</a>
     */
    public enum TrackType { TEXT, AUDIO, VIDEO }

    public final long id;
    public final TrackType type;

    public Track(@JsonProperty("trackId") long id,
                 @JsonProperty("trackType") TrackType type) {
        this.id = id;
        this.type = type;
    }

}
