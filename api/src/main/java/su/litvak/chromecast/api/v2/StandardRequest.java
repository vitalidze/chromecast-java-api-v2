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

import java.util.Map;

/**
 * Parent class for transport object representing messages sent TO ChromeCast device.
 */
abstract class StandardRequest extends StandardMessage implements Request {
    Long requestId;

    @Override
    public final void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    @Override
    public final Long getRequestId() {
        return requestId;
    }

    /**
     * Request for current status of ChromeCast device.
     */
    static class Status extends StandardRequest {}

    /**
     * Request for availability of applications with specified identifiers.
     */
    static class AppAvailability extends StandardRequest {
        @JsonProperty
        final String[] appId;

        AppAvailability(String... appId) {
            this.appId = appId;
        }
    }

    /**
     * Request to launch application with specified identifiers.
     */
    static class Launch extends StandardRequest {
        @JsonProperty
        final String appId;

        Launch(@JsonProperty("appId") String appId) {
            this.appId = appId;
        }
    }

    /**
     * Request to stop currently running application.
     */
    static class Stop extends StandardRequest {
        @JsonProperty
        final String sessionId;

        Stop(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    /**
     * Request to load media.
     */
    static class Load extends StandardRequest {
        @JsonProperty
        final String sessionId;
        @JsonProperty
        final Media media;
        @JsonProperty
        final boolean autoplay;
        @JsonProperty
        final double currentTime;
        @JsonProperty
        final Object customData;

        Load(String sessionId, Media media, boolean autoplay, double currentTime,
             final Map<String, String> customData) {
            this.sessionId = sessionId;
            this.media = media;
            this.autoplay = autoplay;
            this.currentTime = currentTime;

            this.customData = customData == null ? null : new Object() {
                @JsonProperty
                Map<String, String> payload = customData;
            };
        }
    }

    /**
     * Abstract request for an action with currently played media.
     */
    abstract static class MediaRequest extends StandardRequest {
        @JsonProperty
        final long mediaSessionId;
        @JsonProperty
        final String sessionId;

        MediaRequest(long mediaSessionId, String sessionId) {
            this.mediaSessionId = mediaSessionId;
            this.sessionId = sessionId;
        }
    }

    /**
     * Request to start/resume playback.
     */
    static class Play extends MediaRequest {
        Play(long mediaSessionId, String sessionId) {
            super(mediaSessionId, sessionId);
        }
    }

    /**
     * Request to pause playback.
     */
    static class Pause extends MediaRequest {
        Pause(long mediaSessionId, String sessionId) {
            super(mediaSessionId, sessionId);
        }
    }

    /**
     * Request to change current playback position.
     */
    static class Seek extends MediaRequest {
        @JsonProperty
        final double currentTime;

        Seek(long mediaSessionId, String sessionId, double currentTime) {
            super(mediaSessionId, sessionId);
            this.currentTime = currentTime;
        }
    }

    /**
     * Request to change volume.
     */
    static class SetVolume extends StandardRequest {
        @JsonProperty
        final Volume volume;

        SetVolume(Volume volume) {
            this.volume = volume;
        }
    }

    static Status status() {
        return new Status();
    }

    static AppAvailability appAvailability(String... appId) {
        return new AppAvailability(appId);
    }

    static Launch launch(String appId) {
        return new Launch(appId);
    }

    static Stop stop(String sessionId) {
        return new Stop(sessionId);
    }

    static Load load(String sessionId, Media media, boolean autoplay, double currentTime,
                     Map<String, String> customData) {
        return new Load(sessionId, media, autoplay, currentTime, customData);
    }

    static Play play(String sessionId, long mediaSessionId) {
        return new Play(mediaSessionId, sessionId);
    }

    static Pause pause(String sessionId, long mediaSessionId) {
        return new Pause(mediaSessionId, sessionId);
    }

    static Seek seek(String sessionId, long mediaSessionId, double currentTime) {
        return new Seek(mediaSessionId, sessionId, currentTime);
    }

    static SetVolume setVolume(Volume volume) {
        return new SetVolume(volume);
    }
}
