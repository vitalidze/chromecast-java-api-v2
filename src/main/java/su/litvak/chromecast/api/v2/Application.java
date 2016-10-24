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
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Application descriptor
 */
public class Application {
    public final String id;
    public final String name;
    public final String sessionId;
    public final String statusText;
    public final String transportId;
    public final boolean isIdleScreen;
    public final List<Namespace> namespaces;

    public Application(@JsonProperty("appId") String id,
                       @JsonProperty("displayName") String name,
                       @JsonProperty("sessionId") String sessionId,
                       @JsonProperty("statusText") String statusText,
                       @JsonProperty("isIdleScreen") boolean isIdleScreen,
                       @JsonProperty("transportId") String transportId,
                       @JsonProperty("namespaces") List<Namespace> namespaces) {
        this.id = id;
        this.name = name;
        this.sessionId = sessionId;
        this.statusText = statusText;
        this.transportId = transportId;
        this.namespaces = namespaces == null ? Collections.<Namespace>emptyList() : namespaces;
        this.isIdleScreen = isIdleScreen;
    }

    @Override
    public String toString() {
        final String namespaces = this.namespaces == null ? "<null>" : Arrays.toString(this.namespaces.toArray());

        return String.format("Application{id: %s, name: %s, sessionId: %s, statusText: %s, transportId: %s, isIdleScreen: %b, namespaces: %s}",
                this.id, this.name, this.sessionId, this.statusText, this.transportId, this.isIdleScreen, namespaces);
    }

}
