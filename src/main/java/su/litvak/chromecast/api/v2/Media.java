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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnore;
/**
 * Media streamed on ChromeCast device
 *
 * @see <a href="https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media.MediaInformation">https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media.MediaInformation</a>
 */
public class Media {

    /**
     * <p>Stream type</p>
     *
     * <p>Some receivers use upper-case (like Pandora), some use lower-case (like Google Audio),
     * duplicate elements to support both</p>
     *
     * @see <a href="https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.StreamType">https://developers.google.com/cast/docs/reference/receiver/cast.receiver.media#.StreamType</a>
     */
    public enum StreamType {
        BUFFERED, buffered,
        LIVE, live,
        NONE, none
    }

    @JsonProperty
    public final Map<String, Object> metadata;

    @JsonProperty("contentId")
    public final String url;

    @JsonProperty
    public final Double duration;

    @JsonProperty
    public final StreamType streamType;

    @JsonProperty
    public final String contentType;

    @JsonProperty
    public final Map<String, Object> customData;

    @JsonIgnore
    public final Map<String, Object> textTrackStyle;

    @JsonIgnore
    public final List<Track> tracks;

    public Media(String url, String contentType) {
        this(url, contentType, null, null);
    }

    public Media(String url, String contentType, Double duration, StreamType streamType) {
        this(url, contentType, duration, streamType, null, null, null, null);
    }

    public Media(@JsonProperty("contentId") String url,
                 @JsonProperty("contentType") String contentType,
                 @JsonProperty("duration") Double duration,
                 @JsonProperty("streamType") StreamType streamType,
                 @JsonProperty("customData") Map<String, Object> customData,
                 @JsonProperty("metadata") Map<String, Object> metadata,
                 @JsonProperty("textTrackStyle") Map<String, Object> textTrackStyle,
                 @JsonProperty("tracks") List<Track> tracks) {
        this.url = url;
        this.contentType = contentType;
        this.duration = duration;
        this.streamType = streamType;
        this.customData = customData == null ? null : Collections.unmodifiableMap(customData);
        this.metadata = metadata == null ? null : Collections.unmodifiableMap(metadata);
        this.textTrackStyle = textTrackStyle == null ? null : Collections.unmodifiableMap(textTrackStyle);
        this.tracks = tracks == null ? null : Collections.unmodifiableList(tracks);
    }

    @Override
    public int hashCode () {
        return Arrays.hashCode(new Object[] { this.url, this.contentType, this.streamType, this.duration });
    }

    @Override
    public boolean equals (final Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Media)) return false;
        final Media that = (Media) obj;
        return this.url == null ? that.url == null : this.url.equals(that.url) &&
                this.contentType == null ? that.contentType == null : this.contentType.equals(that.contentType) &&
                this.streamType == null ? that.streamType == null : this.streamType.equals(that.streamType) &&
                this.duration == null ? that.duration == null : this.duration.equals(that.duration);
    }

    @Override
    public String toString () {
        return String.format("Media{%s, %s, %s}", this.url, this.contentType, this.duration);
    }

}
