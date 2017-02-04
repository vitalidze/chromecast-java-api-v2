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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class StatusTest {
    final ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();

    @Test
    public void testDeserializationBackdrop1_18() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-backdrop-1.18.json").replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        Status status = response.status;
        assertNotNull(status);
        assertTrue(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        Application app = status.getRunningApp();
        assertFalse(app.isIdleScreen);

        Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(1.0, volume.level, 0.1);
        assertFalse(volume.muted);
        assertNull(volume.controlType);
        assertEquals(Volume.DEFAULT_INCREMENT, volume.increment, 0.001);
        assertEquals(Volume.DEFAULT_INCREMENT, volume.stepInterval, 0.001);
    }

    @Test
    public void testDeserializationBackdrop1_19() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-backdrop-1.19.json").replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        Status status = response.status;
        assertNotNull(status);
        assertFalse(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        Application app = status.getRunningApp();
        assertTrue(app.isIdleScreen);

        Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(1.0, volume.level, 0.1);
        assertFalse(volume.muted);
        assertEquals("attenuation", volume.controlType);
        assertEquals(Volume.DEFAULT_INCREMENT, volume.increment, 0.001);
        assertEquals(0.04, volume.stepInterval, 0.001);
    }

    @Test
    public void testDeserializationChromeMirroring() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-chrome-mirroring-1.19.json").replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        Status status = response.status;
        assertNotNull(status);
        assertFalse(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        Application app = status.getRunningApp();
        assertFalse(app.isIdleScreen);

        Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(1.0, volume.level, 0.1);
        assertFalse(volume.muted);
        assertEquals("attenuation", volume.controlType);
        assertEquals(Volume.DEFAULT_INCREMENT, volume.increment, 0.001);
        assertEquals(0.04, volume.stepInterval, 0.001);
    }
}
