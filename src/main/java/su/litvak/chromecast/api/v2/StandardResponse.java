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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;

/**
 * Parent class for transport object representing messages received FROM ChromeCast device.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "responseType")
@JsonSubTypes({@JsonSubTypes.Type(name = "PING", value = StandardResponse.Ping.class),
               @JsonSubTypes.Type(name = "PONG", value = StandardResponse.Pong.class),
               @JsonSubTypes.Type(name = "RECEIVER_STATUS", value = StandardResponse.Status.class),
               @JsonSubTypes.Type(name = "GET_APP_AVAILABILITY", value = StandardResponse.AppAvailability.class),
               @JsonSubTypes.Type(name = "INVALID_REQUEST", value = StandardResponse.Invalid.class),
               @JsonSubTypes.Type(name = "MEDIA_STATUS", value = StandardResponse.MediaStatus.class),
               @JsonSubTypes.Type(name = "CLOSE", value = StandardResponse.Close.class),
               @JsonSubTypes.Type(name = "LOAD_FAILED", value = StandardResponse.LoadFailed.class),
               @JsonSubTypes.Type(name = "LAUNCH_ERROR", value = StandardResponse.LaunchError.class),
               @JsonSubTypes.Type(name = "DEVICE_ADDED", value = StandardResponse.DeviceAdded.class),
               @JsonSubTypes.Type(name = "DEVICE_UPDATED", value = StandardResponse.DeviceUpdated.class),
               @JsonSubTypes.Type(name = "DEVICE_REMOVED", value = StandardResponse.DeviceRemoved.class)})
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

    /**
     * Request to send 'Pong' message in reply.
     */
    static class Ping extends StandardResponse {}

    /**
     * Response in reply to 'Ping' message.
     */
    static class Pong extends StandardResponse {}

    /**
     * Request to 'Close' connection.
     */
    static class Close extends StandardResponse {}

    /**
     * Identifies that loading of media has failed.
     */
    static class LoadFailed extends StandardResponse {}

    /**
     * Request was invalid for some <code>reason</code>.
     */
    static class Invalid extends StandardResponse {
        final String reason;

        Invalid(@JsonProperty("reason") String reason) {
            this.reason = reason;
        }
    }

    /**
     * Application cannot be launched for some <code>reason</code>.
     */
    static class LaunchError extends StandardResponse {
        final String reason;

        LaunchError(@JsonProperty("reason") String reason) {
            this.reason = reason;
        }
    }

    /**
     * Response to "Status" request.
     */
    static class Status extends StandardResponse {
        @JsonProperty
        final su.litvak.chromecast.api.v2.Status status;

        Status(@JsonProperty("status") su.litvak.chromecast.api.v2.Status status) {
            this.status = status;
        }
    }

    /**
     * Response to "MediaStatus" request.
     */
    static class MediaStatus extends StandardResponse {
        final su.litvak.chromecast.api.v2.MediaStatus[] statuses;

        MediaStatus(@JsonProperty("status") su.litvak.chromecast.api.v2.MediaStatus... statuses) {
            this.statuses = statuses;
        }
    }

    /**
     * Response to "AppAvailability" request.
     */
    static class AppAvailability extends StandardResponse {
        @JsonProperty
        Map<String, String> availability;
    }

    /**
     * Received when power is cycled on ChromeCast Audio device in a group.
     */
    static class DeviceAdded extends StandardResponse {
        final Device device;

        DeviceAdded(@JsonProperty("device") Device device) {
            this.device = device;
        }
    }

    /**
     * Received when volume is changed in ChromeCast Audio group.
     */
    static class DeviceUpdated extends StandardResponse {
        final Device device;

        DeviceUpdated(@JsonProperty("device") Device device) {
            this.device = device;
        }
    }

    /**
     * Received when power is cycled on ChromeCast Audio device in a group.
     */
    static class DeviceRemoved extends StandardResponse {
        final String deviceId;

        DeviceRemoved(@JsonProperty("deviceId") String deviceId) {
            this.deviceId = deviceId;
        }
    }
}
