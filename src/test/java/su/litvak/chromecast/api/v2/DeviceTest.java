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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DeviceTest {
    final ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();

    @Test
    public void testDeviceAdded() throws IOException {
        final String jsonMSG = FixtureHelper.fixtureAsString("/device-added.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.DeviceAdded response = jsonMapper.readValue(jsonMSG, StandardResponse.DeviceAdded.class);

        assertNotNull(response.device);
        Device device = response.device;
        assertEquals("Amplifier", device.name);
        assertEquals("123456", device.deviceId);
        assertEquals(4, device.capabilities);
        assertNotNull(device.volume);
        Volume volume = device.volume;
        assertEquals(0.24, volume.level, 0.001);
        assertFalse(volume.muted);
        assertNotNull(volume.increment);
        assertNotNull(volume.stepInterval);
        assertNull(volume.controlType);
    }

    @Test
    public void testDeviceRemoved() throws IOException {
        final String jsonMSG = FixtureHelper.fixtureAsString("/device-removed.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.DeviceRemoved response =
                jsonMapper.readValue(jsonMSG, StandardResponse.DeviceRemoved.class);

        assertNotNull(response.deviceId);
        assertEquals("111111", response.deviceId);
    }

    @Test
    public void testDeviceUpdated() throws IOException {
        final String jsonMSG = FixtureHelper.fixtureAsString("/device-updated.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.DeviceUpdated response =
                jsonMapper.readValue(jsonMSG, StandardResponse.DeviceUpdated.class);

        assertNotNull(response.device);
        Device device = response.device;
        assertEquals("Amplifier", device.name);
        assertEquals("654321", device.deviceId);
        assertEquals(4, device.capabilities);
        assertNotNull(device.volume);
        Volume volume = device.volume;
        assertEquals(0.35, volume.level, 0.001);
        assertFalse(volume.muted);
        assertNotNull(volume.increment);
        assertNotNull(volume.stepInterval);
        assertNull(volume.controlType);
    }
}
