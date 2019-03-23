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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Parent class for transport objects used to communicate with ChromeCast.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(name = "PING", value = StandardMessage.Ping.class),
               @JsonSubTypes.Type(name = "PONG", value = StandardMessage.Pong.class),
               @JsonSubTypes.Type(name = "CONNECT", value = StandardMessage.Connect.class),
               @JsonSubTypes.Type(name = "GET_STATUS", value = StandardRequest.Status.class),
               @JsonSubTypes.Type(name = "GET_APP_AVAILABILITY", value = StandardRequest.AppAvailability.class),
               @JsonSubTypes.Type(name = "LAUNCH", value = StandardRequest.Launch.class),
               @JsonSubTypes.Type(name = "STOP", value = StandardRequest.Stop.class),
               @JsonSubTypes.Type(name = "LOAD", value = StandardRequest.Load.class),
               @JsonSubTypes.Type(name = "PLAY", value = StandardRequest.Play.class),
               @JsonSubTypes.Type(name = "PAUSE", value = StandardRequest.Pause.class),
               @JsonSubTypes.Type(name = "SET_VOLUME", value = StandardRequest.SetVolume.class),
               @JsonSubTypes.Type(name = "SEEK", value = StandardRequest.Seek.class)})
abstract class StandardMessage implements Message {
    /**
     * Simple "Ping" message to request a reply with "Pong" message.
     */
    static class Ping extends StandardMessage {}

    /**
     * Simple "Pong" message to reply to "Ping" message.
     */
    static class Pong extends StandardMessage {}

    /**
     * Some "Origin" required to be sent with the "Connect" request.
     */
    @JsonSerialize
    static class Origin {}

    /**
     * Used to initiate connection with the ChromeCast device.
     */
    static class Connect extends StandardMessage {
        @JsonProperty
        final Origin origin = new Origin();
    }

    public static Ping ping() {
        return new Ping();
    }

    public static Pong pong() {
        return new Pong();
    }

    public static Connect connect() {
        return new Connect();
    }
}
