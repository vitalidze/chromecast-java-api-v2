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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Current ChromeCast device status.
 */
public class Status {
    public final Volume volume;
    public final List<Application> applications;
    public final boolean activeInput;
    public final boolean standBy;

    Status(@JsonProperty("volume") Volume volume,
           @JsonProperty("applications") List<Application> applications,
           @JsonProperty("isActiveInput") boolean activeInput,
           @JsonProperty("isStandBy") boolean standBy) {
        this.volume = volume;
        this.applications = applications == null ? Collections.<Application>emptyList() : applications;
        this.activeInput = activeInput;
        this.standBy = standBy;
    }

    @JsonIgnore
    public final Application getRunningApp() {
        return applications.isEmpty() ? null : applications.get(0);
    }

    public final boolean isAppRunning(String appId) {
        return getRunningApp() != null && getRunningApp().id.equals(appId);
    }

    @Override
    public final String toString() {
        final String applicationsString = this.applications == null
                ? "<null>"
                : Arrays.toString(this.applications.toArray());

        return String.format("Media{volume: %s, applications: %s, activeInput: %b, standBy; %b}",
                this.volume, applicationsString, this.activeInput, this.standBy);
    }
}
