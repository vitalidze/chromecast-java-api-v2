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
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Parent class for transport objects used to communicate with ChromeCast
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(name = "PING", value = Message.Ping.class),
               @JsonSubTypes.Type(name = "PONG", value = Message.Pong.class),
               @JsonSubTypes.Type(name = "CONNECT", value = Message.Connect.class),
               @JsonSubTypes.Type(name = "GET_STATUS", value = Request.Status.class),
               @JsonSubTypes.Type(name = "GET_APP_AVAILABILITY", value = Request.AppAvailability.class),
               @JsonSubTypes.Type(name = "LAUNCH", value = Request.Launch.class),
               @JsonSubTypes.Type(name = "STOP", value = Request.Stop.class),
               @JsonSubTypes.Type(name = "LOAD", value = Request.Load.class),
               @JsonSubTypes.Type(name = "PLAY", value = Request.Play.class),
               @JsonSubTypes.Type(name = "PAUSE", value = Request.Pause.class),
               @JsonSubTypes.Type(name = "SET_VOLUME", value = Request.SetVolume.class),
               @JsonSubTypes.Type(name = "SEEK", value = Request.Seek.class)})
abstract class Message {
    static class Ping extends Message {}
    static class Pong extends Message {}

    @JsonSerialize
    static class Origin {}

    static class Connect extends Message {
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
