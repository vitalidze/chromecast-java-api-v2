/*
 * Copyright 2018 Vitaly Litvak (vitavaque@gmail.com)
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
 * Device descriptor.
 */
public class Device {
    public final String name;
    public final int capabilities;
    public final String deviceId;
    public final Volume volume;

    public Device(@JsonProperty("name") String name,
                  @JsonProperty("capabilities") int capabilities,
                  @JsonProperty("deviceId") String deviceId,
                  @JsonProperty("volume") Volume volume) {
        this.name = name;
        this.capabilities = capabilities;
        this.deviceId = deviceId;
        this.volume = volume;
    }
}
