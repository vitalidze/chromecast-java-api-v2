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
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.Map;

/**
 * Parent class for transport object representing messages received FROM ChromeCast device
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property = "responseType")
@JsonSubTypes({@JsonSubTypes.Type(name = "PING", value = StandardResponse.Ping.class),
               @JsonSubTypes.Type(name = "PONG", value = StandardResponse.Pong.class),
               @JsonSubTypes.Type(name = "RECEIVER_STATUS", value = StandardResponse.Status.class),
               @JsonSubTypes.Type(name = "GET_APP_AVAILABILITY", value = StandardResponse.AppAvailability.class),
               @JsonSubTypes.Type(name = "INVALID_REQUEST", value = StandardResponse.Invalid.class),
               @JsonSubTypes.Type(name = "MEDIA_STATUS", value = StandardResponse.MediaStatus.class),
               @JsonSubTypes.Type(name = "CLOSE", value = StandardResponse.Close.class),
               @JsonSubTypes.Type(name = "LOAD_FAILED", value = StandardResponse.LoadFailed.class),
               @JsonSubTypes.Type(name = "LAUNCH_ERROR", value = StandardResponse.LaunchError.class)})
abstract class StandardResponse implements Response {
    Long requestId;

    @Override
    public final Long getRequestId() {
        return requestId;
    }

    @Override
    public final void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    static class Ping extends StandardResponse {}
    static class Pong extends StandardResponse {}
    static class LoadFailed extends StandardResponse {}

    static class Close extends StandardResponse {
        @JsonProperty
        final boolean requestedBySender;

        Close() {
            requestedBySender = false;
        }
        Close(@JsonProperty("requestedBySender") boolean requestedBySender) {
            this.requestedBySender = requestedBySender;
        }
    }

    static class Invalid extends StandardResponse {
        final String reason;

        Invalid(@JsonProperty("reason") String reason) {
            this.reason = reason;
        }
    }

    static class LaunchError extends StandardResponse {
        final String reason;

        LaunchError(@JsonProperty("reason") String reason) {
            this.reason = reason;
        }
    }

    static class Status extends StandardResponse {
        @JsonProperty
        final su.litvak.chromecast.api.v2.Status status;

        Status(@JsonProperty("status") su.litvak.chromecast.api.v2.Status status) {
            this.status = status;
        }
    }

    static class MediaStatus extends StandardResponse {
        final su.litvak.chromecast.api.v2.MediaStatus statuses[];

        MediaStatus(@JsonProperty("status") su.litvak.chromecast.api.v2.MediaStatus status[]) {
            this.statuses = status;
        }
    }

    static class AppAvailability extends StandardResponse {
        @JsonProperty
        Map<String, String> availability;
    }
}
