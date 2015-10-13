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
 * Media streamed on ChromeCast device
 */
public class Media {
    @JsonIgnore
    public String metadata;

    @JsonProperty("contentId")
    public final String url;

    @JsonIgnore
    public Double duration;

    @JsonIgnore
    public String streamType = "buffered";

    @JsonProperty
    public final String contentType;

    @JsonIgnore
    public String customData;

    public Media() {
        this.url = "n/a";
        this.contentType = "n/a";
    }

    public Media(@JsonProperty("contentId") String url,
                 @JsonProperty("contentType") String contentType) {
        this.url = url;
        this.contentType = contentType;
    }

    // public Media(@JsonProperty("contentId") String url,
    //              @JsonProperty("duration") Double duration,
    //              // @JsonProperty("metadata") MetaData metadata,
    //              @JsonProperty("streamType") String streamType,
    //              @JsonProperty("contentType") String contentType) {
    //     this.url = url;
    //     this.duration = duration;
    //     // this.metadata = metadata;
    //     this.streamType = streamType;
    //     this.contentType = contentType;

    // }
}
