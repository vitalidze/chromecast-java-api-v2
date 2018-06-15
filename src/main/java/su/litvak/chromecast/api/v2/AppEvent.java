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

/**
 * A custom event sent by a receiver app.
 */
public class AppEvent {
    @JsonProperty
    public final String namespace;
    @JsonProperty
    public final String message;

    AppEvent(String namespace, String message) {
        this.namespace = namespace;
        this.message = message;
    }

    @Override
    public final String toString() {
        return String.format("AppEvent{namespace: %s, message: %s}", this.namespace, this.message);
    }
}
