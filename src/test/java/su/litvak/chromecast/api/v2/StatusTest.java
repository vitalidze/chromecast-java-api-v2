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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class StatusTest {

    final ObjectMapper jsonMapper = JacksonHelper.createJSONMapper();

    @Test
    public void testDeserializationBackdrop118() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-backdrop-1.18.json")
                .replaceFirst("\"type\"", "\"responseType\"");
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
    public void testDeserializationBackdrop119() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-backdrop-1.19.json")
                .replaceFirst("\"type\"", "\"responseType\"");
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
    public void testDeserializationBackdrop128() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-backdrop-1.28.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        Status status = response.status;
        assertNotNull(status);
        assertFalse(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        Application app = status.getRunningApp();
        assertTrue(app.isIdleScreen);
        assertFalse(app.launchedFromCloud);

        Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(1.0, volume.level, 0.1);
        assertFalse(volume.muted);
        assertEquals("attenuation", volume.controlType);
        assertEquals(Volume.DEFAULT_INCREMENT, volume.increment, 0.001);
        assertEquals(0.05, volume.stepInterval, 0.001);
    }

    @Test
    public void testDeserializationChromeMirroring() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-chrome-mirroring-1.28.json")
                .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        Status status = response.status;
        assertNotNull(status);
        assertFalse(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        Application app = status.getRunningApp();
        assertFalse(app.isIdleScreen);
        assertFalse(app.launchedFromCloud);

        Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(1.0, volume.level, 0.1);
        assertFalse(volume.muted);
        assertEquals("attenuation", volume.controlType);
        assertEquals(Volume.DEFAULT_INCREMENT, volume.increment, 0.001);
        assertEquals(0.05, volume.stepInterval, 0.001);
    }

    @Test
    public void testDeserializationSpotify() throws Exception {
        final String jsonMSG = FixtureHelper.fixtureAsString("/status-spotify.json")
                                            .replaceFirst("\"type\"", "\"responseType\"");
        final StandardResponse.Status response = jsonMapper.readValue(jsonMSG, StandardResponse.Status.class);

        Status status = response.status;
        assertNotNull(status);
        assertFalse(status.activeInput);
        assertFalse(status.standBy);

        assertEquals(1, status.applications.size());
        Application app = status.getRunningApp();
        assertFalse(app.isIdleScreen);
        assertFalse(app.launchedFromCloud);
        assertEquals("CC32E753", app.id);
        assertEquals("Spotify", app.name);
        assertEquals("https://lh3.googleusercontent.com/HOX9yqNu6y87Chb1lHYqhKVTQW43oFAFFe2ojx94yCLh0yMzgygTrM0RweAexApRWqq6UahgrWYimVgK", app.iconUrl);
        assertEquals(6, app.namespaces.size());
        assertEquals("urn:x-cast:com.google.cast.debugoverlay", app.namespaces.get(0).name);
        assertEquals("urn:x-cast:com.google.cast.cac", app.namespaces.get(1).name);
        assertEquals("urn:x-cast:com.spotify.chromecast.secure.v1", app.namespaces.get(2).name);
        assertEquals("urn:x-cast:com.google.cast.test", app.namespaces.get(3).name);
        assertEquals("urn:x-cast:com.google.cast.broadcast", app.namespaces.get(4).name);
        assertEquals("urn:x-cast:com.google.cast.media", app.namespaces.get(5).name);
        assertEquals("7fb71850-b38b-43bb-967e-e2c76b6d0990", app.sessionId);
        assertEquals("Spotify", app.statusText);
        assertEquals("7fb71850-b38b-43bb-967e-e2c76b6d0990", app.transportId);

        Volume volume = status.volume;
        assertNotNull(volume);
        assertEquals(0.2258118838071823, volume.level, 0.001);
        assertFalse(volume.muted);
        assertEquals("master", volume.controlType);
        assertEquals(Volume.DEFAULT_INCREMENT, volume.increment, 0.001);
        assertEquals(0.019999999552965164, volume.stepInterval, 0.001);
    }
}
